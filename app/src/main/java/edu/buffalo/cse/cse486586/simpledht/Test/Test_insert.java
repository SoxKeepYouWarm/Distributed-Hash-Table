package edu.buffalo.cse.cse486586.simpledht.Test;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import edu.buffalo.cse.cse486586.simpledht.Message;
import edu.buffalo.cse.cse486586.simpledht.SimpleDhtProvider;

public class Test_insert implements Testable{

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    private final ContentResolver resolver;
    private final Uri uri;

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    ContentValues single_input = new ContentValues();


    String[] results;
    boolean MULTIPLE_INSERT;

    public Test_insert(ContentResolver resolver, boolean multiple_insert) {
        this.resolver = resolver;
        this.uri = Debug.buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
        this.MULTIPLE_INSERT = multiple_insert;
        initialize_sample_input();
    }

    private void initialize_sample_input() {
        if (MULTIPLE_INSERT) {

        } else {
            single_input.put(KEY_FIELD, "key0");
            single_input.put(VALUE_FIELD, "value0");
        }
    }

    @Override
    public void run_test() {
        if (MULTIPLE_INSERT) {
            test_insert_multiple();
        } else {
            test_insert_single();
        }
    }

    @Override
    public String[] get_results() {
        return results;
    }


    private void test_insert_multiple() {

    }

    private void test_insert_single() {
        resolver.insert(uri, single_input);

        Cursor resultCursor = resolver.query(uri, null, Message.QUERY, null, null);
        Log.d(TAG, "DEBUG_INSERT: just received query cursor");

        int keyIndex = resultCursor.getColumnIndex(KEY_FIELD);
        int valueIndex = resultCursor.getColumnIndex(VALUE_FIELD);

        resultCursor.moveToFirst();

        String[] output = new String[resultCursor.getCount()];
        for (int i = 0; i < resultCursor.getCount(); i++) {
            String returnKey = resultCursor.getString(keyIndex);
            String returnValue = resultCursor.getString(valueIndex);
            output[i] = "[ " + returnKey + " ] [ " + returnValue + " ]";

            Log.d(TAG, "DEBUG_INSERT: entry: " + output[i]);
            resultCursor.moveToNext();
        }

        results = output;

    }

}
