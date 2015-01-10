package QCServer.Managers;

import QCServer.Connections.Connection;
import QCServer.Sessions.Session;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by czifro on 12/27/14.
 *
 * @author Will Czifro
 *
 * An abstract manager, inherit to manage custom Session class and custom Connection class
 */
public abstract class Manager {

    protected Object locker = new Object();
    protected ConcurrentHashMap<String, Session> mappedSessions;

    public Manager()
    {
        mappedSessions = new ConcurrentHashMap<String, Session>();
    }

    public abstract void run();
    protected abstract void delay(long millis);
    public abstract void addOrCreate(Connection conn);
    public abstract void startSession(Session s);
}
