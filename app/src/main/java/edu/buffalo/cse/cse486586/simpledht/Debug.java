package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentResolver;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class Debug {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    private final ContentResolver resolver;
    private final Uri uri;
    private TextView output;

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    public Debug(ContentResolver resolver, TextView textView) {
        this.resolver = resolver;
        this.uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
        this.output = textView;
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }


    public void debug_node_pointers() {
        new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class Task extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "TASK: about to call debug_node_pointers");
            String result = get_node_pointers();
            Log.d(TAG, "TASK: just called debug_node_pointers");
            publishProgress("TASK: just called debug_node_pointers");
            publishProgress(result);
            Log.d(TAG, "should have gotten progress report by now");

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d(TAG, "DEBUG: appending to textView");
            output.append(values[0]);
            output.append("DONE!");
        }
    }




    private String get_node_pointers() {
        Cursor resultCursor = resolver.query(uri, null, Message.DEBUG_NODE_POINTERS, null, null);
        Log.d(TAG, "DEBUG_NODE_POINTERS: just received query cursor");

        int keyIndex = resultCursor.getColumnIndex(KEY_FIELD);
        int valueIndex = resultCursor.getColumnIndex(VALUE_FIELD);

        resultCursor.moveToFirst();

        String output = "";
        for (int i = 0; i < resultCursor.getCount(); i++) {
            String returnKey = resultCursor.getString(keyIndex);
            String returnValue = resultCursor.getString(valueIndex);
            String msg = "[ " + returnKey + " ] [ " + returnValue + " ] ";
            output += msg;
            Log.d(TAG, "DEBUG_NODE_POINTERS: entry: " + msg);
            resultCursor.moveToNext();
        }

        Log.d(TAG, "DEBUG_NODE_POINTERS: output: " + output);
        return output;

    }

}
