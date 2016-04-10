package edu.buffalo.cse.cse486586.simpledht.Test;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import edu.buffalo.cse.cse486586.simpledht.Message;
import edu.buffalo.cse.cse486586.simpledht.SimpleDhtProvider;

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


    public Test_query(ContentResolver resolver, String command) {
        this.resolver = resolver;
        this.uri = Debug.buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
        this.COMMAND = command;
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

            Log.d(TAG, "Test: entry: " + output[i]);
            resultCursor.moveToNext();
        }

        return output;

    }

}
