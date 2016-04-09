package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerTask extends Thread {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();


    Connection_manager connection_manager;

    public ServerTask(Connection_manager connection_manager) {
        this.connection_manager = connection_manager;
    }

    @Override
    public void run() {


        // start main server loop
        try {
            ServerSocket serverSocket = new ServerSocket(SimpleDhtProvider.SERVER_PORT);
            serverSocket.setReuseAddress(true);

            while (true) {
                Log.d(TAG, "SERVER_TASK: awaiting connection");
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                Log.d(TAG, "SERVER_TASK: accepted client, receiving message");

                String message;
                while ((message = in.readLine()) != null) {
                    Log.d(TAG, "SERVER_TASK: received message: " + message);
                    Message incoming_message = new Message(message);

                    out.println("ACK");
                    Log.d(TAG, "SERVER_TASK: ACK'ed back to client");

                    connection_manager.route_message(incoming_message);

                }

            }
        } catch (NullPointerException err) {
            Log.e(TAG, "SERVER_TASK: null pointer exception");
        } catch (IOException err) {
            Log.e(TAG, err.getMessage());
        }


    }
}