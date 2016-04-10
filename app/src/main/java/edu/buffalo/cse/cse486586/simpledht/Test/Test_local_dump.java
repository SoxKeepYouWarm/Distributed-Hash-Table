package edu.buffalo.cse.cse486586.simpledht.Test;

import android.content.ContentResolver;
import android.net.Uri;


import edu.buffalo.cse.cse486586.simpledht.SimpleDhtProvider;

public class Test_local_dump implements Testable{

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    private final ContentResolver resolver;
    private final Uri uri;

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    String[] results;

    public Test_local_dump(ContentResolver resolver) {
        this.resolver = resolver;
        this.uri = Debug.buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
    }

    @Override
    public void run_test() {

    }

    @Override
    public String[] get_results() {
        return results;
    }




}
