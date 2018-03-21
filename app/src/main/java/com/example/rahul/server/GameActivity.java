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
                    boolean allPass = false;
                    int suitProposer[] = new int[10];
                    Arrays.fill(suitProposer,-1);

                    while(true) {
                        if (currBidder == lastProposer)
                            break;

                        String bidData = currBid + " " + lastProposer + " " + doubleStatus + " " + redoubleStatus;
                        server.getClient(finalTableNo * 4 + currBidder).getTcpSocket().send(bidData);

                        Log.e("check","bidData: "+bidData+" "+currBidder);

                        String inputBid = server.getClient(finalTableNo * 4 + currBidder).getTcpSocket().receive();
                        int inputBidInt = Integer.parseInt(inputBid);
                        if (inputBidInt >= 0 && inputBidInt<35) {
                            currBid = inputBidInt;
                            lastProposer = currBidder;
                            passCount = 0;
                            int temp = (currBidder % 2) * 5 + (currBid % 5);    // -1???????????
                            if (suitProposer[temp] == -1)
                                suitProposer[temp] = currBidder;
                        } else if (inputBidInt == 37) {
                            doubleStatus = 1;
                        } else if (inputBidInt == 35) {
                            redoubleStatus = 1;
                        } else if (inputBidInt == 36) {
                            passCount++;
                            if (passCount == 4) {
                                // all pass code
                                allPass = true;
                                break;
                            }
                        }
                        currBidder = (currBidder + 1) % 4;
                    }

                    for(int i=0; i<4 ;i++){
                        server.getClient(finalTableNo*4 + i).getTcpSocket().send("bid finished");
                    }

                    if(!allPass) {

                        int temp = (lastProposer % 2) * 5 + (currBid % 5);
                        int currPlayer = (suitProposer[temp] + 1) % 4;
                        int dummyPlayer = (currPlayer + 1) % 4;
                        int trump = (currBid % 5) + 1;
                        int teamScore[] = new int[2];
                        int winnerTeam = -1;
                        boolean firstCard = true;

                        while (true) {
                            int currHand[] = new int[4];
                            int currSuit = -1;
                            for (int i = 0; i < 4; i++) {
                                Log.e("check",""+i+" "+currPlayer);
                                if(currPlayer == dummyPlayer){
                                    server.getClient(finalTableNo*4 + (currPlayer+2)%4).getTcpSocket().send("currSuit dummyTurn " + currSuit);
                                    currHand[currPlayer] = Integer.parseInt(server.getClient(finalTableNo * 4 + (currPlayer+2)%4).getTcpSocket().receive());
                                }
                                else{
                                    server.getClient(finalTableNo*4 + currPlayer).getTcpSocket().send("currSuit yourTurn " + currSuit);
                                    currHand[currPlayer] = Integer.parseInt(server.getClient(finalTableNo * 4 + currPlayer).getTcpSocket().receive());
                                }

                                if(firstCard){
                                    for(int j=0; j<4; j++){
                                        if(j != dummyPlayer) {
                                            server.getClient(finalTableNo * 4 + j).getTcpSocket().send("dummyPlayer " + dummyPlayer);
                                            server.getClient(finalTableNo * 4 + j).getTcpSocket().send(playerCards.get(dummyPlayer).toString());
                                        }
                                    }
                                    firstCard = false;
                                }

                                if (currSuit == -1)
                                    currSuit = currHand[currPlayer] / 100;

                                for (int j = 0; j < 4; j++) {
                                    server.getClient(finalTableNo * 4 + j).getTcpSocket().send("displayPlayedCard " + currPlayer + " " + currHand[currPlayer]);
                                }
                                currPlayer = (currPlayer + 1) % 4;
                            }

                            int maxCard = currHand[currPlayer];
                            int maxPlayer = currPlayer;
                            for (int i = 0; i < 3; i++) {
                                currPlayer = (currPlayer + 1) % 4;
                                int playedSuit = currHand[currPlayer] / 100;
                                if (playedSuit == trump) {
                                    if (currHand[currPlayer] + 1000 > maxCard) {
                                        maxCard = currHand[currPlayer];
                                        maxPlayer = currPlayer;
                                    }
                                } else if (playedSuit == currSuit) {
                                    if (currHand[currPlayer] > maxCard) {
                                        maxCard = currHand[currPlayer];
                                        maxPlayer = currPlayer;
                                    }
                                }
                            }
                            teamScore[maxPlayer % 2]++;
                            currPlayer = maxPlayer;

                            Log.e("check", "gameData: " + trump + " " + currPlayer + " " + currHand.toString());

                            if (teamScore[0] + teamScore[1] == 13) {
                                int targetBid = 6 + (currBid / 5) + 1;
                                if (teamScore[lastProposer % 2] >= targetBid) {
                                    winnerTeam = lastProposer % 2;
                                } else {
                                    winnerTeam = (lastProposer + 1) % 2;
                                }
                                break;
                            }
                        }
                    }
                }
            });

            thread.start();
        }
    }

}
