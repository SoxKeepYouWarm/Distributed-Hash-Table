package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                Log.d(TAG, "server accepted client");

                String message;
                while ((message = in.readLine()) != null) {

                    Message incoming_message = new Message(message);
                    Provider_handlers.route_incoming_message(provider_reference, incoming_message);

                    publishProgress(incoming_message.stringify());
                }
            }
        } catch (NullPointerException err) {
            Log.e(TAG, "client socket was not initialized properly");
        } catch (IOException err) {
            Log.e(TAG, "client socket was not initialized properly");
        }


        return null;
    }

    protected void onProgressUpdate(String...strings) {
        String msg = strings[0];
        Log.d(TAG, "RECEIVED MESSAGE: " + msg);
    }
}