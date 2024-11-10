package Projketi2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TCPServer {

    private static final int PORT = 10000;
    private static final int MAX_CLIENTS = 4;
    private static final long TIMEOUT = 10*60*1000;
    private static Map<Socket, ClientHandler> clients = new ConcurrentHashMap<>();
    private static List<String> log = new ArrayList<>();
    private static File serverFiles = new File("server_files");
    private static boolean maxClientsReached = false;

    public static void main(String[] args) {

        if (!serverFiles.exists()) {
            serverFiles.mkdir();
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);


            while (true) {

                if (clients.size() < MAX_CLIENTS) {
                    Socket clientSocket = serverSocket.accept(); // Prano lidhjen e re të klientit
                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                    // Përcaktoni nëse ky është klienti i parë (Plotëson kërkesën 6)
                    boolean fullAccess = clients.size() == 0;

                    // Krijoni dhe shtoni klientin në map
                    ClientHandler clientHandler = new ClientHandler(clientSocket, fullAccess);
                    clients.put(clientSocket, clientHandler);

                    // Krijo një thread të ri për klientin për menaxhim të pavarur
                    new Thread(clientHandler).start();
                } else {
                    // Shfaq një mesazh kur numri maksimal i klientëve është arritur
                    if (!maxClientsReached) {
                        System.out.println("Max clients reached. Rejecting new connections.");
                        maxClientsReached = true; // Vendos flagun për të treguar që max klientët janë arritur
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

static class ClientHandler implements Runnable {
        public ClientHandler(Socket clientSocket, boolean fullAccess){}

    @Override
    public void run() {

    }
}}