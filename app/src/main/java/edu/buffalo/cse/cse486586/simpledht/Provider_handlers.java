package edu.buffalo.cse.cse486586.simpledht;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.concurrent.CountDownLatch;

public class Provider_handlers {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    public static int ONLY_NODE = 1;
    public static int FIRST_NODE = 2;
    public static int LAST_NODE = 3;
    public static int MIDDLE_NODE = 4;

    public static int NODE_POSITION;

    SimpleDhtProvider provider_reference;
    Util util;

    public Provider_handlers(SimpleDhtProvider provider) {
        this.provider_reference = provider;
        this.util = provider.util;
    }

    public void send_message(Message message, String destination_port) {
        Client_param_wrapper params = new Client_param_wrapper(message, destination_port);
        Log.d(TAG, "SEND_MESSAGE: launching client task");
        new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public void handle_join_request(Message message) {
        if ( ! provider_reference.MY_PORT.equals(SimpleDhtProvider.MASTER_NODE_PORT) ) {
            Log.e(TAG, "non master node received join request from node: " + message.getSender_port());
        }
        Log.d(TAG, "received join request from node: " + message.getSender_port());

        String sender_port = message.getSender_port();
        String sender_node_id;

        try {
            sender_node_id = Util.genHash(sender_port);
        } catch (NoSuchAlgorithmException err) {
            Log.e(TAG, "HANDLE_JOIN_REQUEST: error generating port hash");
            sender_node_id = "";
        }

        if (NODE_POSITION == ONLY_NODE) {

            // add pointers to sender in my node
            provider_reference.SUCCESSOR_PORT = sender_port;
            provider_reference.SUCCESSOR_NODE_ID = sender_node_id;
            provider_reference.PREDECESSOR_PORT = sender_port;
            provider_reference.PREDECESSOR_NODE_ID = sender_node_id;

            // return predecessor and successor info for sender
            Message response_message = new Message(Message.JOIN_RESPONSE, provider_reference.MY_PORT);
            response_message.insert_args(Message.PREDECESSOR, provider_reference.MY_PORT);
            response_message.insert_args(Message.SUCCESSOR, provider_reference.MY_PORT);

            send_message(response_message, sender_port);

        } else if (NODE_POSITION == FIRST_NODE) {

            Message response_message;
            Message update_message;

            switch (util.find_relationship(sender_node_id)) {

                case Util.IS_MY_NODE:
                case Util.IS_SUCCESSOR_NODE:
                case Util.IS_PREDECESSOR_NODE:
                case Util.BEFORE_PREDECESSOR_NODE:
                    Log.e(TAG, "received join request with invalid case");
                    break;
                case Util.AFTER_SUCCESSOR_NODE:
                    send_message(message, provider_reference.SUCCESSOR_PORT);
                    Log.d(TAG, "forwarded join request to successor node");
                    break;
                case Util.BETWEEN_SUCCESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, provider_reference.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, provider_reference.MY_PORT);
                    response_message.insert_args(Message.SUCCESSOR, provider_reference.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // update my successor value
                    provider_reference.SUCCESSOR_PORT = sender_port;
                    provider_reference.SUCCESSOR_NODE_ID = sender_node_id;

                    // update successor node's predecessor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, provider_reference.MY_PORT);
                    update_message.insert_args(Message.PREDECESSOR, sender_port);
                    send_message(update_message, provider_reference.SUCCESSOR_PORT);

                    Log.d(TAG, "sent pointer update for successor's node pointer to predecessor node");
                    Log.d(TAG, "responded to join request: between_successor_node");
                    break;
                case Util.BETWEEN_PREDECESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, provider_reference.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, provider_reference.PREDECESSOR_PORT);
                    response_message.insert_args(Message.SUCCESSOR, provider_reference.MY_PORT);

                    send_message(response_message, sender_port);

                    // update my predecessor value
                    provider_reference.PREDECESSOR_PORT = sender_port;
                    provider_reference.PREDECESSOR_NODE_ID = sender_node_id;

                    // update predecessors node's successor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, provider_reference.MY_PORT);
                    update_message.insert_args(Message.SUCCESSOR, sender_port);
                    send_message(update_message, provider_reference.PREDECESSOR_PORT);

                    Log.d(TAG, "sent pointer update for predecessor's node pointer to successor node");
                    Log.d(TAG, "responded to join request: between_predecessor_node");
                    break;
                default:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: error as first_node");

            }

        } else if (NODE_POSITION == LAST_NODE) {

            int relationship = util.find_relationship(sender_node_id);
            Message response_message;
            Message update_message;

            switch (relationship) {

                case Util.IS_MY_NODE:
                case Util.IS_SUCCESSOR_NODE:
                case Util.IS_PREDECESSOR_NODE:
                case Util.AFTER_SUCCESSOR_NODE:
                    Log.e(TAG, "received join request with invalid case");
                    break;
                case Util.BEFORE_PREDECESSOR_NODE:
                    send_message(message, provider_reference.PREDECESSOR_PORT);
                    Log.d(TAG, "forwarded join request to predecessor node");
                    break;
                case Util.BETWEEN_SUCCESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, provider_reference.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, provider_reference.MY_PORT);
                    response_message.insert_args(Message.SUCCESSOR, provider_reference.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // update my successor value
                    provider_reference.SUCCESSOR_PORT = sender_port;
                    provider_reference.SUCCESSOR_NODE_ID = sender_node_id;

                    // update successor node's predecessor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, provider_reference.MY_PORT);
                    update_message.insert_args(Message.PREDECESSOR, sender_port);
                    send_message(update_message, provider_reference.SUCCESSOR_PORT);

                    Log.d(TAG, "sent pointer update for successor's node pointer to predecessor node");
                    Log.d(TAG, "responded to join request: between_successor_node");
                    break;
                case Util.BETWEEN_PREDECESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, provider_reference.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, provider_reference.PREDECESSOR_PORT);
                    response_message.insert_args(Message.SUCCESSOR, provider_reference.MY_PORT);

                    send_message(response_message, sender_port);

                    // update my successor value
                    provider_reference.PREDECESSOR_PORT = sender_port;
                    provider_reference.PREDECESSOR_NODE_ID = sender_node_id;

                    // update predecessor node's successor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, provider_reference.MY_PORT);
                    update_message.insert_args(Message.SUCCESSOR, sender_port);
                    send_message(update_message, provider_reference.PREDECESSOR_PORT);

                    Log.d(TAG, "sent pointer update for predecessor's node pointer to successor node");
                    Log.d(TAG, "responded to join request: between_predecessor_node");
                    break;
                default:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: error as last_node, relationship: " + relationship);

            }

        } else if (NODE_POSITION == MIDDLE_NODE) {

            Message response_message;
            Message update_message;

            switch (util.find_relationship(sender_node_id)) {

                case Util.IS_MY_NODE:
                case Util.IS_SUCCESSOR_NODE:
                case Util.IS_PREDECESSOR_NODE:
                case Util.AFTER_SUCCESSOR_NODE:
                    send_message(message, provider_reference.SUCCESSOR_PORT);
                    Log.d(TAG, "forwarded join request to successor node");
                    break;
                case Util.BEFORE_PREDECESSOR_NODE:
                    send_message(message, provider_reference.PREDECESSOR_PORT);
                    Log.d(TAG, "forwarded join request to predecessor node");
                    break;
                case Util.BETWEEN_SUCCESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, provider_reference.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, provider_reference.MY_PORT);
                    response_message.insert_args(Message.SUCCESSOR, provider_reference.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // update my successor value
                    provider_reference.SUCCESSOR_PORT = sender_port;
                    provider_reference.SUCCESSOR_NODE_ID = sender_node_id;

                    // update successor node's predecessor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, provider_reference.MY_PORT);
                    update_message.insert_args(Message.PREDECESSOR, sender_port);
                    send_message(update_message, provider_reference.SUCCESSOR_PORT);

                    Log.d(TAG, "sent pointer update for successor's node pointer to predecessor node");
                    Log.d(TAG, "responded to join request: between_successor_node");
                    break;
                case Util.BETWEEN_PREDECESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, provider_reference.MY_PORT);
                    response_message.insert_args(Message.PREDECESSOR, provider_reference.MY_PORT);
                    response_message.insert_args(Message.SUCCESSOR, provider_reference.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // update my successor value
                    provider_reference.PREDECESSOR_PORT = sender_port;
                    provider_reference.PREDECESSOR_NODE_ID = sender_node_id;

                    // update predecessor node's successor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, provider_reference.MY_PORT);
                    update_message.insert_args(Message.SUCCESSOR, sender_port);
                    send_message(update_message, provider_reference.PREDECESSOR_PORT);

                    Log.d(TAG, "sent pointer update for predecessor's node pointer to successor node");
                    Log.d(TAG, "responded to join request: between_predecessor_node");
                    break;
                default:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: error as middle_node");
            }

        }

