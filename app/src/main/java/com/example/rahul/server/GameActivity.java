package com.example.rahul.server;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GameActivity extends AppCompatActivity {

    Server server = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        server = Server.getInstance();
        startTournament();
    }

    public void startTournament(){
        int tableCount = server.getNoOfPair()/2;

        for(int tableNo=0; tableNo<tableCount; tableNo++){
            final int finalTableNo = tableNo;

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // creating Deck
                    ArrayList<Integer> deck = new ArrayList<>();

                    // suitOrder Ascending    club < diamond < heart < spade < noTrump
                    for(int i=102;i<115;i++) deck.add(i);  // club 2 to club ace
                    for(int i=202;i<215;i++) deck.add(i);  // diamond 2 to diamond ace
                    for(int i=302;i<315;i++) deck.add(i);  // hearts 2 to haerts ace
                    for(int i=402;i<415;i++) deck.add(i);  // spade 2 to spae ace
                    Collections.shuffle(deck);

                    // dealing cards
                    ArrayList<ArrayList<Integer>> playerCards = new ArrayList<>();
                    for(int i=0; i<4; i++){
                        ArrayList<Integer> temp = new ArrayList<>();
                        for(int j=0; j<13 ;j++){
                            temp.add(j, deck.get(i*13 + j));
                        }
                        playerCards.add(i, temp);
                    }
                    //sending cards
                    for(int i=0; i<4; i++){
                        server.getClient(finalTableNo*4 + i).getTcpSocket().send(playerCards.get(i).toString());
                    }
                    int currBid = 0;
                    int currBidder = 0;
                    int lastProposer = -1;
                    int doubleStatus = 0;
                    int passCount = 0;
                    int redoubleStatus = 0;
                    int suitProposer[] = new int[10];
                    Arrays.fill(suitProposer,-1);

                    while(true) {
                        if (currBidder == lastProposer)
                            break;

                        String bidData = currBid + " " + lastProposer + " " + doubleStatus + " " + redoubleStatus;
                        server.getClient(finalTableNo * 4 + currBidder).getTcpSocket().send(bidData);

                        String inputBid = server.getClient(finalTableNo * 4 + currBidder).getTcpSocket().receive();
                        int inputBidInt = Integer.parseInt(inputBid);
                        if (inputBidInt >= 0) {
                            currBid = inputBidInt;
                            lastProposer = currBidder;
                            int temp = (currBidder % 2) * 5 + (currBid % 5);    // -1???????????
                            if (suitProposer[temp] == -1)
                                suitProposer[temp] = currBidder;
                        } else if (inputBidInt == -1) {
                            doubleStatus = 1;
                        } else if (inputBidInt == -2) {
                            redoubleStatus = 1;
                        } else if (inputBidInt == -3) {
                            passCount++;
                            if (passCount == 4)
                                break;
                        }
                        currBidder = (currBidder + 1) % 4;
                    }

                    for(int i=0; i<4 ;i++){
                        server.getClient(finalTableNo*4 + i).getTcpSocket().send("bid finished");
                    }
                }
            });

            thread.start();
        }
    }

}
