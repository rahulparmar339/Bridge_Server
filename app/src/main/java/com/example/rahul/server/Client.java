package com.example.rahul.server;

import java.net.Socket;

/**
 * Created by rahul on 21-Feb-18.
 */

public class Client {
    private  String name = null;
    private  TCPSocket tcpSocket = null;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }

    public void setTcpSocket(Socket socket){
        this.tcpSocket = new TCPSocket(socket);
    }
    public TCPSocket getTcpSocket(){
        return this.tcpSocket;
    }
}
