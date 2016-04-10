package edu.buffalo.cse.cse486586.simpledht;

import android.util.Log;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;


public class Provider_handlers {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    public static final int ONLY_NODE = 1;
    public static final int FIRST_NODE = 2;
    public static final int LAST_NODE = 3;
    public static final int MIDDLE_NODE = 4;

    public static int NODE_POSITION;

    //SimpleDhtProvider connection_state;
    Connection_manager connection_manager;
    Connection_state connection_state;

    public Provider_handlers(Connection_manager manager) {
        this.connection_manager = manager;
        this.connection_state = Connection_state.getConnectionState();
    }

    public void send_message(Message message, String destination_port) {
        if (destination_port.equals(connection_state.MY_PORT)) route_incoming_message(message);
        else new ClientTask(message, destination_port).start();
    }

    public void handle_join_request(Message message) {
        Log.d(TAG, "HANDLE_JOIN_REQUEST: sender: " + message.getSender_port());

        String sender_port = message.getSender_port();
        String sender_node_id;

        sender_node_id = Util.port_to_hash(sender_port);

        int relationship = Util.find_relationship(sender_node_id);

        if (relationship == Util.IS_MY_NODE
                || relationship == Util.IS_SUCCESSOR_NODE
                || relationship == Util.IS_PREDECESSOR_NODE) {
            Log.e(TAG, "HANDLE_JOIN_REQUEST: invalid case");
        }

        if (NODE_POSITION == ONLY_NODE) {
            Log.d(TAG, "HANDLE_JOIN_REQUEST: handling as only_node");
            // add pointers to sender in my node
            connection_state.SUCCESSOR_PORT = sender_port;
            connection_state.SUCCESSOR_NODE_ID = sender_node_id;
            connection_state.PREDECESSOR_PORT = sender_port;
            connection_state.PREDECESSOR_NODE_ID = sender_node_id;

            // return predecessor and successor info for sender
            Message response_message = new Message(Message.JOIN_RESPONSE, connection_state.MY_PORT);
            response_message.insert_arg(Message.PREDECESSOR, connection_state.MY_PORT);
            response_message.insert_arg(Message.SUCCESSOR, connection_state.MY_PORT);

            send_message(response_message, sender_port);

        } else if (NODE_POSITION == FIRST_NODE) {
            Log.d(TAG, "HANDLE_JOIN_REQUEST: handling as first_node");
            Message response_message;
            Message update_message;

            switch (relationship) {

                case Util.BEFORE_PREDECESSOR_NODE:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: invalid case");
                    break;
                case Util.AFTER_SUCCESSOR_NODE:
                    Log.d(TAG, "HANDLE_JOIN_REQUEST: forwarded join request to successor node");
                    send_message(message, connection_state.SUCCESSOR_PORT);
                    break;
                case Util.BETWEEN_SUCCESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, connection_state.MY_PORT);
                    response_message.insert_arg(Message.PREDECESSOR, connection_state.MY_PORT);
                    response_message.insert_arg(Message.SUCCESSOR, connection_state.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // update successor node's predecessor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, connection_state.MY_PORT);
                    update_message.insert_arg(Message.PREDECESSOR, sender_port);
                    send_message(update_message, connection_state.SUCCESSOR_PORT);

                    // update my successor value
                    connection_state.SUCCESSOR_PORT = sender_port;
                    connection_state.SUCCESSOR_NODE_ID = sender_node_id;

                    Log.d(TAG, "UPDATE: target: " + connection_state.MY_PORT +
                            " predecessor: " + sender_port);
                    Log.d(TAG, "responded to join request: between_successor_node");
                    break;
                case Util.BETWEEN_PREDECESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, connection_state.MY_PORT);
                    response_message.insert_arg(Message.PREDECESSOR, connection_state.PREDECESSOR_PORT);
                    response_message.insert_arg(Message.SUCCESSOR, connection_state.MY_PORT);

                    send_message(response_message, sender_port);

                    // update predecessors node's successor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, connection_state.MY_PORT);
                    update_message.insert_arg(Message.SUCCESSOR, sender_port);
                    send_message(update_message, connection_state.PREDECESSOR_PORT);

                    // update my predecessor value
                    connection_state.PREDECESSOR_PORT = sender_port;
                    connection_state.PREDECESSOR_NODE_ID = sender_node_id;

                    Log.d(TAG, "sent pointer update for predecessor's node pointer to successor node");
                    break;
                default:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: error as first_node");

            }

        } else if (NODE_POSITION == LAST_NODE) {
            Log.d(TAG, "HANDLE_JOIN_REQUEST: handling as last_node");
            Message response_message;
            Message update_message;

            switch (relationship) {

                case Util.AFTER_SUCCESSOR_NODE:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: invalid case");
                    break;
                case Util.BEFORE_PREDECESSOR_NODE:
                    Log.d(TAG, "HANDLE_JOIN_REQUEST: forwarding message to predecessor");
                    send_message(message, connection_state.PREDECESSOR_PORT);
                    break;
                case Util.BETWEEN_SUCCESSOR_NODE:
                    // return predecessor and successor info for sender
                    Log.d(TAG, "HANDLE_JOIN_REQUEST: between_successor_node");
                    response_message = new Message(Message.JOIN_RESPONSE, connection_state.MY_PORT);
                    response_message.insert_arg(Message.PREDECESSOR, connection_state.MY_PORT);
                    response_message.insert_arg(Message.SUCCESSOR, connection_state.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // update successor node's predecessor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, connection_state.MY_PORT);
                    update_message.insert_arg(Message.PREDECESSOR, sender_port);
                    send_message(update_message, connection_state.SUCCESSOR_PORT);

                    // update my successor value
                    connection_state.SUCCESSOR_PORT = sender_port;
                    connection_state.SUCCESSOR_NODE_ID = sender_node_id;

                    Log.d(TAG, "sent pointer update for successor's node pointer to predecessor node");
                    break;
                case Util.BETWEEN_PREDECESSOR_NODE:
                    // return predecessor and successor info for sender
                    Log.d(TAG, "HANDLE_JOIN_REQUEST: between_predecessor_node");
                    response_message = new Message(Message.JOIN_RESPONSE, connection_state.MY_PORT);
                    response_message.insert_arg(Message.PREDECESSOR, connection_state.PREDECESSOR_PORT);
                    response_message.insert_arg(Message.SUCCESSOR, connection_state.MY_PORT);

                    send_message(response_message, sender_port);

                    // update predecessor node's successor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, connection_state.MY_PORT);
                    update_message.insert_arg(Message.SUCCESSOR, sender_port);
                    send_message(update_message, connection_state.PREDECESSOR_PORT);

                    // update my successor value
                    connection_state.PREDECESSOR_PORT = sender_port;
                    connection_state.PREDECESSOR_NODE_ID = sender_node_id;

                    Log.d(TAG, "sent pointer update for predecessor's node pointer to successor node");
                    break;
                default:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: error as last_node, relationship: " + relationship);

            }

        } else if (NODE_POSITION == MIDDLE_NODE) {
            Log.d(TAG, "HANDLE_JOIN_REQUEST: handling as middle_node");
            Message response_message;
            Message update_message;

            switch (relationship) {

                case Util.AFTER_SUCCESSOR_NODE:
                    send_message(message, connection_state.SUCCESSOR_PORT);
                    Log.d(TAG, "forwarded join request to successor node");
                    break;
                case Util.BEFORE_PREDECESSOR_NODE:
                    send_message(message, connection_state.PREDECESSOR_PORT);
                    Log.d(TAG, "forwarded join request to predecessor node");
                    break;
                case Util.BETWEEN_SUCCESSOR_NODE:
                    // return predecessor and successor info for sender
                    Log.d(TAG, "HANDLE_JOIN_REQUEST: between_successor_node");
                    response_message = new Message(Message.JOIN_RESPONSE, connection_state.MY_PORT);
                    response_message.insert_arg(Message.PREDECESSOR, connection_state.MY_PORT);
                    response_message.insert_arg(Message.SUCCESSOR, connection_state.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // update successor node's predecessor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, connection_state.MY_PORT);
                    update_message.insert_arg(Message.PREDECESSOR, sender_port);
                    send_message(update_message, connection_state.SUCCESSOR_PORT);

                    // update my successor value
                    connection_state.SUCCESSOR_PORT = sender_port;
                    connection_state.SUCCESSOR_NODE_ID = sender_node_id;

                    Log.d(TAG, "sent pointer update for successor's node pointer to predecessor node");
                    break;
                case Util.BETWEEN_PREDECESSOR_NODE:
                    // return predecessor and successor info for sender
                    response_message = new Message(Message.JOIN_RESPONSE, connection_state.MY_PORT);
                    response_message.insert_arg(Message.PREDECESSOR, connection_state.MY_PORT);
                    response_message.insert_arg(Message.SUCCESSOR, connection_state.SUCCESSOR_PORT);

                    send_message(response_message, sender_port);

                    // update predecessor node's successor pointer
                    update_message = new Message(Message.UPDATE_POINTERS, connection_state.MY_PORT);
                    update_message.insert_arg(Message.SUCCESSOR, sender_port);
                    send_message(update_message, connection_state.PREDECESSOR_PORT);

                    // update my successor value
                    connection_state.PREDECESSOR_PORT = sender_port;
                    connection_state.PREDECESSOR_NODE_ID = sender_node_id;

                    Log.d(TAG, "sent pointer update for predecessor's node pointer to successor node");
                    break;
                default:
                    Log.e(TAG, "HANDLE_JOIN_REQUEST: error as middle_node");
            }

        }

        else Log.e(TAG, "HANDLE_JOIN_REQUEST: node position was not set");

    }

    public void handle_join_response(Message message) {
        Log.d(TAG, "received join response from master node");
        connection_state.PREDECESSOR_PORT = message.get_arg(Message.PREDECESSOR);
        connection_state.SUCCESSOR_PORT = message.get_arg(Message.SUCCESSOR);

        connection_state.PREDECESSOR_NODE_ID = Util.port_to_hash(connection_state.PREDECESSOR_PORT);
        connection_state.SUCCESSOR_NODE_ID = Util.port_to_hash(connection_state.SUCCESSOR_PORT);

        connection_state.CONNECTED = true;

        Log.d(TAG, "HANDLE_JOIN_RESPONSE: connected: true predecessor_port: " + connection_state.PREDECESSOR_PORT +
                " successor_port: " + connection_state.SUCCESSOR_PORT);
    }

    public void handle_query(Message message) {

        Log.d(TAG, "HANDLE_QUERY: " + message.stringify());

        String message_id;
        try {
            message_id = Util.genHash(message.get_arg(Message.QUERY_SELECTION));
        } catch (NoSuchAlgorithmException err) {
            Log.e(TAG, "HANDLE_QUERY: error generating hash");
            return;
        }

        String key = message.get_arg(Message.QUERY_SELECTION);
        String value = connection_manager.query(key);

        switch (NODE_POSITION) {
            case ONLY_NODE:

                if (value == null) {
                    message.insert_message(key, Message.QUERY_NOT_FOUND);
                    Log.d(TAG, "HANDLE_QUERY: not found: " + message.stringify());
                } else {
                    message.insert_message(key, value);
                    Log.d(TAG, "HANDLE_QUERY: found locally, key: " + key+ " msg: " + message.stringify());
                }

                message.setCommand(Message.QUERY_RESPONSE);
                send_message(message, message.getSender_port());
                break;

            case FIRST_NODE:

                if (Util.less_than(message_id, connection_state.MY_NODE_ID)
                        || Util.greater_than(message_id, connection_state.PREDECESSOR_NODE_ID)) {

                    if (value == null) {
                        message.insert_message(key, Message.QUERY_NOT_FOUND);
                        Log.d(TAG, "HANDLE_QUERY: not found: " + message.stringify());
                    } else {
                        message.insert_message(key, value);
                        Log.d(TAG, "HANDLE_QUERY: found locally, key: " + key+ " msg: " + message.stringify());
                    }

                    message.setCommand(Message.QUERY_RESPONSE);
                    send_message(message, message.getSender_port());

                } else {
                    Log.d(TAG, "HANDLE_QUERY: forwarding query");
                    send_message(message, connection_state.SUCCESSOR_PORT);
                }
                break;

            case LAST_NODE:
            case MIDDLE_NODE:

                if (Util.less_than(message_id, connection_state.MY_NODE_ID)
                        && Util.greater_than(message_id, connection_state.PREDECESSOR_NODE_ID)) {

                    if (value == null) {
                        message.insert_message(key, Message.QUERY_NOT_FOUND);
                        Log.d(TAG, "HANDLE_QUERY: not found: " + message.stringify());
                    } else {
                        message.insert_message(key, value);
                        Log.d(TAG, "HANDLE_QUERY: found locally, key: " + key+ " msg: " + message.stringify());
                    }

                    message.setCommand(Message.QUERY_RESPONSE);
                    send_message(message, message.getSender_port());

                } else {
                    Log.d(TAG, "HANDLE_QUERY: forwarding query");
                    send_message(message, connection_state.SUCCESSOR_PORT);
                }
                break;

            default:
                Log.e(TAG, "HANDLE_INSERT: error identifying node position");
                break;

        }

    }

    public void handle_query_response(Message message) {
        Log.d(TAG, "HANDLE_QUERY_RESPONSE: " + message.stringify());
        connection_manager.notify_query_results(message);
    }

    public void handle_query_all(Message message) {

        // insert all local data first
        Hashtable<String, String> data = connection_manager.get_datastore();
        for (String key : data.keySet()) {
            String value = data.get(key);
            message.insert_message(key, value);
        }

        // if next node is the original sender, send as response
        if (message.getSender_port().equals(connection_state.SUCCESSOR_PORT)) {
            message.setCommand(Message.QUERY_ALL_RESPONSE);
            send_message(message, message.getSender_port());
        } else {
            send_message(message, connection_state.SUCCESSOR_PORT);
        }

    }

    public void handle_query_all_response(Message message) {
        connection_manager.notify_query_results(message);
    }

    public void handle_query_local(Message message) {
        Hashtable<String, String> data = connection_manager.get_datastore();
        for (String key : data.keySet()) {
            String value = data.get(key);
            message.insert_message(key, value);
        }

        connection_manager.notify_query_results(message);

    }

    public void handle_insert(Message message) {

        Log.d(TAG, "HANDLE_INSERT: " + message.stringify());

        String key = message.get_arg(Message.INSERT_KEY);
        String value = message.get_arg(Message.INSERT_VALUE);

        String message_id;
        try {
            message_id = Util.genHash(key);
        } catch (NoSuchAlgorithmException err) {
            Log.e(TAG, "HANDLE_INSERT: error generating hash");
            return;
        }

        switch (NODE_POSITION) {
            case ONLY_NODE:
                Log.d(TAG, "HANDLE_INSERT: ONLY_NODE: inserting pair locally");
                connection_manager.insert(key, value);
                break;
            case FIRST_NODE:
                if (Util.less_than(message_id, connection_state.MY_NODE_ID)
                        || Util.greater_than(message_id, connection_state.PREDECESSOR_NODE_ID)) {
                    Log.d(TAG, "HANDLE_INSERT: FIRST_NODE: inserting pair locally");
                    connection_manager.insert(key, value);
                } else {
                    Log.d(TAG, "HANDLE_INSERT: FIRST_NODE: forwarding message to next node");
                    send_message(message, connection_state.SUCCESSOR_PORT);
                }
                break;
            case LAST_NODE:
            case MIDDLE_NODE:
                if (Util.less_than(message_id, connection_state.MY_NODE_ID)
                        && Util.greater_than(message_id, connection_state.PREDECESSOR_NODE_ID)) {
                    Log.d(TAG, "HANDLE_INSERT: MID/LAST NODE: inserting pair locally");
                    connection_manager.insert(key, value);
                } else {
                    Log.d(TAG, "HANDLE_INSERT: MID/LAST NODE: forwarding message to next node");
                    send_message(message, connection_state.SUCCESSOR_PORT);
                }
                break;
            default:
                Log.e(TAG, "HANDLE_INSERT: error identifying node position");
                break;

        }

    }

    public void handle_delete(Message message) {

        Log.d(TAG, "HANDLE_DELETE: " + message.stringify());

        String message_id;
        try {
            message_id = Util.genHash(message.get_arg(Message.DELETE_KEY));
        } catch (NoSuchAlgorithmException err) {
            Log.e(TAG, "HANDLE_DELETE: error generating hash");
            return;
        }

        String key = message.get_arg(Message.DELETE_KEY);

        switch (NODE_POSITION) {
            case ONLY_NODE:
                connection_manager.delete(key);
                break;
            case FIRST_NODE:
                if (Util.less_than(message_id, connection_state.MY_NODE_ID)
                        || Util.greater_than(message_id, connection_state.PREDECESSOR_NODE_ID)) {
                    Log.d(TAG, "HANDLE_DELETE: deleting locally");
                    connection_manager.delete(key);
                } else {
                    Log.d(TAG, "HANDLE_DELETE: forwarding delete");
                    send_message(message, connection_state.SUCCESSOR_PORT);
                }
                break;
            case LAST_NODE:
            case MIDDLE_NODE:
                if (Util.less_than(message_id, connection_state.MY_NODE_ID)
                        && Util.greater_than(message_id, connection_state.PREDECESSOR_NODE_ID)) {
                    Log.d(TAG, "HANDLE_DELETE: deleting locally");
                    connection_manager.delete(key);
                } else {
                    Log.d(TAG, "HANDLE_DELETE: forwarding delete");
                    send_message(message, connection_state.SUCCESSOR_PORT);
                }
                break;
            default:
                Log.e(TAG, "HANDLE_INSERT: error identifying node position");
                break;

        }

    }

    public void handle_delete_all(Message message) {

        Log.d(TAG, "HANDLE_DELETE_ALL: " + message.stringify());
        Hashtable<String, String> datastore = connection_manager.get_datastore();
        datastore.clear();

        // if next node isn't original sender
        if ( ! message.getSender_port().equals(connection_state.SUCCESSOR_PORT) ) {
            // forward message to next node
            send_message(message, connection_state.SUCCESSOR_PORT);
        }

    }

    public void handle_delete_local(Message message) {

        Log.d(TAG, "HANDLE_DELETE_ALL: " + message.stringify());
        Hashtable<String, String> datastore = connection_manager.get_datastore();
        datastore.clear();

    }

    public void handle_update_pointers(Message message) {

        Log.d(TAG, "HANDLE_UPDATE_POINTERS: " + message.stringify());

        String successor = message.get_arg(Message.SUCCESSOR);
        String predecessor = message.get_arg(Message.PREDECESSOR);

        if (successor != null) {
            connection_state.SUCCESSOR_PORT = successor;
            connection_state.SUCCESSOR_NODE_ID = Util.port_to_hash(successor);
        }

        if (predecessor != null) {
            connection_state.PREDECESSOR_PORT = predecessor;
            connection_state.PREDECESSOR_NODE_ID = Util.port_to_hash(predecessor);
        }

        Log.d(TAG, "HANDLE_UPDATE_POINTERS: succ: " + connection_state.SUCCESSOR_PORT +
                " predd: " + connection_state.PREDECESSOR_PORT);

    }

    public void handle_update_keys(Message message) {

    }

    public void handle_debug_node_pointers(Message message) {

        String my_info = "PREDECESSOR: " + connection_state.PREDECESSOR_PORT +
                " SUCCESSOR: " + connection_state.SUCCESSOR_PORT;
        message.insert_message(connection_state.MY_PORT, my_info);

        if (message.getSender_port().equals(connection_state.SUCCESSOR_PORT)) {
            // forwarding response to the original sender
            Log.d(TAG, "HANDLE_DEBUG_NODE_POINTERS: forwarding response to original sender");
            message.setCommand(Message.DEBUG_NODE_POINTERS_RESPONSE);
            message.setSender_port(connection_state.MY_PORT);
            send_message(message, connection_state.SUCCESSOR_PORT);
        } else {
            // forwarding to another node
            Log.d(TAG, "HANDLE_DEBUG_NODE_POINTERS: forwarding response to next node");
            send_message(message, connection_state.SUCCESSOR_PORT);
        }

    }

    public void handle_debug_node_pointers_response(Message message) {
        connection_manager.notify_query_results(message);
    }

    public void determine_node_position() {

        if (connection_state.MY_NODE_ID.equals(connection_state.SUCCESSOR_NODE_ID)
                && connection_state.MY_NODE_ID.equals(connection_state.PREDECESSOR_NODE_ID)) {
            NODE_POSITION = ONLY_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: ONLY_NODE");
        } else if (Util.less_than(connection_state.MY_NODE_ID, connection_state.PREDECESSOR_NODE_ID)) {
            NODE_POSITION = FIRST_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: FIRST_NODE");
        } else if (Util.greater_than(connection_state.MY_NODE_ID, connection_state.SUCCESSOR_NODE_ID)) {
            NODE_POSITION = LAST_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: LAST_NODE");
        } else {
            NODE_POSITION = MIDDLE_NODE;
            Log.d(TAG, "DETERMINE_NODE_POSITION: MIDDLE_NODE");
        }

    }

    public void route_incoming_message(Message message) {
        if (connection_state.CONNECTED) determine_node_position();

        if (message.getCommand().equals(Message.QUERY)) handle_query(message);
        else if (message.getCommand().equals(Message.QUERY_RESPONSE)) handle_query_response(message);
        else if (message.getCommand().equals(Message.QUERY_ALL)) handle_query_all(message);
        else if (message.getCommand().equals(Message.QUERY_ALL_RESPONSE)) handle_query_all_response(message);
        else if (message.getCommand().equals(Message.QUERY_LOCAL)) handle_query_local(message);

        else if (message.getCommand().equals(Message.INSERT)) handle_insert(message);

        else if (message.getCommand().equals(Message.DELETE)) handle_delete(message);
        else if (message.getCommand().equals(Message.DELETE_ALL)) handle_delete_all(message);
        else if (message.getCommand().equals(Message.DELETE_LOCAL)) handle_delete_local(message);

        else if (message.getCommand().equals(Message.JOIN)) handle_join_request(message);
        else if (message.getCommand().equals(Message.JOIN_RESPONSE)) handle_join_response(message);

        else if (message.getCommand().equals(Message.UPDATE_POINTERS)) handle_update_pointers(message);
        else if (message.getCommand().equals(Message.UPDATE_KEYS)) handle_update_keys(message);

        else if (message.getCommand().equals(Message.DEBUG_NODE_POINTERS)) handle_debug_node_pointers(message);
        else if (message.getCommand().equals(Message.DEBUG_NODE_POINTERS_RESPONSE)) handle_debug_node_pointers_response(message);

        else Log.e(TAG, "ROUTE_INCOMING_MESSAGE: message was not routed correctly");
    }

}
