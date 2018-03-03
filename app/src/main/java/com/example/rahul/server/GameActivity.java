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
                    for(int i=0;i<52;i++) deck.add(i);
                    Collections.shuffle(deck);

                    // dealing cards
                    ArrayList<ArrayList<Integer>> playerCards = new ArrayList<>();

                    String bid = server.getClient(0).getTcpSocket().receive();
                    Log.e("check"," "+bid);
                    /*
                    for(int i=0; i<4; i++){
                        server.getClient(finalTableNo*4 + i).getTcpSocket().send(playerCards.get(i).toString());
                    }
                    */

                }
            });

            thread.start();
        }
    }

}
