package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {
    static final String TAG = SimpleDhtProvider.class.getSimpleName();
    //private ServerSocket serverSocket;
    private static int SERVER_PORT = 10000;

    public static String MASTER_NODE_PORT = "11108";

    public static String MY_PORT;
    public static String MY_NODE_ID;

    public static String PREDECESSOR_PORT;
    public static String PREDECESSOR_NODE_ID;

    public static String SUCCESSOR_PORT;
    public static String SUCCESSOR_NODE_ID;

    Hashtable<String, String> datastore = new Hashtable<String, String>();
    Hashtable<String, String> providers = new Hashtable<String, String>();

    private void set_my_port() {
        try {
            TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
            MY_PORT = String.valueOf((Integer.parseInt(portStr) * 2));
        } catch (NullPointerException err) {
            Log.e(TAG, "SET_MY_PORT: error getting telephony manager");
        }

    }

    private void set_my_node_id() {
        try{
            MY_NODE_ID=Provider_handlers.genHash(MY_PORT);
            Log.d(TAG, "node id: " + MY_NODE_ID);
        }catch(NoSuchAlgorithmException err){
            Log.e(TAG, "SET_NODE_ID: error setting node id");
        }
    }

    private void start_server_task() {
        try {

            Log.d(TAG, "initiating server task");
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            Server_param_wrapper params = new Server_param_wrapper(serverSocket, this);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
        }
    }

    @Override
    public boolean onCreate() {
        set_my_port();
        set_my_node_id();
        start_server_task();

        Log.d(TAG, "PORT: " + MY_PORT + " ID: " + MY_NODE_ID);

        // check if this device is master node
        if (MY_PORT.equals(MASTER_NODE_PORT)) {
            PREDECESSOR_PORT = MY_PORT;
            SUCCESSOR_PORT = MY_PORT;
            try {
                PREDECESSOR_NODE_ID = Provider_handlers.genHash(MY_PORT);
                SUCCESSOR_NODE_ID = Provider_handlers.genHash(MY_PORT);
            } catch (NoSuchAlgorithmException err) {
                Log.e(TAG, "error setting predecessor node id");
            }

        } else {
            // connect to master node
            Message join_request = new Message(Message.JOIN, MY_PORT);
            Provider_handlers.send_message(join_request, MASTER_NODE_PORT);

        }

        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete selection is: " + selection);

        Message delete_message = new Message(Message.DELETE, MY_PORT);
        delete_message.insert_args(Message.SELECTION, selection);

        Provider_handlers.route_incoming_message(this, delete_message);

        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert value is key: " + values.getAsString("key") +
                " value: " + values.getAsString("value"));

        Message insert_message = new Message(Message.INSERT, MY_PORT);
        insert_message.insert_args(values.getAsString("key"), values.getAsString("value"));

        Provider_handlers.route_incoming_message(this, insert_message);

        return null;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Log.d(TAG, "query selection is: " + selection);

        Message query_message = new Message(Message.QUERY, MY_PORT);
        query_message.insert_args(Message.SELECTION, selection);

        Provider_handlers.route_incoming_message(this, query_message);

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return 0;
    }

}
