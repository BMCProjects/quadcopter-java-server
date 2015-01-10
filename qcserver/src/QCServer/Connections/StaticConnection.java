package QCServer.Connections;

import QCServer.Protocol.MessageType;
import net.jsock.MessageSocket;

import java.io.IOException;

/**
 * Created by czifro on 12/19/14.
 *
 * @author Will Czifro
 *
 * Represents a connection that will persist through out a session.
 */
public class StaticConnection extends Connection {

    public StaticConnection(MessageSocket sock) throws IOException {
        super(sock);

        Thread read = new Thread()
        {
            public void run()
            {
                read();
            }
        };

        Thread write = new Thread()
        {
            public void run()
            {
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
    protected void read()
    {
        while (true)
        {
            if (connectionClosed())
                break;
            String s_size = sock.recv_msg();
            if (!s_size.contains(MessageType.QCCLIENT_MSGSIZE))
                continue;
            int size = Integer.parseInt(s_size.substring(s_size.indexOf(MessageType.QCCLIENT_MSGSIZE)));
            String message = sock.recv_all_msg(size);

            if (!message.contains(MessageType.QCCLIENT_ID))
                continue;

            addMessageToRead(message);
        }
    }

    /**
     * Writes a message to the socket stream
     */
    @Override
    protected void write()
    {
        while (true)
        {
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
     * Adds a message to a queue of messages to send
     * @param msg
     */
    public void addMessageToSend(String msg)
    {
        synchronized (locker)
        {
            try {
                messagesToSend.put((Object) msg);
            } catch (InterruptedException e) {
                System.out.println("Failed to add message to send queue for static connection");
            }
        }
    }

    /**
     * Pulls the first message from queue to send
     * @return String
     */
    protected String pullMessageToSend()
    {
        synchronized (locker)
        {
            if (!messagesToRead.isEmpty())
            {
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
     * @param msg
     */
    protected void addMessageToRead(String msg)
    {
        synchronized (locker)
        {
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
     * @return
     */
    public String pullMessageToRead()
    {
        synchronized (locker)
        {
            if (!messagesToRead.isEmpty())
            {
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
     * @return
     */
    public boolean missedCheckIn()
    {
        synchronized (locker)
        {
            return ttl < System.currentTimeMillis() - lastCheckin;
        }
    }

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
