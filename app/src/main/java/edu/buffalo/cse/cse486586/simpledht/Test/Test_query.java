package edu.buffalo.cse.cse486586.simpledht.Test;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.security.NoSuchAlgorithmException;

import edu.buffalo.cse.cse486586.simpledht.Connection_state;
import edu.buffalo.cse.cse486586.simpledht.Message;
import edu.buffalo.cse.cse486586.simpledht.SimpleDhtProvider;
import edu.buffalo.cse.cse486586.simpledht.Util;

public class Test_query implements Testable{

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    private final ContentResolver resolver;
    private final Uri uri;

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    public static final String TEST_NODE_POINTERS = Message.DEBUG_NODE_POINTERS;

    private String COMMAND;

    String[] saved_results;

    @Override
    public void run_test() {
        this.saved_results = test();
    }

    @Override
    public String[] get_results() {
        return saved_results;
    }


    private Connection_state connection_state;

    public Test_query(ContentResolver resolver, String command) {
        this.resolver = resolver;
        this.uri = Debug.buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
        this.COMMAND = command;
        connection_state = Connection_state.getConnectionState();
    }

    private String[] test() {
        Cursor resultCursor = resolver.query(uri, null, COMMAND, null, null);
        Log.d(TAG, "DEBUG_NODE_POINTERS: just received query cursor");

        int keyIndex = resultCursor.getColumnIndex(KEY_FIELD);
        int valueIndex = resultCursor.getColumnIndex(VALUE_FIELD);

        resultCursor.moveToFirst();

        String[] output = new String[resultCursor.getCount()];
        for (int i = 0; i < resultCursor.getCount(); i++) {
            String returnKey = resultCursor.getString(keyIndex);
            String returnValue = resultCursor.getString(valueIndex);
            output[i] = "[ " + returnKey + " ] [ " + returnValue + " ]";

            String key_hash = "UNSET";
            try {
                key_hash = Util.genHash(returnKey);
            } catch (NoSuchAlgorithmException err) {
                Log.e(TAG, "DEBUG: gen hash error");
            }

            Log.d(TAG, "Test: entry: " + output[i] + " hash: " + key_hash);
            resultCursor.moveToNext();
        }

        Log.d(TAG, "TEST: my_id: " + connection_state.MY_NODE_ID
                + " succ_id: " + connection_state.SUCCESSOR_NODE_ID
                + " predd_id: " + connection_state.PREDECESSOR_NODE_ID);

        return output;

    }

}
