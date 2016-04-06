package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTask extends AsyncTask<Server_param_wrapper, String, Void> {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    @Override
    protected Void doInBackground(Server_param_wrapper... params) {
        ServerSocket serverSocket = params[0].serverSocket;
        SimpleDhtProvider provider_reference = params[0].simpleDhtProvider;

        // start main server loop
        try {
            while (true) {
                Log.d(TAG, "SERVER_TASK: awaiting connection");
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                Log.d(TAG, "SERVER_TASK: accepted client, receiving message");

                String message;
                while ((message = in.readLine()) != null) {
                    Log.d(TAG, "inside server while read loop");
                    //publishProgress(message);

                    Message incoming_message = new Message(message);
                    Log.d(TAG, "SERVER_TASK: received message: " + incoming_message.stringify());

                    out.println("ACK");
                    Log.d(TAG, "ACK'ed back to client");

                    Provider_handlers.route_incoming_message(incoming_message);

                }

                //clientSocket.close();
                Log.d(TAG, "SERVER_TASK: finished read while loop and closed client socket");
            }
        } catch (NullPointerException err) {
            Log.e(TAG, err.getMessage());
        } catch (IOException err) {
            Log.e(TAG, err.getMessage());
        }

        return null;
    }

    protected void onProgressUpdate(String...strings) {
        String msg = strings[0];
        Message incoming_message = new Message(msg);
        Log.d(TAG, "SERVER_TASK: received message: " + incoming_message.stringify());
        Provider_handlers.route_incoming_message(incoming_message);
    }
}