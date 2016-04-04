package edu.buffalo.cse.cse486586.simpledht;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Message {

    public static String JOIN = "join";
    public static String JOIN_RESPONSE = "join_response";
    public static String INSERT = "insert";
    public static String QUERY = "query";

    private static String SECTION_BREAKPOINT = Character.toString(Character.toChars(200)[0]);
    private static String KEY_VAL_BREAKPOINT = Character.toString(Character.toChars(201)[0]);
    private static String PAIR_BREAKPOINT = Character.toString(Character.toChars(202)[0]);

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

        String data_string = message_components[2];

        String[] pairs = data_string.split(PAIR_BREAKPOINT);

        for (String pair: pairs) {
            String[] key_val = pair.split(KEY_VAL_BREAKPOINT);
            data.put(key_val[0], key_val[1]);
        }


    }

    public void insert_key_val(String key, String val) {
        data.put(key, val);
    }

    public void set_data(Hashtable<String, String> data) {
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

    public String getSender_port() {
        return this.sender_port;
    }

    public HashMap<String, String> get_message_data() {
        return this.data;
    }

}