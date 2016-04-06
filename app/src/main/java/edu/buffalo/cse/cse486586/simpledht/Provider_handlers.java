package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Provider_handlers {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    private static int ONLY_NODE = 1;
    private static int FIRST_NODE = 2;
    private static int LAST_NODE = 3;
    private static int MIDDLE_NODE = 4;

    private static int NODE_POSITION;

    SimpleDhtProvider provider_reference;

    public Provider_handlers(SimpleDhtProvider provider) {
        this.provider_reference = provider;
    }

    public static void send_message(Message message, String destination_port) {
        Client_param_wrapper params = new Client_param_wrapper(message, destination_port);
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, params);
    }

    public static void handle_join_request(Message message) {
        if ( ! SimpleDhtProvider.MY_PORT.equals(SimpleDhtProvider.MASTER_NODE_PORT) ) {
            Log.e(TAG, "non master node received join request from node: " + message.getSender_port());
        }
        Log.d(TAG, "received join request from node: " + message.getSender_port());

        String sender_port = message.getSender_port();
        String sender_node_id;

        try {
            sender_node_id = genHash(sender_port);
        } catch (NoSuchAlgorithmException err) {
            Log.e(TAG, "HANDLE_JOIN_REQUEST: error generating port hash");
            sender_node_id = "";
        }

        if (NODE_POSITION == ONLY_NODE) {

            // add pointers to sender in my node
            SimpleDhtProvider.SUCCESSOR_PORT = sender_port;
            SimpleDhtProvider.SUCCESSOR_NODE_ID = sender_node_id;
            SimpleDhtProvider.PREDECESSOR_PORT = sender_port;
            SimpleDhtProvider.PREDECESSOR_NODE_ID = sender_node_id;

            // return predecessor and successor info for sender
            Message response_message = new Message(Message.JOIN_RESPONSE, SimpleDhtProvider.MY_PORT);
            response_message.insert_args(Message.PREDECESSOR, SimpleDhtProvider.MY_PORT);
            response_message.insert_args(Message.SUCCESSOR, SimpleDhtProvider.MY_PORT);

            send_message(response_message, sender_port);

        } else if (NODE_POSITION == FIRST_NODE) {

            Message response_message;

            switch (find_relationship(sender_node_id)) {

                case IS_MY_NODE:
                case IS_SUCCESSOR_NODE:
                case IS_PREDECESSOR_NODE:
                case BEFORE_PREDECESSOR_NODE:
                    Log.e(TAG, "received join request with invalid case");
                    break;
                case AFTER_SUCCESSOR_NODE:
                    send_message(message, SimpleDhtProvider.SUCCESSOR_PORT);
                    Log.d(TAG, "forwarded join request to successor node");
                    break;
                case BETWEEN_SUCCESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.SUCCESSOR, SimpleDhtProvider.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // TODO update successor node's predecessor pointer
                    Log.d(TAG, "responded to join request: between_successor_node");
                    break;
                case BETWEEN_PREDECESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, SimpleDhtProvider.PREDECESSOR_PORT);
                    response_message.insert_args(Message.SUCCESSOR, SimpleDhtProvider.MY_PORT);

                    send_message(response_message, sender_port);

                    // TODO update predecessors node's successor pointer
                    Log.d(TAG, "responded to join request: between_predecessor_node");
                    break;
                default:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: error as first_node");

            }

        } else if (NODE_POSITION == LAST_NODE) {

            int relationship = find_relationship(sender_node_id);
            Message response_message;

            switch (relationship) {

                case IS_MY_NODE:
                case IS_SUCCESSOR_NODE:
                case IS_PREDECESSOR_NODE:
                case AFTER_SUCCESSOR_NODE:
                    Log.e(TAG, "received join request with invalid case");
                    break;
                case BEFORE_PREDECESSOR_NODE:
                    send_message(message, SimpleDhtProvider.PREDECESSOR_PORT);
                    Log.d(TAG, "forwarded join request to predecessor node");
                    break;
                case BETWEEN_SUCCESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.SUCCESSOR, SimpleDhtProvider.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // TODO update successor node's predecessor pointer
                    Log.d(TAG, "responded to join request: between_successor_node");
                    break;
                case BETWEEN_PREDECESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, SimpleDhtProvider.PREDECESSOR_PORT);
                    response_message.insert_args(Message.SUCCESSOR, SimpleDhtProvider.MY_PORT);

                    send_message(response_message, sender_port);

                    // TODO update successor node's predecessor pointer
                    Log.d(TAG, "responded to join request: between_predecessor_node");
                    break;
                default:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: error as last_node, relationship: " + relationship);

            }

        } else if (NODE_POSITION == MIDDLE_NODE) {

            Message response_message;

            switch (find_relationship(sender_node_id)) {

                case IS_MY_NODE:
                case IS_SUCCESSOR_NODE:
                case IS_PREDECESSOR_NODE:
                case AFTER_SUCCESSOR_NODE:
                    send_message(message, SimpleDhtProvider.SUCCESSOR_PORT);
                    Log.d(TAG, "forwarded join request to successor node");
                    break;
                case BEFORE_PREDECESSOR_NODE:
                    send_message(message, SimpleDhtProvider.PREDECESSOR_PORT);
                    Log.d(TAG, "forwarded join request to predecessor node");
                    break;
                case BETWEEN_SUCCESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.SUCCESSOR, SimpleDhtProvider.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // TODO update successor node's predecessor pointer
                    Log.d(TAG, "responded to join request: between_successor_node");
                    break;
                case BETWEEN_PREDECESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, SimpleDhtProvider.MY_PORT);
                    response_message.insert_args(Message.SUCCESSOR, SimpleDhtProvider.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // TODO update successor node's predecessor pointer
                    Log.d(TAG, "responded to join request: between_predecessor_node");
                    break;
                default:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: error as middle_node");
            }

        }

        else Log.e(TAG, "HANDLE_JOIN_REQUEST: node position was not set");

    }

    public static void handle_join_response(Message message) {
        Log.d(TAG, "received join response from master node");
        SimpleDhtProvider.PREDECESSOR_PORT = message.get_arg(Message.PREDECESSOR);
        SimpleDhtProvider.SUCCESSOR_PORT = message.get_arg(Message.SUCCESSOR);

        try {
            SimpleDhtProvider.PREDECESSOR_NODE_ID = genHash(SimpleDhtProvider.PREDECESSOR_PORT);
            SimpleDhtProvider.SUCCESSOR_NODE_ID = genHash(SimpleDhtProvider.SUCCESSOR_PORT);
        } catch (NoSuchAlgorithmException err) {
            Log.e(TAG, "HANDLE_JOIN_RESPONSE: error generating hash");
        }

        SimpleDhtProvider.CONNECTED = true;
    }

    public static void handle_query(Message message) {

    }

    public static void handle_insert(Message message) {

    }

    public static void handle_delete(Message message) {

    }

    public static void determine_node_position() {

        if (SimpleDhtProvider.MY_NODE_ID.equals(SimpleDhtProvider.SUCCESSOR_NODE_ID) &&
                SimpleDhtProvider.MY_NODE_ID.equals(SimpleDhtProvider.PREDECESSOR_NODE_ID)) {
            NODE_POSITION = ONLY_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: this node is only node");
        } else if (less_than(SimpleDhtProvider.MY_NODE_ID, SimpleDhtProvider.PREDECESSOR_NODE_ID)) {
            NODE_POSITION = FIRST_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: this node is first node");
        } else if (greater_than(SimpleDhtProvider.MY_NODE_ID, SimpleDhtProvider.SUCCESSOR_NODE_ID)) {
            NODE_POSITION = LAST_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: this node is last node");
        } else {
            NODE_POSITION = MIDDLE_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: this node is middle node");
        }

    }

    public static void route_incoming_message(Message message) {
        if (SimpleDhtProvider.CONNECTED) determine_node_position();

        if (message.getCommand().equals(Message.QUERY)) handle_query(message);
        else if (message.getCommand().equals(Message.INSERT)) handle_insert(message);
        else if (message.getCommand().equals(Message.DELETE)) handle_delete(message);
        else if (message.getCommand().equals(Message.JOIN)) handle_join_request(message);
        else if (message.getCommand().equals(Message.JOIN_RESPONSE)) handle_join_response(message);
        else Log.e(TAG, "ROUTE_INCOMING_MESSAGE: message was not routed correctly");
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


    private static final int IS_MY_NODE = 1;
    private static final int IS_SUCCESSOR_NODE = 2;
    private static final int IS_PREDECESSOR_NODE = 3;
    private static final int BETWEEN_SUCCESSOR_NODE = 4;
    private static final int BETWEEN_PREDECESSOR_NODE = 5;
    private static final int AFTER_SUCCESSOR_NODE = 6;
    private static final int BEFORE_PREDECESSOR_NODE = 7;

    public static int find_relationship(String sender_id) {

        if (sender_id.equals(SimpleDhtProvider.MY_NODE_ID)) {
            return IS_MY_NODE;
        }

        if (sender_id.equals(SimpleDhtProvider.SUCCESSOR_NODE_ID)) {
            return IS_SUCCESSOR_NODE;
        }

        if (sender_id.equals(SimpleDhtProvider.PREDECESSOR_NODE_ID)) {
            return IS_PREDECESSOR_NODE;
        }


        if (NODE_POSITION == ONLY_NODE) {

            Log.e(TAG, "FIND_RELATIONSHIP: called when only node");
            return -1;


        } else if (NODE_POSITION == FIRST_NODE) {

            if (less_than(sender_id, SimpleDhtProvider.MY_NODE_ID)
                    || greater_than(sender_id, SimpleDhtProvider.PREDECESSOR_NODE_ID)) {
                return BETWEEN_PREDECESSOR_NODE;
            }

            if (greater_than(sender_id, SimpleDhtProvider.MY_NODE_ID)
                    && less_than(sender_id, SimpleDhtProvider.SUCCESSOR_NODE_ID)) {
                return BETWEEN_SUCCESSOR_NODE;
            }

            if (greater_than(sender_id, SimpleDhtProvider.SUCCESSOR_NODE_ID)) {
                return AFTER_SUCCESSOR_NODE;
            }

            Log.e(TAG, "FIND_RELATIONSHIP: error with node_position: first_node");
            return -1;

        } else if (NODE_POSITION == LAST_NODE) {

            if (greater_than(sender_id, SimpleDhtProvider.MY_NODE_ID)
                    || less_than(sender_id, SimpleDhtProvider.SUCCESSOR_NODE_ID)) {
                return BETWEEN_SUCCESSOR_NODE;
            }

            if (less_than(sender_id, SimpleDhtProvider.MY_NODE_ID)
                    && greater_than(sender_id, SimpleDhtProvider.PREDECESSOR_NODE_ID)) {
                return BETWEEN_PREDECESSOR_NODE;
            }

            if (less_than(sender_id, SimpleDhtProvider.PREDECESSOR_NODE_ID)) {
                return BEFORE_PREDECESSOR_NODE;
            }

            Log.e(TAG, "FIND_RELATIONSHIP: error with node_position: last_node");
            return -1;

        } else if (NODE_POSITION == MIDDLE_NODE) {

            if (greater_than(sender_id, SimpleDhtProvider.MY_NODE_ID)
                    && greater_than(sender_id, SimpleDhtProvider.SUCCESSOR_NODE_ID)) {
                return AFTER_SUCCESSOR_NODE;
            }

            if (greater_than(sender_id, SimpleDhtProvider.MY_NODE_ID)
                    && less_than(sender_id, SimpleDhtProvider.SUCCESSOR_NODE_ID)) {
                return BETWEEN_SUCCESSOR_NODE;
            }

            if (less_than(sender_id, SimpleDhtProvider.MY_NODE_ID)
                    && less_than(sender_id, SimpleDhtProvider.SUCCESSOR_NODE_ID)) {
                return BEFORE_PREDECESSOR_NODE;
            }

            if (less_than(sender_id, SimpleDhtProvider.MY_NODE_ID)
                    && greater_than(sender_id, SimpleDhtProvider.PREDECESSOR_NODE_ID)) {
                return BEFORE_PREDECESSOR_NODE;
            }

            Log.e(TAG, "FIND_RELATIONSHIP: error with node_position: middle_node");
            return -1;

        } else {
            Log.e(TAG, "FIND_RELATIONSHIP: error with node position");
            return -1;
        }
    }

    private static boolean greater_than(String a, String b) {
        return a.compareTo(b) > 0;
    }

    private static boolean less_than(String a, String b) {
        return a.compareTo(b) < 0;
    }

}
