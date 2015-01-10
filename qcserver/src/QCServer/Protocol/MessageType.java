package QCServer.Protocol;

/**
 * Created by czifro on 12/20/14.
 *
 * @author Will Czifro
 * Message tags recognized by this system
 */
public class MessageType {

    ///// Message tags used by Desktop Client
    /// Messages from Desktop Client begin with one of these tags
    public static String QCCLIENT_MSGSIZE = "<QCCLIENT-MSGSIZE>";
    public static String QCCLIENT_ID = "<QCCLIENT>";
    ///

    public static String STARTQCCOPTER = "<START>";
    public static String STOPQCCOPTER = "<STOP>";
    public static String GOTO = "<GOTO>";
    public static String GO_OFFLINE = "<GO-OFFLINE>";
    public static String ACK_TERMINATE = "<ACK-TERMINATE>";
    /////


    ///// Message tags used by Quadcopter Client
    /// Messages from Quadcopter Client begin with one of these tags
    public static String QCCOPTER_MSGSIZE = "<QCCOPTER-MSGSIZE>";
    public static String QCCOPTER_ID = "<QCCOPTER>";
    ///

    public static String ONLINE = "<ONLINE>";
    public static String CONFIRM = "<CONFIRM>";
    public static String READY = "<READY>";
    public static String SUSPENDCONNECTION = "<SUSPENDCONNECTION>";
    public static String TERMINATE = "<TERMINATE>";
    /////

    ///// Message tags the server uses
    public static String QCSERVER_ID = "<QCSERVER>";
    public static String FAIL = "<FAIL>";
    /////
}
