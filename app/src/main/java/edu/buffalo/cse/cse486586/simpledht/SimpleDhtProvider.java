package edu.buffalo.cse.cse486586.simpledht;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider implements Connection_manager {
    static final String TAG = SimpleDhtProvider.class.getSimpleName();
    //private ServerSocket serverSocket;
    public static int SERVER_PORT = 10000;

    public static String MASTER_NODE_PORT = "11108";

    Connection_state connection_state;

    Hashtable<String, String> datastore = new Hashtable<String, String>();

    public Provider_handlers handlers;

    @Override
    public void notify_query_results(Message message) {
        this.query_result = message;
        this.release_query_latch();
    }

    @Override
    public void route_message(Message message) {
        handlers.route_incoming_message(message);
    }

    @Override
    public Hashtable<String, String> get_datastore() {
        return datastore;
    }

    @Override
    public void insert(String key, String value) {
        datastore.put(key, value);
    }

    @Override
    public String query(String key) {
        return datastore.get(key);
    }

    @Override
    public void delete(String key) {
        datastore.remove(key);
    }

    private void set_my_port() {
        try {
            TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
            connection_state.MY_PORT = String.valueOf((Integer.parseInt(portStr) * 2));
        } catch (NullPointerException err) {
            Log.e(TAG, "SET_MY_PORT: error getting telephony manager");
        }

    }

    private void set_my_node_id() {
        connection_state.MY_NODE_ID=Util.port_to_hash(connection_state.MY_PORT);
        Log.d(TAG, "node id: " + connection_state.MY_NODE_ID);
    }

    private void start_server_task() {
        new ServerTask(this).start();
    }

    @Override
    public boolean onCreate() {

        connection_state = Connection_state.getConnectionState();

        set_my_port();
        set_my_node_id();
        start_server_task();

        this.handlers = new Provider_handlers(this);

        Log.d(TAG, "PORT: " + connection_state.MY_PORT + " ID: " + connection_state.MY_NODE_ID);

        connection_state.PREDECESSOR_PORT = connection_state.MY_PORT;
        connection_state.SUCCESSOR_PORT = connection_state.MY_PORT;
        connection_state.CONNECTED = true;

        connection_state.PREDECESSOR_NODE_ID = Util.port_to_hash(connection_state.MY_PORT);
        connection_state.SUCCESSOR_NODE_ID = Util.port_to_hash(connection_state.MY_PORT);

        // check if this device is master node
        if ( ! connection_state.MY_PORT.equals(MASTER_NODE_PORT) ) {
            // connect to master node
            Message join_request = new Message(Message.JOIN, connection_state.MY_PORT);
            handlers.send_message(join_request, MASTER_NODE_PORT);
        }

        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "DELETE: selection: " + selection);

        String command;

        if (selection.equals("*")) command = Message.DELETE_ALL;
        else if (selection.equals("@")) command = Message.DELETE_LOCAL;
        else command = Message.DELETE;

        Message delete_message = new Message(command, connection_state.MY_PORT);
        delete_message.insert_arg(Message.DELETE_KEY, selection);

        handlers.route_incoming_message(delete_message);

        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "INSERT: key: " + values.getAsString(Message.KEY) +
                " value: " + values.getAsString(Message.VALUE));

        Message insert_message = new Message(Message.INSERT, connection_state.MY_PORT);
        insert_message.insert_arg(Message.INSERT_KEY, values.getAsString(Message.KEY));
        insert_message.insert_arg(Message.INSERT_VALUE, values.getAsString(Message.VALUE));

        handlers.route_incoming_message(insert_message);

        return uri;
    }



    private CountDownLatch query_latch;
    private Message query_result;
    private void release_query_latch() {
        query_latch.countDown();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Log.d(TAG, "QUERY: selection is: " + selection);

        query_latch = new CountDownLatch(1);

        String command;

        if (selection.equals("*")) command = Message.QUERY_ALL;
        else if (selection.equals("@")) command = Message.QUERY_LOCAL;
        else if (selection.equals(Message.DEBUG_NODE_POINTERS)) command = Message.DEBUG_NODE_POINTERS;
        else command = Message.QUERY;

        Message query_message = new Message(command, connection_state.MY_PORT);
        if (command.equals(Message.QUERY)) query_message.insert_arg(Message.QUERY_SELECTION, selection);

        handlers.route_incoming_message(query_message);

        try {
            Log.d(TAG, "QUERY: waiting for query result");
            query_latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException err) {
            Log.e(TAG, "QUERY: interrupted exception");
            String[] columns= {"key", "value"};
            return new MatrixCursor(columns);
        }

        Log.d(TAG, "QUERY: latch has been raised");

        if (query_result == null) {
            Log.e(TAG, "QUERY: result object is null :(");
            String[] columns= {"key", "value"};
            return new MatrixCursor(columns);
        }

        Log.d(TAG, "QUERY: result_message: " + query_result.stringify());

        String[] columns= {"key", "value"};
        MatrixCursor result= new MatrixCursor(columns);

        String[] sorted_keys = query_result
                .get_messages().keySet()
                .toArray(new String[query_result.get_messages().keySet().size()]);

        Arrays.sort(sorted_keys);

        for (String key: sorted_keys) {
            String value = query_result.get_message(key);
            if (value.equals(Message.QUERY_NOT_FOUND)) {
                Log.e(TAG, "QUERY: " + key + " was not found");
                value = "not found";
            }

            String[] entry = { key, value };
            result.addRow(entry);
        }

        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return 0;
    }

}
