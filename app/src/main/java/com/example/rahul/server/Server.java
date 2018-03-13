package com.example.rahul.server;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by rahul on 21-Feb-18.
 */

public class Server {
    private static Server server = null;
    private static String name = null;
    private static int noOfPair;
    private static String movementType = null;
    private static int boardsPerTable;
    private static String scoringType = null;
    private static UDPSocket udpSocket = null;
    private static TCPServerSocket tcpServerSocket = null;
    private static Client client[];

    public static Server getInstance(){
        if(server == null){
            server = new Server();
        }
        return server;
    }
    public void setUdpSocket(){
        udpSocket = new UDPSocket();
    }

    public UDPSocket getUdpSocket(){
        return udpSocket;
    }

    public void setTcpServerSocket(){
        tcpServerSocket = new TCPServerSocket();
    }
    public TCPServerSocket getTcpServerSocket(){
        return tcpServerSocket;
    }

    public void setNoOfPair(int n){
        noOfPair = n;
        client = new Client[noOfPair*2];
        Arrays.fill(client,null);
    }
    public int getNoOfPair(){
        return noOfPair;
    }


    public void setClient(int index, Client c){
        client[index] = c;
    }
    public Client getClient(int index){
        return client[index];
    }

}
