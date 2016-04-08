package edu.buffalo.cse.cse486586.simpledht;

public class Connection_state {

    public String MY_PORT;
    public String MY_NODE_ID;

    public String PREDECESSOR_PORT;
    public String PREDECESSOR_NODE_ID;

    public String SUCCESSOR_PORT;
    public String SUCCESSOR_NODE_ID;

    public boolean CONNECTED = false;

    public Connection_state() {
        CONNECTED = false;
    }

}
