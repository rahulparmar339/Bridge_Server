package com.example.rahul.server;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
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

                    server.getClient(0).getTcpSocket().send(playerCards.get(0).toString());
                    String bid = server.getClient(0).getTcpSocket().receive();

                    Log.e("check",""+bid);
                }
            });

            thread.start();
        }
    }

}
