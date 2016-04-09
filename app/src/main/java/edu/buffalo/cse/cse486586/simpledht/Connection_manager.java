package edu.buffalo.cse.cse486586.simpledht;

import java.util.Hashtable;

public interface Connection_manager {

    public void notify_query_results(Message message);

    public void route_message(Message message);

    public Hashtable<String, String> get_datastore();

    public void insert(String key, String value);

    public String query(String key);

    public void delete(String key);
}
