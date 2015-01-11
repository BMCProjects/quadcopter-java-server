package QCServer.Managers;

import QCServer.Connections.Connection;
import QCServer.Interfaces.IThreadDelay;
import QCServer.Sessions.Session;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by czifro on 12/27/14.
 *
 * @author Will Czifro
 *
 * An abstract manager, inherit to manage custom Session class and custom Connection class
 */
public abstract class Manager implements IThreadDelay {

    protected Object locker = new Object();
    protected ConcurrentHashMap<String, Session> mappedSessions;

    public Manager()
    {
        mappedSessions = new ConcurrentHashMap<String, Session>();
    }

    @Override
    public void delay(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract void run();
    public abstract void addOrCreate(Connection conn);
    public abstract void startSession(Session s);
}
