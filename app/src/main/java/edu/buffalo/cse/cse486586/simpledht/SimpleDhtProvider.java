package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Hashtable;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

public class SimpleDhtProvider extends ContentProvider {
    static final String TAG = SimpleDhtProvider.class.getSimpleName();
    private ServerSocket serverSocket;
    private static int SERVER_PORT = 10000;

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
            MY_NODE_ID=genHash(MY_PORT);
            Log.d(TAG, "node id: "+ MY_NODE_ID);
        }catch(NoSuchAlgorithmException err){
            Log.e(TAG, "SET_NODE_ID: error setting node id");
        }
    }

    private void start_server_task() {
        try {

            Log.d(TAG, "initiating server task");
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
        }
    }

    @Override
    public boolean onCreate() {
        set_my_port();
        set_my_node_id();
        start_server_task();

        // check if this device is master node
        if (MY_PORT.equals("5554")) {
            PREDECESSOR_PORT = MY_PORT;
            SUCCESSOR_PORT = MY_PORT;
            try {
                PREDECESSOR_NODE_ID = genHash(MY_PORT);
                SUCCESSOR_NODE_ID = genHash(MY_PORT);
            } catch (NoSuchAlgorithmException err) {
                Log.e(TAG, "error setting predecessor node id");
            }

        } else {
            // connect to master node


        }

        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Message delete_message = new Message(Message.DELETE, MY_PORT);
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Message insert_message = new Message(Message.INSERT, MY_PORT);
        insert_message.insert_key_val(values.getAsString("key"), values.getAsString("value"));
        Provider_handlers.handle_insert(insert_message);

        return null;
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Message delete_message = new Message(Message.QUERY, MY_PORT);
        Log.d(TAG, "query selection is: " + selection);
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            serverSocket = sockets[0];

            // start main server loop
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    //Log.d(TAG, "server accepted client");

                    String message;
                    while ((message = in.readLine()) != null) {

                        Message incoming_message = new Message(message);
                        Provider_handlers.route_incoming_message(incoming_message);

                        publishProgress(incoming_message.stringify());
                    }
                }
            } catch (NullPointerException err) {
                Log.e(TAG, "client socket was not initialized properly");
            } catch (IOException err) {
                Log.e(TAG, "client socket was not initialized properly");
            }


            return null;
        }

        protected void onProgressUpdate(String...strings) {
            String msg = strings[0];
            Log.d(TAG, "RECEIVED MESSAGE: " + msg);
            //final TextView tv  = (TextView) findViewById(R.id.textView1);
            //tv.append(strings[0] + '\n');
        }
    }

}
