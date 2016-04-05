package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Provider_handlers {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();
    static SimpleDhtProvider provider_reference;

    public static void send_message(Message message, String destination_port) {
        Client_param_wrapper params = new Client_param_wrapper(message, destination_port);
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, params);
    }

    public static void handle_join_request(Message message) {
        if ( ! SimpleDhtProvider.MY_PORT.equals(SimpleDhtProvider.MASTER_NODE_PORT) ) {
            Log.e(TAG, "non master node received join request from node: " + message.getSender_port());
        }
        Log.d(TAG, "received join request from node: " + message.getSender_port());

        if (SimpleDhtProvider.MY_NODE_ID.equals(SimpleDhtProvider.SUCCESSOR_NODE_ID) &&
                SimpleDhtProvider.MY_NODE_ID.equals(SimpleDhtProvider.PREDECESSOR_NODE_ID)) {
            // first node is joining
            String sender_port = message.getSender_port();
            String sender_node_id;
            try {
                sender_node_id = genHash(sender_port);
            } catch (NoSuchAlgorithmException err) {
                Log.e(TAG, "HANDLE_JOIN_REQUEST: error generating port hash");
                sender_node_id = null;
            }

            SimpleDhtProvider.SUCCESSOR_PORT = sender_port;
            SimpleDhtProvider.SUCCESSOR_NODE_ID = sender_node_id;
            SimpleDhtProvider.PREDECESSOR_PORT = sender_port;
            SimpleDhtProvider.PREDECESSOR_NODE_ID = sender_node_id;

            Message response_message = new Message(Message.JOIN_RESPONSE, SimpleDhtProvider.MY_PORT);
            response_message.insert_args(Message.PREDECESSOR, SimpleDhtProvider.MY_PORT);
            response_message.insert_args(Message.SUCCESSOR, SimpleDhtProvider.MY_PORT);

            send_message(response_message, sender_port);

        }

    }

    public static void handle_join_response(Message message) {

    }

    public static void handle_query(Message message) {

    }

    public static void handle_insert(Message message) {

    }

    public static void handle_delete(Message message) {

    }

    public static void route_incoming_message(SimpleDhtProvider provider, Message message) {
        provider_reference = provider;
        if (message.getCommand().equals(Message.QUERY)) handle_query(message);
        if (message.getCommand().equals(Message.INSERT)) handle_insert(message);
        if (message.getCommand().equals(Message.DELETE)) handle_delete(message);
        if (message.getCommand().equals(Message.JOIN)) handle_join_request(message);
        if (message.getCommand().equals(Message.JOIN_RESPONSE)) handle_join_response(message);
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
