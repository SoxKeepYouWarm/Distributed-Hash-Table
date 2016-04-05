package edu.buffalo.cse.cse486586.simpledht;

import java.net.ServerSocket;

public class Server_param_wrapper {
    ServerSocket serverSocket;
    SimpleDhtProvider simpleDhtProvider;
    public Server_param_wrapper(ServerSocket serverSocket, SimpleDhtProvider simpleDhtProvider) {
        this.serverSocket = serverSocket;
        this.simpleDhtProvider = simpleDhtProvider;
    }
}
