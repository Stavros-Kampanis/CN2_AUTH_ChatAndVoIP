package com.cn2.communication;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Peer {
    public static final int DISCOVERY_PORT = 9876;
    public static final int RESPONSE_PORT = 9877;
    private static final int MESSAGE_PORT = 12345;
    private static final int VOICE_CALL_PORT = 9876;

    private Client client;
    private VoiceCall voiceCall;
    private InetAddress address;
    @SuppressWarnings("unused")
    private App app;

    public Client getClient() {
        return client;
    }
    public InetAddress getAddress() {
        return address;
    }

    public Peer() {
        try {
            this.address = InetAddress.getLocalHost();
            
        } catch (IOException e) {
            System.out.println("Error getting local address: " + e);
        }
    }

    public void start(App app) {
        this.app = app;
        // Start the server
        new Thread(() -> new Server(this,MESSAGE_PORT,Server.Type.MESSAGE, app).start()).start();

        // // Listen for discovery messages
        // new Thread(() -> new Server(this,DISCOVERY_PORT,Server.Type.DISCOVERY, app).start()).start();

        // // Listen for response messages
        // new Thread(() -> new Server(this,RESPONSE_PORT,Server.Type.RESPONSE, app).start()).start();
        // // Send discovery message
        // sendDiscoveryMessage();
    }

    public void voiceCall(){
        
        String peerAddress = "192.168.1.28";      // Place the IP Address of the other peer here
        int peerPort = VOICE_CALL_PORT;
        this.voiceCall = new VoiceCall(peerAddress, peerPort);
        this.voiceCall.start();
    }
    
    public void stopVoiceCall(){
        voiceCall.stop();
        Runtime.getRuntime().addShutdownHook(new Thread(this.voiceCall::stop));
    }



    public void startClient(String peerAddress, int peerPort) {
        // Start the client and bind it to the address and port of the other peer
        System.out.println("Starting client to connect to " + peerAddress + ":" + peerPort);
        
        // Create and start the client in a new thread
        this.client = new Client(peerAddress, peerPort);
        this.client.start();
        
    }
    
    @SuppressWarnings("unused")
    private void sendDiscoveryMessage() {
        try {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                String discoveryMessage = "DISCOVER_PEER";
                byte[] buffer = discoveryMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
                socket.send(packet);
            }
            System.out.println("Discovery message sent to port " + DISCOVERY_PORT);
        } catch (IOException e) {
            System.out.println("Error sending discovery message: " + e.getMessage());

        }
    }

    // public static void main(String[] args) {
    //     Peer peer = new Peer();
    //     peer.start(null);
    // }
}