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
    public static String DELETE_LOCAL = "delete_local";
    public static String DELETE_ALL = "delete_all";

    public static String UPDATE_POINTERS = "update_pointers";
    public static String UPDATE_KEYS = "update_keys";

    public static String QUERY = "query";
    public static String QUERY_RESPONSE = "query_response";

    public static String QUERY_ALL = "query_all";
    public static String QUERY_ALL_RESPONSE = "query_all_response";

    public static String QUERY_LOCAL = "query_local";
    /* COMMANDS */

    /* ARGUMENTS */
    public static String PREDECESSOR = "predecessor";
    public static String SUCCESSOR = "successor";

    public static String QUERY_SELECTION = "query_selection";
    public static String INSERT_KEY = "insert_key";
    public static String INSERT_VALUE = "insert_value";
    public static String DELETE_KEY = "delete_key";

    public static String KEY = "key";
    public static String VALUE = "value";
    public static String QUERY_NOT_FOUND = new String(Character.toChars(203));
    /* ARGUMENTS */

    public static String DEBUG_NODE_POINTERS = new String(Character.toChars(199));
    public static String DEBUG_NODE_POINTERS_RESPONSE = "debug_node_pointers_response";

    private static String SECTION_BREAKPOINT = new String(Character.toChars(200));
    private static String KEY_VAL_BREAKPOINT = new String(Character.toChars(201));
    private static String PAIR_BREAKPOINT = new String(Character.toChars(202));
    private static String NULL_BUFFER = new String(Character.toChars(204));


    private String command;
    private String sender_port;
    private HashMap<String, String> data = new HashMap<String, String>();
    private HashMap<String, String> messages = new HashMap<String, String>();

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

        if (message_components[2].equals(NULL_BUFFER)) {
            // args table is empty
        } else {
            String data_string = message_components[2];

            String[] pairs = data_string.split(PAIR_BREAKPOINT);

            for (String pair: pairs) {
                String[] key_val = pair.split(KEY_VAL_BREAKPOINT);
                data.put(key_val[0], key_val[1]);
            }
        }


        if (message_components[3].equals(NULL_BUFFER)) {
            // message table is empty
        } else {
            String data_string = message_components[3];

            String[] pairs = data_string.split(PAIR_BREAKPOINT);

            for (String pair: pairs) {
                String[] key_val = pair.split(KEY_VAL_BREAKPOINT);
                messages.put(key_val[0], key_val[1]);
            }
        }

    }

    public void insert_arg(String key, String val) {
        data.put(key, val);
    }

    public String get_arg(String key) {
        return this.data.get(key);
    }

    public void insert_message(String key, String value) {
        messages.put(key, value);
    }

    public String get_message(String key) {
        return this.messages.get(key);
    }

    public HashMap<String, String> get_messages() {
        return this.messages;
    }

    public void set_args(Hashtable<String, String> data) {
        this.data = new HashMap<String, String>(data);
    }

    public String stringify() {
        String stringified = command + SECTION_BREAKPOINT;
        stringified += sender_port + SECTION_BREAKPOINT;

        if (data.isEmpty()) {
            stringified += NULL_BUFFER;
        } else {
            for (Map.Entry<String, String> entry: data.entrySet()) {
                stringified += entry.getKey() + KEY_VAL_BREAKPOINT + entry.getValue() + PAIR_BREAKPOINT;

            }
        }

        stringified += SECTION_BREAKPOINT;

        if (messages.isEmpty()) {
            stringified += NULL_BUFFER;
        } else {
            for (Map.Entry<String, String> entry: messages.entrySet()) {
                stringified += entry.getKey() + KEY_VAL_BREAKPOINT + entry.getValue() + PAIR_BREAKPOINT;

            }
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

}