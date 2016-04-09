package edu.buffalo.cse.cse486586.simpledht;

public class Connection_state {

    private static Connection_state state;

    public String MY_PORT;
    public String MY_NODE_ID;

    public String PREDECESSOR_PORT;
    public String PREDECESSOR_NODE_ID;

    public String SUCCESSOR_PORT;
    public String SUCCESSOR_NODE_ID;

    public boolean CONNECTED = false;

    private Connection_state() {
        MY_PORT = "";
        MY_NODE_ID = "";
        PREDECESSOR_PORT = "";
        PREDECESSOR_NODE_ID = "";
        SUCCESSOR_PORT = "";
        SUCCESSOR_NODE_ID = "";
        CONNECTED = false;
    }

    public static Connection_state getConnectionState() {
        if (state == null) {
            state = new Connection_state();
            return state;
        } else {
            return state;
        }
    }

}
