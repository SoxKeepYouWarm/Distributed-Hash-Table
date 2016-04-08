package edu.buffalo.cse.cse486586.simpledht;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Message {

    /* COMMANDS */
    public static String JOIN = "join";
    public static String JOIN_RESPONSE = "join_response";

    public static String INSERT = "insert";
    public static String DELETE = "delete";
    public static String UPDATE_POINTERS = "update_pointers";
    public static String UPDATE_KEYS = "update_keys";

    public static String QUERY = "query";
    public static String QUERY_RESPONSE = "query_response";

    public static String QUERY_ALL = "query_all";
    public static String QUERY_ALL_RESPONSE = "query_all_response";

    public static String QUERY_LOCAL = "query_local";
    public static String QUERY_LOCAL_RESPONSE = "query_local_response";
    /* COMMANDS */

    /* ARGUMENTS */
    public static String PREDECESSOR = "predecessor";
    public static String SUCCESSOR = "successor";
    public static String SELECTION = "selection";
    /* ARGUMENTS */

    public static String DEBUG_NODE_POINTERS = new String(Character.toChars(203));
    public static String DEBUG_NODE_POINTERS_RESPONSE = "debug_node_pointers_response";

    private static String SECTION_BREAKPOINT = new String(Character.toChars(200));
    private static String KEY_VAL_BREAKPOINT = new String(Character.toChars(201));
    private static String PAIR_BREAKPOINT = new String(Character.toChars(202));

    private String command;
    private String sender_port;
    private HashMap<String, String> data = new HashMap<String, String>();

    public Message(String command, String sender_port) {
        this.command = command;
        this.sender_port = sender_port;
    }

    public Message(String message_string) {

        String[] message_components = message_string.split(SECTION_BREAKPOINT);
        String command = message_components[0];
        String sender_port = message_components[1];

        this.command = command;
        this.sender_port = sender_port;

        if (message_components.length == 3) {
            String data_string = message_components[2];

            String[] pairs = data_string.split(PAIR_BREAKPOINT);

            for (String pair: pairs) {
                String[] key_val = pair.split(KEY_VAL_BREAKPOINT);
                data.put(key_val[0], key_val[1]);
            }
        }

    }

    public void insert_args(String key, String val) {
        data.put(key, val);
    }

    public void set_args(Hashtable<String, String> data) {
        this.data = new HashMap<String, String>(data);
    }

    public String stringify() {
        String stringified = command + SECTION_BREAKPOINT;
        stringified += sender_port + SECTION_BREAKPOINT;

        for (Map.Entry<String, String> entry: data.entrySet()) {
            stringified += entry.getKey() + KEY_VAL_BREAKPOINT + entry.getValue() + PAIR_BREAKPOINT;

        }

        stringified += SECTION_BREAKPOINT;
        return stringified;

    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String new_command) {
        this.command = new_command;
    }

    public String getSender_port() {
        return this.sender_port;
    }

    public void setSender_port(String sender_port) {
        this.sender_port = sender_port;
    }

    public HashMap<String, String> get_args() {
        return this.data;
    }

    public String get_arg(String key) {
        return this.data.get(key);
    }

}