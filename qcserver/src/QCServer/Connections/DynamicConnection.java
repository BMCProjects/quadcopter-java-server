package QCServer.Connections;

import QCServer.Protocol.MessageType;
import net.jsock.MessageSocket;

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

    public DynamicConnection(MessageSocket sock) {
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
            if (connectionClosed())
                break;
            if (isSuspended()) {
                delay(500);
                continue;
            }

            String message = MessageType.FAIL;

            if (sock == null || sock.isClosed()) {
                if (isSuspended()) {
                    delay(500);
                    continue;
                }
                break;
            }
            String s_size = sock.recv_msg();
            if (!s_size.contains(MessageType.QCCOPTER_MSGSIZE))
                continue;
            sock.send_msg(MessageType.QCSERVER_ID + "OK");
            int size = Integer.parseInt(s_size.substring(MessageType.QCCOPTER_MSGSIZE.length()));
            message = sock.recv_all_msg(size);

            if (!message.equals(MessageType.FAIL)) {
                if (!message.contains(MessageType.QCCOPTER_ID))
                    continue;
                message = message.substring(MessageType.QCCOPTER_ID.length());
                addMessageToRead(message);
            }
        }
    }

    /**
     * Writes a message to the socket stream
     */
    @Override
    protected void write() {
        while (true) {
            if (connectionClosed())
                break;
            if (isSuspended()) {
                delay(500);
                continue;
            }
            String message = pullMessageToResend();

            if (message == null || message.equals(MessageType.FAIL)) {
                message = pullMessageToSend();

                if (message == null || message.equals(MessageType.FAIL))
                    continue;
            }

            if (sock == null || sock.isClosed()) {
                if (isSuspended()) {
                    addMessageToResend(message);
                    delay(500);
                    continue;
                }
                break;
            }
            sock.send_msg(message);
        }
    }

    /**
     * Reconnects to a new socket
     * @param sock, a MessageSocket object
     * @throws java.io.IOException
     */
    public void reconnect(MessageSocket sock) {
        synchronized (locker) {
            this.sock = sock;
            suspended = false;
        }
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
                System.out.println("Failed to add message to send queue for dynamic connection");
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
            if (!messagesToSend.isEmpty()) {
                try {
                    return (String) messagesToSend.take();
                } catch (InterruptedException e) {
                    return "<FAIL>";
                }
            }
        }
        return null;
    }

    @Override
    protected void addMessageToResend(String msg)
    {
        synchronized (locker) {
            try {
                messagesToResend.put((Object) msg);
            } catch(InterruptedException e)
            {
                System.out.println("Failed to add messages to resend queue for dynamic connection");
            }
        }
    }

    @Override
    protected String pullMessageToResend()
    {
        synchronized (locker) {
            if (!messagesToResend.isEmpty()) {
                try {
                    return (String) messagesToResend.take();
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
                System.out.println("Failed to add message to read queue for dynamic connection");
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
        while (!messagesToSend.isEmpty()) { delay(10); }
        synchronized (locker)
        {
            closedConnection = true;
            sock.close();
        }
    }
}