        else Log.e(TAG, "HANDLE_JOIN_REQUEST: node position was not set");

    }

    public void handle_join_response(Message message) {
        Log.d(TAG, "received join response from master node");
        provider_reference.PREDECESSOR_PORT = message.get_arg(Message.PREDECESSOR);
        provider_reference.SUCCESSOR_PORT = message.get_arg(Message.SUCCESSOR);

        try {
            provider_reference.PREDECESSOR_NODE_ID = Util.genHash(provider_reference.PREDECESSOR_PORT);
            provider_reference.SUCCESSOR_NODE_ID = Util.genHash(provider_reference.SUCCESSOR_PORT);
        } catch (NoSuchAlgorithmException err) {
            Log.e(TAG, "HANDLE_JOIN_RESPONSE: error generating hash");
        }

        provider_reference.CONNECTED = true;

        Log.d(TAG, "HANDLE_JOIN_RESPONSE: connected: true predecessor_port: " + provider_reference.PREDECESSOR_PORT +
                " successor_port: " + provider_reference.SUCCESSOR_PORT);
    }

    public void handle_query(Message message) {

    }

    public void handle_query_response(Message message) {

    }

    public void handle_query_all(Message message) {

    }

    public void handle_query_all_response(Message message) {

    }

    public void handle_query_local(Message message) {

    }

    public void handle_query_local_response(Message message) {

    }

    public void handle_insert(Message message) {

    }

    public void handle_delete(Message message) {

    }

    public void handle_update_pointers(Message message) {

    }

    public void handle_update_keys(Message message) {

    }

    public void handle_debug_node_pointers(Message message) {

        String my_info = "PREDECESSOR: " + provider_reference.PREDECESSOR_PORT +
                " SUCCESSOR: " + provider_reference.SUCCESSOR_PORT;
        message.insert_args(provider_reference.MY_PORT, my_info);

        if (message.getSender_port().equals(provider_reference.SUCCESSOR_PORT)) {
            // forwarding response to the original sender
            Log.d(TAG, "HANDLE_DEBUG_NODE_POINTERS: forwarding response to original sender");
            message.setCommand(Message.DEBUG_NODE_POINTERS_RESPONSE);
            message.setSender_port(provider_reference.MY_PORT);
            send_message(message, provider_reference.SUCCESSOR_PORT);
        } else {
            // forwarding to another node
            Log.d(TAG, "HANDLE_DEBUG_NODE_POINTERS: forwarding response to next node");
            send_message(message, provider_reference.SUCCESSOR_PORT);
        }

    }

    public void handle_debug_node_pointers_response(Message message) {
        provider_reference.query_result = message;
        provider_reference.release_query_latch();
    }

    public void determine_node_position() {

        if (provider_reference.MY_NODE_ID.equals(provider_reference.SUCCESSOR_NODE_ID) &&
                provider_reference.MY_NODE_ID.equals(provider_reference.PREDECESSOR_NODE_ID)) {
            NODE_POSITION = ONLY_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: this node is only node");
        } else if (Util.less_than(provider_reference.MY_NODE_ID, provider_reference.PREDECESSOR_NODE_ID)) {
            NODE_POSITION = FIRST_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: this node is first node");
        } else if (Util.greater_than(provider_reference.MY_NODE_ID, provider_reference.SUCCESSOR_NODE_ID)) {
            NODE_POSITION = LAST_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: this node is last node");
        } else {
            NODE_POSITION = MIDDLE_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: this node is middle node");
        }

    }

    public void route_incoming_message(Message message) {
        if (provider_reference.CONNECTED) determine_node_position();

        if (message.getCommand().equals(Message.QUERY)) handle_query(message);
        else if (message.getCommand().equals(Message.QUERY_RESPONSE)) handle_query_response(message);
        else if (message.getCommand().equals(Message.QUERY_ALL)) handle_query_all(message);
        else if (message.getCommand().equals(Message.QUERY_ALL_RESPONSE)) handle_query_all_response(message);
        else if (message.getCommand().equals(Message.QUERY_LOCAL)) handle_query_local(message);
        else if (message.getCommand().equals(Message.QUERY_LOCAL_RESPONSE)) handle_query_local_response(message);

        else if (message.getCommand().equals(Message.INSERT)) handle_insert(message);
        else if (message.getCommand().equals(Message.DELETE)) handle_delete(message);

        else if (message.getCommand().equals(Message.JOIN)) handle_join_request(message);
        else if (message.getCommand().equals(Message.JOIN_RESPONSE)) handle_join_response(message);

        else if (message.getCommand().equals(Message.UPDATE_POINTERS)) handle_update_pointers(message);
        else if (message.getCommand().equals(Message.UPDATE_KEYS)) handle_update_keys(message);

        else if (message.getCommand().equals(Message.DEBUG_NODE_POINTERS)) handle_debug_node_pointers(message);
        else if (message.getCommand().equals(Message.DEBUG_NODE_POINTERS_RESPONSE)) handle_debug_node_pointers_response(message);

        else Log.e(TAG, "ROUTE_INCOMING_MESSAGE: message was not routed correctly");
    }






}
