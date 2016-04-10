package edu.buffalo.cse.cse486586.simpledht;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Util {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    public static final int IS_MY_NODE = 1;
    public static final int IS_SUCCESSOR_NODE = 2;
    public static final int IS_PREDECESSOR_NODE = 3;
    public static final int BETWEEN_SUCCESSOR_NODE = 4;
    public static final int BETWEEN_PREDECESSOR_NODE = 5;
    public static final int AFTER_SUCCESSOR_NODE = 6;
    public static final int BEFORE_PREDECESSOR_NODE = 7;

    private static Connection_state connection_state;

    public static int find_relationship(String sender_id) {

        if (connection_state == null) {
            connection_state = Connection_state.getConnectionState();
        }

        Log.d(TAG, "FIND_RELATIONSHIP: starting");
        if (sender_id.equals(connection_state.MY_NODE_ID)) {
            return IS_MY_NODE;
        }

        if (sender_id.equals(connection_state.SUCCESSOR_NODE_ID)) {
            return IS_SUCCESSOR_NODE;
        }

        if (sender_id.equals(connection_state.PREDECESSOR_NODE_ID)) {
            return IS_PREDECESSOR_NODE;
        }


        if (Provider_handlers.NODE_POSITION == Provider_handlers.ONLY_NODE) {

            Log.d(TAG, "FIND_RELATIONSHIP: called when only node");
            return -1;


        } else if (Provider_handlers.NODE_POSITION == Provider_handlers.FIRST_NODE) {

            if (less_than(sender_id, connection_state.MY_NODE_ID)
                    || greater_than(sender_id, connection_state.PREDECESSOR_NODE_ID)) {
                return BETWEEN_PREDECESSOR_NODE;
            }

            if (greater_than(sender_id, connection_state.MY_NODE_ID)
                    && less_than(sender_id, connection_state.SUCCESSOR_NODE_ID)) {
                return BETWEEN_SUCCESSOR_NODE;
            }

            if (greater_than(sender_id, connection_state.SUCCESSOR_NODE_ID)) {
                return AFTER_SUCCESSOR_NODE;
            }

            Log.e(TAG, "FIND_RELATIONSHIP: error with node_position: first_node");
            return -1;

        } else if (Provider_handlers.NODE_POSITION == Provider_handlers.LAST_NODE) {

            if (greater_than(sender_id, connection_state.MY_NODE_ID)
                    || less_than(sender_id, connection_state.SUCCESSOR_NODE_ID)) {
                return BETWEEN_SUCCESSOR_NODE;
            }

            if (less_than(sender_id, connection_state.MY_NODE_ID)
                    && greater_than(sender_id, connection_state.PREDECESSOR_NODE_ID)) {
                return BETWEEN_PREDECESSOR_NODE;
            }

            if (less_than(sender_id, connection_state.PREDECESSOR_NODE_ID)) {
                return BEFORE_PREDECESSOR_NODE;
            }

            Log.e(TAG, "FIND_RELATIONSHIP: error with node_position: last_node");
            return -1;

        } else if (Provider_handlers.NODE_POSITION == Provider_handlers.MIDDLE_NODE) {

            if (greater_than(sender_id, connection_state.MY_NODE_ID)
                    && greater_than(sender_id, connection_state.SUCCESSOR_NODE_ID)) {
                return AFTER_SUCCESSOR_NODE;
            }

            if (greater_than(sender_id, connection_state.MY_NODE_ID)
                    && less_than(sender_id, connection_state.SUCCESSOR_NODE_ID)) {
                return BETWEEN_SUCCESSOR_NODE;
            }

            if (less_than(sender_id, connection_state.MY_NODE_ID)
                    && less_than(sender_id, connection_state.SUCCESSOR_NODE_ID)) {
                return BEFORE_PREDECESSOR_NODE;
            }

            if (less_than(sender_id, connection_state.MY_NODE_ID)
                    && greater_than(sender_id, connection_state.PREDECESSOR_NODE_ID)) {
                return BEFORE_PREDECESSOR_NODE;
            }

            Log.e(TAG, "FIND_RELATIONSHIP: error with node_position: middle_node");
            return -1;

        } else {
            Log.e(TAG, "FIND_RELATIONSHIP: error with node position");
            return -1;
        }
    }

    public static boolean greater_than(String a, String b) {
        return a.compareTo(b) > 0;
    }

    public static boolean less_than(String a, String b) {
        return a.compareTo(b) < 0;
    }

    public static String port_to_hash(String port) {
        String hash = "";
        try {
            hash = genHash(Integer.toString(Integer.parseInt(port) / 2));
        } catch (NoSuchAlgorithmException err) {
            Log.e(TAG, "error hashing " + port);
        }
        return hash;
    }

    public static String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}
