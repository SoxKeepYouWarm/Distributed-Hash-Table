package edu.buffalo.cse.cse486586.simpledht.Test;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.TextView;

import edu.buffalo.cse.cse486586.simpledht.Message;
import edu.buffalo.cse.cse486586.simpledht.SimpleDhtProvider;

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


    public void debug_node_pointers() {
        Testable node_pointer_test = new Test_query(resolver, Message.DEBUG_NODE_POINTERS);
        new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, node_pointer_test);
    }

    public void debug_local_dump() {
        Testable local_dump_test = new Test_query(resolver, Message.DEBUG_DUMP_LOCAL);
        new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, local_dump_test);
    }

    public void debug_global_dump() {
        Testable global_dump_test = new Test_query(resolver, Message.DEBUG_DUMP_GLOBAL);
        new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, global_dump_test);
    }

    public void debug_insert() {
        Testable insert_test = new Test_insert(resolver, false);
        new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, insert_test);
    }


    private class Task extends AsyncTask<Testable, String, Void> {

        @Override
        protected Void doInBackground(Testable... params) {
            Testable test = params[0];
            test.run_test();
            publishProgress(test.get_results());

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            for (String line : values) {
                output.append(line + '\n');
            }

            output.append("DONE!\n");
        }
    }


    public static Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

}
