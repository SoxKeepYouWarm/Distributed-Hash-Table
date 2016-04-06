package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class ClientTask extends AsyncTask<Client_param_wrapper, Void, Void> {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    @Override
    protected Void doInBackground(Client_param_wrapper... params) {

        Message message = params[0].message;
        String destination_port = params[0].port;

        Socket client_socket;
        try {
            client_socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(destination_port));

            PrintWriter out = new PrintWriter(client_socket.getOutputStream(), true);
            out.println(message.stringify());

            out.flush();
            out.close();
            client_socket.close();
        } catch (SocketException e) {
            Log.e(TAG, "Socket exception (timeout): " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "io exception connecting client socket: " + e.getMessage());
        }

        Log.d(TAG, "CLIENT_TASK: sent " + message.stringify());
        return null;

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
