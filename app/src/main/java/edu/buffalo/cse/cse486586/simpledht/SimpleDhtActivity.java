package edu.buffalo.cse.cse486586.simpledht;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import edu.buffalo.cse.cse486586.simpledht.Test.Debug;

public class SimpleDhtActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_dht_main);
        
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.button3).setOnClickListener(
                new OnTestClickListener(tv, getContentResolver()));


        final Debug debugger = new Debug(getContentResolver(), tv);

        findViewById(R.id.local_dump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugger.debug_local_dump();
            }
        });

        findViewById(R.id.global_dump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugger.debug_global_dump();
            }
        });

        findViewById(R.id.debug_pointers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugger.debug_node_pointers();
            }
        });

        findViewById(R.id.debug_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugger.debug_insert();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_simple_dht_main, menu);
        return true;
    }

}
