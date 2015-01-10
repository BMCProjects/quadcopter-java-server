package QCServer.Sessions;

import QCServer.Connections.*;
import com.sun.istack.internal.Nullable;

/**
 * Created by czifro on 12/20/14.
 */
public abstract class Session {

    protected String sessionId;

    protected StaticConnection client;
    protected DynamicConnection copter;
    protected String copterMsg, clientMsg;



    // Abstract methods
    public abstract void run();
    public abstract void setConnection(Connection conn);
    public abstract Connection getConnection(@Nullable ConnectionType type);
    protected abstract void close();
    public abstract boolean isClosed();
}
