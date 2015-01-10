package QCServer;

import QCServer.Connections.DynamicConnection;
import QCServer.Connections.StaticConnection;
import QCServer.Managers.QuadcopterManager;
import QCServer.Protocol.MessageType;
import net.jsock.MessageSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by czifro on 1/10/15.
 */
public class Server {

    private final int PORT = 50000;

    private ServerSocket server;
    private Socket conn;
    private MessageSocket sock;

    private QuadcopterManager qm;

    private boolean run = true;

    public Server() throws IOException
    {
        server = new ServerSocket(PORT);
        qm = new QuadcopterManager();
    }

    public void accept()
    {
        while (run)
        {
            try {
                conn = server.accept();
                Socket s = conn;
                conn = null;
                sock = new MessageSocket(s);
                final MessageSocket ms = sock;
                sock = null;
                Thread process = new Thread()
                {
                    public void run()
                    {
                        processConnection(ms);
                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void processConnection(MessageSocket ms)
    {
        ms.send_msg(MessageType.QCSERVER_ID + "Identify");

        String msg;

        do {
            msg = ms.recv_msg();
        } while (msg.equals(MessageType.FAIL));

        if (msg.contains(MessageType.QCCOPTER_ID)&&msg.contains("ID:"))
        {
            if (msg.contains("Renew")) {
                String id = msg.substring((MessageType.QCCOPTER_ID + "Renew, ID: ").length());
                qm.renewDynamicConnection(id, ms);
            }
            else
            {
                try {
                    DynamicConnection dConn = new DynamicConnection(ms);
                    qm.addOrCreate(dConn);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (msg.contains(MessageType.QCCLIENT_ID)&&msg.contains("ID:"))
        {
            try {
                StaticConnection sConn = new StaticConnection(ms);
                qm.addOrCreate(sConn);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
