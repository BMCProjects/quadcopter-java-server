package QCServer.Protocol;

/**
 * Created by czifro on 12/21/14.
 * An enum of the different types of operations
 */
public enum CommandType {
    // Client command types
    STARTQCCOPTER,
    STOPQCCOPTER,
    GOTOCOORDINATE,
    GO_OFFLINE,
    // Copter command types
    CONFIRM,
    READY,
    SUSPENDCONNECTION,
    TERMINATE,
    NO_OP
}

