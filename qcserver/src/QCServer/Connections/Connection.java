package QCServer.Connections;

import net.jsock.MessageSocket;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by czifro on 12/20/14.
 */
public abstract class Connection {

    protected String id;

    // Time-to-Live variable, duration connection has to check-in
    protected long ttl;

    // The time the connection last checked in
    protected long lastCheckin;

    // TCP variables
    protected MessageSocket sock;

    // Messaging variable
    protected LinkedBlockingQueue<Object> messagesToRead;
    protected LinkedBlockingQueue<Object> messagesToSend;

    // Mutex variable
    protected Object locker = new Object();

    // Ends threads of true
    protected boolean closedConnection = false;

    public Connection(MessageSocket sock) throws IOException
    {
        this.sock = sock;
        ttl = 5000l;
        lastCheckin = System.currentTimeMillis();
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    // Abstract I/O methods
    protected abstract void read();
    protected abstract void write();

    // Abstract queueing methods
    public abstract void addMessageToSend(String msg);
    protected abstract String pullMessageToSend();
    protected abstract void addMessageToRead(String msg);
    public abstract String pullMessageToRead();

    // Abstract protocol methods
    public abstract boolean missedCheckIn();
    protected abstract boolean connectionClosed();
    public abstract void close();
}
