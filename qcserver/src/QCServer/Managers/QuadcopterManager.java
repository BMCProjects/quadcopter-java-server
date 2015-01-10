package QCServer.Managers;

import QCServer.Connections.*;
import QCServer.Sessions.CopterSession;
import QCServer.Sessions.Session;
import net.jsock.MessageSocket;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by czifro on 12/27/14.
 *
 * @author Will Czifro
 *
 * A manager that handles CopterSessions
 */
public class QuadcopterManager extends Manager {

    public QuadcopterManager()
    {
        super();
    }

    @Override
    public void run() {
        while(true)
        {
            Enumeration<String> keys = mappedSessions.keys();

            for (String key : (ArrayList<String>)keys)
            {
                CopterSession cs = (CopterSession) mappedSessions.get(key);

            }

            delay(500);
        }
    }

    @Override
    protected void delay(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addOrCreate(Connection conn)
    {
        if (conn instanceof DynamicConnection)
            addOrCreate((DynamicConnection) conn);
        else if (conn instanceof StaticConnection)
            addOrCreate((StaticConnection) conn);
    }

    /**
     * Adds a DynamicConnection to an existing session or creates a session
     * @param dConn
     */
    private void addOrCreate(DynamicConnection dConn) {
        synchronized (locker)
        {
            if (mappedSessions.containsKey(dConn.getId()))
            {
                CopterSession cs = (CopterSession) mappedSessions.get(dConn.getId());

                if (!cs.isClosed() && cs.getConnection(ConnectionType.Dynamic) == null)
                {
                    cs.setConnection(dConn);
                }
            }
            else
            {
                CopterSession cs = new CopterSession();

                cs.setConnection(dConn);

                mappedSessions.put(dConn.getId(), cs);

                startSession(cs);
            }
        }
    }

    /**
     * Adds a StaticConnection to an existing session or creates a session
     * @param sConn
     */
    private void addOrCreate(StaticConnection sConn) {
        synchronized (locker)
        {
            if (mappedSessions.containsKey(sConn.getId()))
            {
                CopterSession cs = (CopterSession) mappedSessions.get(sConn.getId());

                if (!cs.isClosed() && cs.getConnection(ConnectionType.Static) == null)
                {
                    cs.setConnection(sConn);
                }
            }
            else
            {
                CopterSession cs = new CopterSession();

                cs.setConnection(sConn);

                mappedSessions.put(sConn.getId(), cs);

                startSession(cs);
            }
        }
    }

    public boolean renewDynamicConnection(String dConnId, MessageSocket sock)
    {
        synchronized (locker)
        {
            if (mappedSessions.containsKey(dConnId))
            {
                CopterSession cs = (CopterSession) mappedSessions.get(dConnId);

                if (!cs.isClosed())
                {
                    DynamicConnection dConn = (DynamicConnection) cs.getConnection(ConnectionType.Dynamic);

                    if (dConn.isSuspended())
                    {
                        dConn.reconnect(sock);
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void startSession(final Session s) {
        Thread session = new Thread()
        {
            @Override
            public void run()
            {
                s.run();
            }
        };

        session.start();
    }
}
