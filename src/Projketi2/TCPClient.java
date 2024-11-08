package Projketi2;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient {
    private static final String SERVER_ADDRESS = "localhost"; 
    private static final int PORT = 10000; 

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {

            System.out.println("Connected to the server.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}