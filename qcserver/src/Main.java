import QCServer.Server;

import java.io.IOException;

/**
 * Created by czifro on 1/10/15.
 */
public class Main {

    public static void main(String [] args)
    {
        try {
            Server server = new Server();
            server.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
