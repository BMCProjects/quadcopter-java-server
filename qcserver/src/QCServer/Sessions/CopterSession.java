package QCServer.Sessions;

import QCServer.Connections.*;
import QCServer.Protocol.*;
import com.sun.istack.internal.Nullable;

/**
 * Created by czifro on 12/20/14.
 */
public class CopterSession extends Session {

    private DynamicConnection dConn;
    private StaticConnection sConn;


    @Override
    public void run() {
        while (true)
        {
            if (sessionId.equals("Test"))
            {
                runTest();
                break;
            }
            while (dConn != null && sConn != null)
            {
                while (dConn.isSuspended())
                {
                    if (dConn.missedCheckIn())
                    {
                        switch (dConn.getFailCount())
                        {
                            case 1:
                                delay(15000);
                                continue;
                            case 2:
                                delay(30000);
                                continue;
                            case 3:
                                delay(45000);
                                continue;
                            case 4:
                                close();
                                continue;
                        }
                    }
                    delay(500);
                }

                String msg = dConn.pullMessageToRead();

                if (!msg.contains(MessageType.FAIL))
                    processCopterCommand(getCopterCommandType(msg));

                msg = sConn.pullMessageToRead();

                if (!msg.contains(MessageType.FAIL))
                    processClientCommand(getClientCommandType(msg));
            }
        }
    }

    @Override
    public void setSessionId(String Id)
    {
        sessionId = Id;
    }

    @Override
    public void setConnection(Connection conn) {
        if (conn == null)
            return;
        if (conn instanceof DynamicConnection)
            setDynamicConnection((DynamicConnection) conn);
        else if (conn instanceof StaticConnection)
            setStaticConnection((StaticConnection) conn);
    }

    @Override
    public Connection getConnection(@Nullable ConnectionType type) {
        if (type != null && type == ConnectionType.Dynamic)
            return getDynamicConnection();
        else if (type != null && type == ConnectionType.Static)
            return getStaticConnection();
        return null;
    }

    private void setStaticConnection(StaticConnection sConn)
    {
        if (this.sConn != null)
        {
            this.sConn = sConn;
        }

        if (dConn != null)
        {
            String msg = MessageType.QCSERVER_ID + "Session is ready to begin";
            sConn.addMessageToSend(msg);
            dConn.addMessageToSend(msg);
        }
        else
        {
            String msg = MessageType.QCSERVER_ID + "Waiting for Quadcopter to join session...";
            sConn.addMessageToSend(msg);
        }
    }

    private StaticConnection getStaticConnection()
    {
        return sConn;
    }

    private void setDynamicConnection(DynamicConnection dConn)
    {
        this.dConn = dConn;

        if (sConn != null)
        {
            String msg = MessageType.QCSERVER_ID + "Session is ready to begin";
            sConn.addMessageToSend(msg);
            dConn.addMessageToSend(msg);
        }
        else
        {
            String msg = MessageType.QCSERVER_ID + "Waiting for remote to join session...";
            dConn.addMessageToSend(msg);
        }
    }

    private DynamicConnection getDynamicConnection()
    {
        return dConn;
    }

    private void processCopterCommand(CommandType command)
    {
        String msg;
        if (command == CommandType.SUSPENDCONNECTION)
        {
            if (copterMsg.contains("Duration: "))
            {

            }
            dConn.suspendConnection();
            msg = MessageType.QCSERVER_ID;
            msg += "Quadcopter has gone offline.";
            sConn.addMessageToSend(msg);
        }
        else if (command == CommandType.TERMINATE)
        {
            close();
        }
        else if (command == CommandType.CONFIRM || command == CommandType.READY)
        {
            msg = MessageType.QCSERVER_ID;
            msg += "Quadcopter is ready for a command," + copterMsg;
            sConn.addMessageToSend(msg);
        }
    }

    private void processClientCommand(CommandType command)
    {
        String msg;
        if (command == CommandType.STARTQCCOPTER)
        {
            msg = MessageType.QCSERVER_ID;
            msg += "Start take off sequence.";
            dConn.addMessageToSend(msg);
        }
        else if (command == CommandType.STOPQCCOPTER)
        {
            msg = MessageType.QCSERVER_ID;
            msg += "Start landing sequence.";
            dConn.addMessageToSend(msg);
        }
        else if (command == CommandType.GOTOCOORDINATE)
        {
            msg = MessageType.QCSERVER_ID;
            msg += "Start navigation sequence," + clientMsg;
            dConn.addMessageToSend(msg);
        }
        else if (command == CommandType.GO_OFFLINE)
        {
            msg = MessageType.QCSERVER_ID;
            msg += "Start autonomous sequence," + clientMsg;
            dConn.addMessageToSend(msg);
        }
    }

    private CommandType getCopterCommandType(String msg)
    {
        if (msg.contains(MessageType.READY))
        {
            copterMsg = msg.substring(MessageType.READY.length());
            return CommandType.READY;
        }
        else if (msg.contains(MessageType.CONFIRM))
        {
            copterMsg = msg.substring(MessageType.CONFIRM.length());
            return CommandType.CONFIRM;
        }
        else if (msg.contains(MessageType.SUSPENDCONNECTION))
        {
            copterMsg = msg.substring(MessageType.SUSPENDCONNECTION.length());
            return CommandType.SUSPENDCONNECTION;
        }
        else if (msg.contains(MessageType.TERMINATE))
        {
            copterMsg = msg.substring(MessageType.TERMINATE.length());
            return CommandType.TERMINATE;
        }
        return CommandType.NO_OP;
    }

    private CommandType getClientCommandType(String msg)
    {
        if (msg.contains(MessageType.STARTQCCOPTER))
        {
            clientMsg = msg.substring(MessageType.STARTQCCOPTER.length());
            return CommandType.STARTQCCOPTER;
        }
        else if (msg.contains(MessageType.GOTO))
        {
            clientMsg = msg.substring(MessageType.GOTO.length());
            return CommandType.GOTOCOORDINATE;
        }
        else if (msg.contains(MessageType.GO_OFFLINE))
        {
            clientMsg = msg.substring(MessageType.GO_OFFLINE.length());
            return CommandType.GO_OFFLINE;
        }
        else if (msg.contains(MessageType.STOPQCCOPTER))
        {
            clientMsg = msg.substring(MessageType.STOPQCCOPTER.length());
            return CommandType.STOPQCCOPTER;
        }
        return CommandType.NO_OP;
    }

    private void runTest()
    {
        String msg;

        do{
            msg = dConn.pullMessageToRead();
        } while (msg == null || msg.equals(MessageType.FAIL));

        if (msg.equals(MessageType.SUSPENDCONNECTION))
        {
            dConn.suspendConnection();
        }
    }

    @Override
    public void close()
    {
        if (dConn != null)
            dConn.close();
        if (sConn != null) {
            String msg = MessageType.QCSERVER_ID;
            msg += "Copter has failed to reconnect. Terminating session.";
            sConn.addMessageToSend(msg);

            while (!sConn.pullMessageToRead().equals(MessageType.ACK_TERMINATE)) {
                delay(500);
            }

            sConn.close();
        }
        dConn = null;
        sConn = null;
    }

    @Override
    public boolean isClosed() {
        return dConn == null && sConn == null;
    }
}
