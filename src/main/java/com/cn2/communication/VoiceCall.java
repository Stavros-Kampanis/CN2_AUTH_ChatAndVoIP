package com.cn2.communication;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class VoiceCall {
    private static final float SAMPLE_RATE = 8000.0f;
    private static final int SAMPLE_SIZE_IN_BITS = 8;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    private static final int BUFFER_SIZE = 1024;

    private final String peerAddress;
    private final int peerPort;
    private DatagramSocket socket;
    private boolean running;

    public VoiceCall(String peerAddress, int peerPort) {
        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
    }

    public void start() {
        try {
            socket = new DatagramSocket(peerPort);
            running = true;

            // Start capturing and sending audio
            new Thread(this::captureAndSendAudio).start();

            // Start receiving and playing audio
            new Thread(this::receiveAndPlayAudio).start();
        } catch (IOException e) {
            System.err.println("Error starting voice call peer: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private void captureAndSendAudio() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);

        try (TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo)) {
            targetLine.open(format);
            targetLine.start();

            byte[] buffer = new byte[BUFFER_SIZE];
            while (running) {
                int bytesRead = targetLine.read(buffer, 0, buffer.length);
                InetAddress address = InetAddress.getByName(peerAddress);
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, address, peerPort);
                socket.send(packet);
                System.out.println("Sent " + bytesRead + " bytes to " + peerAddress + ":" + peerPort);

            }
        } catch (LineUnavailableException | IOException e) {
            System.err.println("Error capturing and sending audio: " + e.getMessage());
        }
    }

    private void receiveAndPlayAudio() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
        try (SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo)) {
            sourceLine.open(format);
            sourceLine.start();

            byte[] buffer = new byte[BUFFER_SIZE];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Waiting to receive packet...");
                try {
                    socket.receive(packet);
                    System.out.println("Received " + packet.getLength() + " bytes from " + packet.getAddress() + ":" + packet.getPort());
                    sourceLine.write(packet.getData(), 0, packet.getLength());
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error receiving packet: " + e.getMessage());
                    } else {
                        System.out.println("Socket closed, stopping receive loop.");
                    }
                }
            }
        } catch (LineUnavailableException e) {
            System.err.println("Error receiving and playing audio: " + e.getMessage());
        }
    }

    // public static void main(String[] args) {
  
    //     String peerAddress = "192.168.2.8";
    //     int peerPort = 9876; // Define the peerPort variable
    //     VoiceCall voiceCall = new VoiceCall(peerAddress, peerPort);
    //     voiceCall.start();

    //     // Add a shutdown hook to stop the peer gracefully
    //     Runtime.getRuntime().addShutdownHook(new Thread(voiceCall::stop));
    // }
} 
    

