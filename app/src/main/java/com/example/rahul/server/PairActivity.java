package com.example.rahul.server;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.net.Socket;
import java.net.UnknownHostException;

public class PairActivity extends AppCompatActivity {

    //Messenger msgnr;
    Server server = null;
    Handler handler = new Handler();
    int connectedClient = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);

        server = Server.getInstance();
        server.setUdpSocket();

        displayTable();
        toggleStartTournamentButton(false);
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
                            updateTable(clientNo);
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

    @SuppressLint("ResourceType")
    public void displayTable(){
        int tableCount = server.getNoOfPair() / 2;
        int tableNoTextWidth = 300;
        int seatTextWidth = 150;
        int boxHeight = 100;
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.tableRelativeLayout);
        int prevtextViewId = 0;
        int prevTextViewId = 0;

        final TextView textView1 = new TextView(this);
        textView1.setText("Table No.");
        textView1.setTextColor(0xff000000);
        textView1.setId(1000);
        final RelativeLayout.LayoutParams params1 =
                new RelativeLayout.LayoutParams(tableNoTextWidth,boxHeight);

        params1.addRule(RelativeLayout.ALIGN_LEFT);
        textView1.setLayoutParams(params1);
        layout.addView(textView1, params1);


        final TextView textView2 = new TextView(this);
        textView2.setText("North");
        textView2.setTextColor(0xff000000);
        textView2.setId(-4);
        final RelativeLayout.LayoutParams params2 =
                new RelativeLayout.LayoutParams(seatTextWidth,boxHeight);

        params2.addRule(RelativeLayout.RIGHT_OF, 1000);
        textView2.setLayoutParams(params2);
        layout.addView(textView2, params2);


        final TextView textView3 = new TextView(this);
        textView3.setText("East");
        textView3.setTextColor(0xff000000);
        textView3.setId(-3);
        final RelativeLayout.LayoutParams params3 =
                new RelativeLayout.LayoutParams(seatTextWidth,boxHeight);

        params3.addRule(RelativeLayout.RIGHT_OF, -4);
        textView3.setLayoutParams(params3);
        layout.addView(textView3, params3);


        final TextView textView4 = new TextView(this);
        textView4.setText("South");
        textView4.setTextColor(0xff000000);
        textView4.setId(-2);
        final RelativeLayout.LayoutParams params4 =
                new RelativeLayout.LayoutParams(seatTextWidth,boxHeight);

        params4.addRule(RelativeLayout.RIGHT_OF, -3);
        textView4.setLayoutParams(params4);
        layout.addView(textView4, params4);


        final TextView textView5 = new TextView(this);
        textView5.setText("West");
        textView5.setTextColor(0xff000000);
        textView5.setId(-1);
        final RelativeLayout.LayoutParams params5 =
                new RelativeLayout.LayoutParams(seatTextWidth,boxHeight);

        params5.addRule(RelativeLayout.RIGHT_OF, -2);
        textView5.setLayoutParams(params5);
        layout.addView(textView5, params5);


        int tableNoListId = 1000;
        int northListId = -3;
        int eastListId = -2;
        int southListId = -1;
        int westListId = -0;

        for(int i=1;i<tableCount+1;i++){

            tableNoListId++;


            final TextView tableNoLoop = new TextView(this);
            tableNoLoop.setText("Table No"+(i));
            tableNoLoop.setTextColor(0xff000000);
            tableNoLoop.setId(tableNoListId);
            final RelativeLayout.LayoutParams tableNoLoopparams =
                    new RelativeLayout.LayoutParams(tableNoTextWidth,boxHeight);

            tableNoLoopparams.addRule(RelativeLayout.BELOW, (tableNoListId-1));
            tableNoLoop.setLayoutParams(tableNoLoopparams);
            layout.addView(tableNoLoop, tableNoLoopparams);

            final TextView northListLoop = new TextView(this);
            northListLoop.setText("Dis");
            northListLoop.setTextColor(0xff000000);
            northListLoop.setId(4 * i + northListId);
            final RelativeLayout.LayoutParams northloopparams =
                    new RelativeLayout.LayoutParams(seatTextWidth,boxHeight);

            northloopparams.addRule(RelativeLayout.BELOW,(tableNoListId-1));
            northloopparams.addRule(RelativeLayout.RIGHT_OF,tableNoListId);
            northListLoop.setLayoutParams(northloopparams);
            layout.addView(northListLoop, northloopparams);


            final TextView eastListLoop = new TextView(this);
            eastListLoop.setText("Dis");
            eastListLoop.setTextColor(0xff000000);
            eastListLoop.setId(4 * i + eastListId);
            final RelativeLayout.LayoutParams eastloopparams =
                    new RelativeLayout.LayoutParams(seatTextWidth,boxHeight);

            eastloopparams.addRule(RelativeLayout.BELOW,(tableNoListId-1));
            eastloopparams.addRule(RelativeLayout.RIGHT_OF,4 * i + northListId);
            eastListLoop.setLayoutParams(eastloopparams);
            layout.addView(eastListLoop, eastloopparams);


            final TextView southListLoop = new TextView(this);
            southListLoop.setText("Dis");
            southListLoop.setTextColor(0xff000000);
            southListLoop.setId(4 * i + southListId);
            final RelativeLayout.LayoutParams southloopparams =
                    new RelativeLayout.LayoutParams(seatTextWidth,boxHeight);

            southloopparams.addRule(RelativeLayout.BELOW,(tableNoListId-1));
            southloopparams.addRule(RelativeLayout.RIGHT_OF,4 * i + eastListId);
            southListLoop.setLayoutParams(southloopparams);
            layout.addView(southListLoop, southloopparams);


            final TextView westListLoop = new TextView(this);
            westListLoop.setText("Dis");
            westListLoop.setTextColor(0xff000000);
            westListLoop.setId(4 * i + westListId);
            final RelativeLayout.LayoutParams westloopparams =
                    new RelativeLayout.LayoutParams(seatTextWidth,boxHeight);

            westloopparams.addRule(RelativeLayout.BELOW,(tableNoListId-1));
            westloopparams.addRule(RelativeLayout.RIGHT_OF,4 * i + southListId);
            westListLoop.setLayoutParams(westloopparams);
            layout.addView(westListLoop, westloopparams);
        }

    }

    public void updateTable(final int clientNo){
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(clientNo+1);
                textView.setText("conn");
                connectedClient++;

                if(connectedClient == server.getNoOfPair() * 2 ){
                    toggleStartTournamentButton(true);
                }
            }
        });
    }

    public void toggleStartTournamentButton(boolean state){
        findViewById(R.id.startTournamentButton).setEnabled(state);
    }
}
