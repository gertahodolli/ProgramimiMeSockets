package Projketi2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class TCPServer {

    private static final int PORT = 10000;
    private static final int MAX_CLIENTS = 4;
    private static boolean maxClientsReached = false;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);


            while (true) {

                if () {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                } else {

                    if (!maxClientsReached) {
                        System.out.println("Max clients reached. Rejecting new connections.");
                        maxClientsReached = true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

}
