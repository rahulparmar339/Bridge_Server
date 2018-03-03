package com.example.rahul.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by rahul on 21-Feb-18.
 */

public class TCPServerSocket {

    private static final int SERVER_PORT_NO = 9002;
    private ServerSocket tcpSocket = null;


    public TCPServerSocket(){
        try {
            tcpSocket = new ServerSocket(SERVER_PORT_NO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket accept(){
        try {
            return this.tcpSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
