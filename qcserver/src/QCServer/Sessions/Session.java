package QCServer.Sessions;

import QCServer.Connections.*;
import QCServer.Interfaces.IThreadDelay;
import com.sun.istack.internal.Nullable;

/**
 * Created by czifro on 12/20/14.
 */
public abstract class Session implements IThreadDelay {

    protected String sessionId;

    protected String copterMsg, clientMsg;

    @Override
    public void delay(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Abstract methods
    public abstract void run();
    public abstract void setSessionId(String Id);
    public abstract void setConnection(Connection conn);
    public abstract Connection getConnection(@Nullable ConnectionType type);
    public abstract void close();
    public abstract boolean isClosed();
}
