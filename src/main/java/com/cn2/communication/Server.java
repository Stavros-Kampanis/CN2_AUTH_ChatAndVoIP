package com.cn2.communication;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;


public class Server{
    private final Peer peer;
    private final int port;
    private final Type serverType;
    private App app;


    public Server(Peer peer,int port,Type serverType,App app) {
        this.peer = peer;
        this.port = port;
        this.serverType = serverType;
        this.app = app;
    }

    public void start() {
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            System.out.println(serverType+" Server is listening on port " + port);

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet); // Wait for an incoming packet
                new ClientHandler(serverSocket, packet).start();
            }
        } catch (IOException e) {
            System.out.println(serverType+" server on port: "+port+" starting error :"+e.getMessage());
        }
    }   
    
    private void respondToDiscoveryMessage(InetAddress address) {
        try {
            try (DatagramSocket socket = new DatagramSocket()) {
                String responseMessage = "PEER_RESPONSE";
                byte[] buffer = responseMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, Peer.RESPONSE_PORT);
                socket.send(packet);
            }
            System.out.println("Response message sent to: " + address.getHostAddress());
        } catch (IOException e) {
            System.out.println("Error responding to discovery message: " + e.getMessage());
        }
    }


    class ClientHandler extends Thread {
        private final DatagramSocket serverSocket;
        private final DatagramPacket packet;

        public ClientHandler(DatagramSocket serverSocket, DatagramPacket packet) {
            this.serverSocket = serverSocket;
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received from client: " + message);

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                switch (serverType) {
                    case MESSAGE: handleMessageType(clientAddress, clientPort, message); break;
                    case DISCOVERY: handleDiscoveryType(clientAddress, message); break;
                    case RESPONSE: handleResponseType(clientAddress, message); break;
                }

                // Check for a termination condition (e.g., client sends "exit")
                if ("exit".equalsIgnoreCase(message)) {
                    System.out.println("Client disconnected.");
                }
            } catch (IOException e) {
                System.err.println("Error handling client message: " + e.getMessage());
            }
        }

        private void handleMessageType(InetAddress clientAddress, int clientPort, String message) throws IOException {
            System.out.println("Handling MESSAGE type");
            String response = "Message received: " + message;
            byte[] responseBuffer = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, clientAddress, clientPort);
            serverSocket.send(responsePacket);
            // Notify the GUI about the received message
             if (app != null) {
                app.handleMessage(message);
            }
        }

        private void handleDiscoveryType(InetAddress clientAddress, String message) {
            System.out.println("Handling DISCOVERY type");
            if (!clientAddress.equals(peer.getAddress()) && "DISCOVER_PEER".equals(message)) {
                System.out.println("Discovery message received from: " + clientAddress.getHostAddress());
                respondToDiscoveryMessage(clientAddress);
            }
        }

        private void handleResponseType(InetAddress clientAddress, String message) {
            System.out.println("Handling RESPONSE type");
            if ("PEER_RESPONSE".equals(message)) {
                try (Scanner scanner = new Scanner(System.in)) {
                    System.out.println("Do you want to chat with " + clientAddress + "? (yes/no)");
                    String userAnswer = scanner.nextLine();
                    if ("yes".equalsIgnoreCase(userAnswer)) {
                        peer.startClient(clientAddress.getHostAddress(), port);
                    } else {
                        System.out.println("Chat declined.");
                    }
                }
            }
        }
    }
    public enum Type {
        DISCOVERY,
        RESPONSE,
        MESSAGE
    }

    public interface MessageHandler {
        void handleMessage(String message);
    }
}
