package com.example.rahul.server;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.net.Socket;
import java.net.UnknownHostException;

public class PairActivity extends AppCompatActivity {

    //Messenger msgnr;
    Server server = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);

        server = Server.getInstance();
        server.setUdpSocket();

        startListening();

    }


    private void startListening(){
        Thread udpThread = new Thread(new Runnable() {
            @Override
            public void run() {

                server.getUdpSocket().setBroadcast(true);

                while (true) {
                    Log.e("server Listning: ",">>>Ready to receive broadcast packets!");

                    String clientData[] = server.getUdpSocket().receive().split(" ");
                    String clientName = clientData[0];
                    String clientIpAddress = clientData[1];

                    Log.e("Client: ",">>>Discovery packet received from: " + clientName+" "+clientIpAddress);

                    WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    String serverIpAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

                    /*
                    String serverIpAddress = null;
                    try {
                        serverIpAddress = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    */
                    server.getUdpSocket().send(clientIpAddress,"server1 "+serverIpAddress);

                    Log.e("sucess","replied to client : server1 "+serverIpAddress);
                }

            }
        });
        udpThread.start();


        Thread tcpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    server.setTcpServerSocket();

                    while (true) {
                        Log.e("server Listning: ",">>>Ready to accept clients!");

                        Socket s = server.getTcpServerSocket().accept();
                        Client client = new Client();
                        client.setTcpSocket(s);

                        String tableDetails[] = client.getTcpSocket().receive().split(" ");

                        int tableNo = Integer.parseInt(tableDetails[0]);
                        int seatNo = Integer.parseInt(tableDetails[1]);
                        int clientNo = (tableNo*4) + seatNo;


                        if(server.getClient(clientNo) != null){
                            client.getTcpSocket().send("FROM SERVER - seat is already taken");
                        }
                        else{
                            server.setClient(clientNo,client);
                            server.getClient(clientNo).getTcpSocket().send("sucess");
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }catch(Exception e){
                    Log.e("Exception","Exception Occured: " + e);
                }
            }
        });
        tcpThread.start();

    }

    public void onClickStartTournament(View view){
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

}
