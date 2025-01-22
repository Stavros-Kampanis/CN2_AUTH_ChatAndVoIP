package com.cn2.communication;
import java.io.*;
import java.net.*;

public class Client {
    private final String serverAddress;
    private final int serverPort;
    private DatagramSocket socket;
    
    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }
    public void start() {
        try {
            System.out.println("Client starting..");
            this.socket = new DatagramSocket();
            System.out.println("Connected to the server");
            // Start a thread to listen for messages from the server
            new Thread(new ServerListener()).start();
            
        } catch (SocketException e) {
            System.out.println("Socket error: " + e.getMessage());
        }
       
    }

    public void sendMessageFromInputField(String inputText){

        try {
            readMessagesFromString(inputText);
        } catch (Exception e) {
            System.out.println("Error sending the message" + e.getMessage());
        }
    }

    private void readMessagesFromString(String messages) {
            try (BufferedReader stringReader = new BufferedReader(new StringReader(messages))) {
                String message;
                while ((message = stringReader.readLine()) != null) {
                    sendMessage(message);
                    if ("exit".equalsIgnoreCase(message)) {
                        System.out.println("Disconnecting from the server...");
                        closeConnection();
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading from string: " + e.getMessage());
            }
        }

    private void sendMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(serverAddress), serverPort);
        socket.send(packet);
    }

    private void closeConnection() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("Connection closed");
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024]; 
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Server: " + message);
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
            }
        }
    }
}