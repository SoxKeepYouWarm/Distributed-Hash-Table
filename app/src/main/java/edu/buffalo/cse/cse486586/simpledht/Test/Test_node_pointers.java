package edu.buffalo.cse.cse486586.simpledht.Test;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import edu.buffalo.cse.cse486586.simpledht.Debug;
import edu.buffalo.cse.cse486586.simpledht.Message;
import edu.buffalo.cse.cse486586.simpledht.SimpleDhtProvider;

public class Test_node_pointers implements Testable{

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    private final ContentResolver resolver;
    private final Uri uri;

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    String[] saved_results;

    @Override
    public void run_test() {
        this.saved_results = get_node_pointers();
    }

    @Override
    public String[] get_results() {
        return saved_results;
    }

    public Test_node_pointers(ContentResolver resolver) {
        this.resolver = resolver;
        this.uri = Debug.buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
    }

    private String[] get_node_pointers() {
        Cursor resultCursor = resolver.query(uri, null, Message.DEBUG_NODE_POINTERS, null, null);
        Log.d(TAG, "DEBUG_NODE_POINTERS: just received query cursor");

        int keyIndex = resultCursor.getColumnIndex(KEY_FIELD);
        int valueIndex = resultCursor.getColumnIndex(VALUE_FIELD);

        resultCursor.moveToFirst();

        String[] output = new String[resultCursor.getCount()];
        for (int i = 0; i < resultCursor.getCount(); i++) {
            String returnKey = resultCursor.getString(keyIndex);
            String returnValue = resultCursor.getString(valueIndex);
            output[i] = "[ " + returnKey + " ] [ " + returnValue + " ]";

            Log.d(TAG, "DEBUG_NODE_POINTERS: entry: " + output[i]);
            resultCursor.moveToNext();
        }

        Log.d(TAG, "DEBUG_NODE_POINTERS: output: " + output);
        return output;

    }

}
