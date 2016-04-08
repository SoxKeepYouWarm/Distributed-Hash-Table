package edu.buffalo.cse.cse486586.simpledht;

public interface Connnection_manager {

    public Connection_state get_connection_state();

    public void notify_query_results(Message message);

}
