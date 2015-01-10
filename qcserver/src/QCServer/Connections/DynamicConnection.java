package QCServer.Connections;

import QCServer.Protocol.MessageType;
import net.jsock.MessageSocket;

import java.io.IOException;

/**
 * Created by czifro on 12/19/14.
 *
 * @author Will Czifro
 *
 * Represents a connection that allows the client to temporarily break connection
 */
public class DynamicConnection extends Connection {

    // Time-of-Suspension, the time when the connection was suspended
    private long tos;

    // Timeout count
    private int failCount = 0;

    // Connection is suspended if true, otherwise connection is active
    private boolean suspended = false;

    public DynamicConnection(MessageSocket sock) throws IOException {
        super(sock);

        Thread read = new Thread() {
            public void run() {
                read();
            }
        };

        Thread write = new Thread() {
            public void run() {
                write();
            }
        };

        read.start();
        write.start();
    }

    /**
     * Reads a message from socket stream and adds it to a queue;
     */
    @Override
    protected void read() {
        while (true) {
            if (isSuspended())
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (connectionClosed())
                break;
            String s_size = sock.recv_msg();
            if (!s_size.contains(MessageType.QCCOPTER_MSGSIZE))
                continue;
            int size = Integer.parseInt(s_size.substring(s_size.indexOf(MessageType.QCCOPTER_MSGSIZE)));
            String message = sock.recv_all_msg(size);

            if (!message.contains(MessageType.QCCOPTER_ID))
                continue;

            addMessageToRead(message);
        }
    }

    /**
     * Writes a message to the socket stream
     */
    @Override
    protected void write() {
        while (true) {
            if (isSuspended())
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (connectionClosed())
                break;
            String message = pullMessageToSend();

            if (message == null)
                continue;

            if (message.equals("<FAIL>"))
                continue;

            sock.send_msg(message);
        }
    }

    /**
     * Reconnects to a new socket
     * @param sock, a MessageSocket object
     * @throws java.io.IOException
     */
    public void reconnect(MessageSocket sock) {
        this.sock = sock;
        suspended = false;
    }

    /**
     * Puts connection in a suspended state
     */
    public void suspendConnection() {
        synchronized (locker) {
            sock.close();
            sock = null;
            suspended = true;
        }
    }

    /**
     * Adds a message to a queue of messages to send
     *
     * @param msg
     */
    @Override
    public void addMessageToSend(String msg) {
        synchronized (locker) {
            try {
                messagesToSend.put((Object) msg);
            } catch (InterruptedException e) {
                System.out.println("Failed to add message to send queue for static connection");
            }
        }
    }

    /**
     * Pulls the first message from queue to send
     *
     * @return String
     */
    @Override
    protected String pullMessageToSend() {
        synchronized (locker) {
            if (!messagesToRead.isEmpty()) {
                try {
                    return (String) messagesToSend.take();
                } catch (InterruptedException e) {
                    return "<FAIL>";
                }
            }
        }
        return null;
    }

    /**
     * Adds a message to a queue of messages to read
     *
     * @param msg
     */
    @Override
    protected void addMessageToRead(String msg) {
        synchronized (locker) {
            try {
                messagesToRead.put((Object) msg);
                lastCheckin = System.currentTimeMillis();
            } catch (InterruptedException e) {
                System.out.println("Failed to add message to read queue for static connection");
            }
        }
    }

    /**
     * Pulls the first message from queue to read
     *
     * @return
     */
    @Override
    public String pullMessageToRead() {
        synchronized (locker) {
            if (!messagesToRead.isEmpty()) {
                try {
                    return (String) messagesToRead.take();
                } catch (InterruptedException e) {
                    return "<FAIL>";
                }
            }
        }
        return null;
    }

    /**
     * Checks if connection has timed out
     *
     * @return
     */
    @Override
    public boolean missedCheckIn() {
        synchronized (locker) {
            if (suspended)
            {
                return (tos * 1000l) < System.currentTimeMillis() - lastCheckin;
            }
            return ttl < System.currentTimeMillis() - lastCheckin;
        }
    }

    /**
     * Checks if connection is in a suspended state
     *
     * @return
     */
    public boolean isSuspended()
    {
        return suspended;
    }

    public void incrementFailCount()
    {
        ++failCount;
    }

    @Override
    protected boolean connectionClosed()
    {
        synchronized (locker)
        {
            return closedConnection;
        }
    }

    public void setTTL(long ttl)
    {
        this.ttl = ttl;
    }

    public long getTTL()
    {
        return ttl;
    }

    public void setTOS(long tos) { this.tos = tos; }

    public int getFailCount()
    {
        return failCount;
    }

    /**
     * Closes socket streams and terminates threads
     */
    public void close()
    {
        sock.close();
        synchronized (locker)
        {
            closedConnection = true;
        }
    }
}
