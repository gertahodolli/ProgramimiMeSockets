package Projketi2;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.out;

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
            out.println("Server listening on port " + PORT);


            while (true) {

                if (clients.size() < MAX_CLIENTS) {
                    Socket clientSocket = serverSocket.accept(); // Prano lidhjen e re të klientit
                    out.println("Client connected: " + clientSocket.getInetAddress());

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
                        out.println("Max clients reached. Rejecting new connections.");
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

        private void handleFullAccessCommand(String command) throws IOException{
            if (command.startsWith("READ")) {
                File file = new File(serverFiles, command.split(" ")[1]);
                if (file.exists()) {
                    out.println("File contents: " + new String(Files.readAllBytes(file.toPath())));
                } else {
                    out.println("File not found.");
                }
            }
            else if (command.startsWith("WRITE")) {
                String[] parts = command.split(" ", 3);
                if (parts.length < 3) {
                    out.println("Invalid WRITE command format. Usage: WRITE <filename> <text>");
                } else {
                    File file = new File(serverFiles, parts[1]);
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                        writer.write(parts[2] + "\n");
                        out.println("Write successful.");
                    } catch (IOException e) {
                        out.println("Error writing to file.");
                    }
                }
            }
            else if (command.equals("LIST")) {
                String[] files = serverFiles.list();
                if (files != null && files.length > 0) {
                    out.println("Files available: " + String.join(", ", files));
                } else {
                    out.println("No files available in the directory.");
                }
            }

        }
    }
}