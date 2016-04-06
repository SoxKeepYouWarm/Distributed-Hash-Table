package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

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
            Log.d(TAG, "CLIENT_TASK: sent " + message.stringify());

            client_socket.setSoTimeout(100);
            BufferedReader in = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            String msg;
            if ((msg = in.readLine()) != null) {
                if (msg.equals("ACK")) {
                    Log.d(TAG, "CLIENT_TASK: received ACK");
                } else {
                    Log.e(TAG, "CLIENT_TASK: received " + msg + " from server");
                }

            } else {
                Log.e(TAG, "CLIENT_TASK: didn't receive ACK");
            }

            in.close();
            out.close();
            client_socket.close();

        } catch (SocketTimeoutException e) {
            Log.e(TAG, "timeout " + e.getMessage());
        } catch (SocketException e) {
            Log.e(TAG, "Socket exception (timeout): " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "io exception connecting client socket: " + e.getMessage());
        }

        return null;

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
