package com.example.rahul.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by rahul on 21-Feb-18.
 */

public class UDPSocket {
    private static final String BROADCAST_RECEIVE_IP_ADDRESS = "0.0.0.0";
    private static final int CLIENT_PORT_NO = 9999;
    private static final int SERVER_PORT_NO = 4445;

    private DatagramSocket socket = null;

    public UDPSocket(){
        try {
            socket = new DatagramSocket(SERVER_PORT_NO,InetAddress.getByName(BROADCAST_RECEIVE_IP_ADDRESS));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void send(String clientIP, String msg){
        byte[] sendBuffer = msg.getBytes();
        DatagramPacket sendPacket =null;

        try {
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(clientIP), CLIENT_PORT_NO);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBroadcast(boolean val){
        try {
            socket.setBroadcast(val);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public String receive(){
        byte[] recvBuffer = new byte[1000];
        DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);

        try {
            socket.receive(recvPacket);
            return new String(recvPacket.getData()).trim();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close(){
        socket.close();
    }
}
