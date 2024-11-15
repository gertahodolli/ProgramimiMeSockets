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
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private boolean fullAccess;

        public ClientHandler(Socket socket, boolean fullAccess) {
            this.socket = socket;
            this.fullAccess = fullAccess;
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace(); // Shfaq një gabim nëse ndodh gabim gjatë lidhjes
            }
        }

        @Override
        public void run() {
            try {
                socket.setSoTimeout((int) TIMEOUT);

                while (true) {
                    String message = in.readLine();
                    if (message == null) break;
                    logRequest(message);
                    System.out.println("Received from client: " + message);


                    if (!fullAccess) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                    if (fullAccess) {
                        handleFullAccessCommand(message);
                    } else {
                        handleReadOnlyCommand(message);
                    }
                }
            }
            catch (SocketTimeoutException e) {
                System.out.println("Client timed out: " + socket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
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
            else if (command.startsWith("DELETE")) {
                String[] parts = command.split(" ");
                if (parts.length < 2) {
                    out.println("Invalid DELETE command format. Usage: DELETE <filename>");
                } else {
                    File file = new File(serverFiles, parts[1]);
                    if (file.exists() && file.delete()) {
                        out.println("File deleted successfully.");
                    } else {
                        out.println("File not found or unable to delete.");
                    }
                }
            }
            else {
                out.println("Command received: " + command);
            }
        }

        private void handleReadOnlyCommand(String command) throws IOException {
            if (command.startsWith("READ")) {
                File file = new File(serverFiles, command.split(" ")[1]);
                if (file.exists()) {
                    out.println("File contents: " + new String(Files.readAllBytes(file.toPath())));
                } else {
                    out.println("File not found.");
                }
            } else if (command.startsWith("WRITE") || command.startsWith("DELETE") || command.equals("LIST")) {
                out.println("You have read-only access. Command not allowed: " + command);
            } else {
                out.println("Message received: " + command);
            }
        }

        private void logRequest(String message) {
            String logEntry = new Date() + " - " + socket.getInetAddress() + ": " + message;
            log.add(logEntry);
            System.out.println(logEntry);
        }

        private void closeConnection() {
            try {
                clients.remove(socket);
                socket.close();
                System.out.println("Connection closed: " + socket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}