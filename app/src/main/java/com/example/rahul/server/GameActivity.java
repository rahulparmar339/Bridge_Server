package com.example.rahul.server;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GameActivity extends AppCompatActivity {

    Server server = null;
    ArrayList<ArrayList<Integer>> board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        server = Server.getInstance();
        startTournament();
    }

    public void startTournament(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendTotalTableAndBoardperTable();
                Mitchell mitchell = new Mitchell();
                board = mitchell.createBoards();
                startMovement(mitchell);
            }
        });
        thread.start();
    }

    public void sendTotalTableAndBoardperTable(){
        //sending totalTable and boardspaerTable to all client
        for(int clienNo=0; clienNo<server.getNoOfPair()*2; clienNo++){
            server.getClient(clienNo).getTcpSocket().send(""+server.getNoOfPair()/2 +" "+server.getBoardsPerTable());
        }
    }

    public void startMovement(Mitchell mitchell){
        int totalTable = server.getNoOfPair()/2;
        for(int roundNo=0; roundNo<totalTable; roundNo++){
            startRound();
            mitchell.updateTableAndBoard(board);
        }
    }

    public void startRound()    {
        int totalTable = server.getNoOfPair()/2;
        Thread thread[] = new Thread[totalTable];

        for(int tableNo=0; tableNo<totalTable; tableNo++){
            final int finalTableNo = tableNo;
            thread[finalTableNo] = new Thread(new Runnable() {
                @Override
                public void run() {

                    for(int boardNo = finalTableNo; boardNo< finalTableNo+ server.getBoardsPerTable(); boardNo++){

                        Log.e("checkboardNo",""+boardNo);

                        // dealing cards
                        ArrayList<ArrayList<Integer>> playerCards = new ArrayList<>();
                        for(int i=0; i<4; i++){
                            ArrayList<Integer> temp = new ArrayList<>();
                            for(int j=0; j<13 ;j++){
                                temp.add(j, board.get(boardNo).get(i*13 + j));
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
                            server.getClient(finalTableNo * 4 + currBidder).getTcpSocket().send("bidTurn " + bidData);

                            Log.e("check","bidData: "+bidData+" "+currBidder);

                            String inputBid = server.getClient(finalTableNo * 4 + currBidder).getTcpSocket().receive();
                            int inputBidInt = Integer.parseInt(inputBid) -1 ; // bid range 0 to 37

                            //sending bidData to everyOne
                            for(int i=0; i<4 ;i++){
                                server.getClient(finalTableNo*4 + i).getTcpSocket().send("bidData "+currBidder+" "+inputBidInt);
                            }

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
                            server.getClient(finalTableNo*4 + i).getTcpSocket().send("bidFinished");
                        }

                        int winnerTeam = -1;
                        if(!allPass) {
                            int temp = (lastProposer % 2) * 5 + (currBid % 5);
                            int currPlayer = (suitProposer[temp] + 1) % 4;
                            int dummyPlayer = (currPlayer + 1) % 4;
                            int trump = (currBid % 5) + 1;
                            int teamScore[] = new int[2];
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
                                    if(i==0) {
                                        for (int j = 0; j < 4; j++) {
                                            server.getClient(finalTableNo * 4 + j).getTcpSocket().send("handComplete "+teamScore[0]+" "+teamScore[1]);
                                        }
                                    }
                                    for (int j = 0; j < 4; j++) {
                                        server.getClient(finalTableNo * 4 + j).getTcpSocket().send("displayPlayedCard " + currPlayer + " " + currHand[currPlayer]);
                                    }
                                    currPlayer = (currPlayer + 1) % 4;
                                }


                                int maxCard = currHand[currPlayer];
                                int maxPlayer = currPlayer;
                                if(currHand[currPlayer] / 100==trump)
                                    maxCard+=1000;
                                for (int i = 0; i < 3; i++) {
                                    currPlayer = (currPlayer + 1) % 4;
                                    int playedSuit = currHand[currPlayer] / 100;
                                    if (playedSuit == trump) {
                                        if (currHand[currPlayer] + 1000 > maxCard) {
                                            maxCard = currHand[currPlayer] + 1000;
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
                        for(int i=0; i<4 ;i++){
                            server.getClient(finalTableNo*4 + i).getTcpSocket().send("winner "+winnerTeam);
                        }
                    }

                }
            });

            thread[finalTableNo].start();
        }
        for(int tableNo=0; tableNo<totalTable; tableNo++){
            try {
                thread[tableNo].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
