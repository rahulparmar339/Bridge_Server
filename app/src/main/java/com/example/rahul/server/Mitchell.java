package com.example.rahul.server;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by rahul on 11-Apr-18.
 */

public class Mitchell {
    Server server = null;
    int totalBoard = 0;

    public Mitchell(){
        server = Server.getInstance();
    }

    public ArrayList<ArrayList<Integer>> createBoards(){

        //creating boards
        ArrayList<ArrayList<Integer>> board = new ArrayList<>();
        int totalTable = server.getNoOfPair()/2;
        totalBoard =  server.getBoardsPerTable() * totalTable;

        if(totalTable % 2 ==0) //for evenTablemovement
            totalBoard += server.getBoardsPerTable();

        for(int i=0; i<totalBoard; i++){
            ArrayList<Integer> newBoard = new ArrayList<>();
            // suitOrder Ascending    club < diamond < heart < spade < noTrump
            for(int j=102;j<115;j++) newBoard.add(j);  // club 2 to club ace
            for(int j=202;j<215;j++) newBoard.add(j);  // diamond 2 to diamond ace
            for(int j=302;j<315;j++) newBoard.add(j);  // hearts 2 to haerts ace
            for(int j=402;j<415;j++) newBoard.add(j);  // spade 2 to spae ace
            Collections.shuffle(newBoard);

            board.add(newBoard);
        }

        if(totalTable % 2== 0){
            for(int i=0; i<server.getBoardsPerTable() ;i++){  //making first and last table boards same
                board.set(i, board.get(totalBoard-i-1));
            }
            for(int i=0; i<server.getBoardsPerTable() ;i++){  //sending virtual table to last
                ArrayList<Integer> temp = board.remove((totalTable/2) * totalBoard);
                board.add(temp);
            }
        }
        return board;
    }

    public void updateTableAndBoard(ArrayList<ArrayList<Integer>> board){
        if(server.getBoardsPerTable() % 2 == 0){
            evenTableMovement(board);
        }
        else{
            oddTableMovement(board);
        }
    }

    public void oddTableMovement(ArrayList<ArrayList<Integer>> board){

        // client movement
        int totalTable = server.getNoOfPair()/2;

        Client lastTableEast = server.getClient(totalTable * 4 - 3);
        Client lastTableWest = server.getClient(totalTable * 4 - 1);

        for(int tableNo=totalTable ; tableNo>1 ; tableNo++){
            int currClient = tableNo*4 - 3; //curr east player
            server.setClient( currClient, server.getClient(currClient-4));
            currClient = currClient+2;    //curr west player
            server.setClient( currClient, server.getClient(currClient-4));
        }

        server.setClient( 1, lastTableEast);
        server.setClient( 3, lastTableWest);

        //board movement
        for(int i=0; i<server.getBoardsPerTable(); i++){
            ArrayList<Integer> temp = board.remove(0);
            board.add(temp);
        }


    }

    public void evenTableMovement(ArrayList<ArrayList<Integer>> board){

        // client movement
        int totalTable = server.getNoOfPair()/2;

        Client lastTableEast = server.getClient(totalTable * 4 - 3);
        Client lastTableWest = server.getClient(totalTable * 4 - 1);

        for(int tableNo=totalTable ; tableNo>1 ; tableNo--){
            int currClient = tableNo*4 - 3; //curr east player
            server.setClient( currClient, server.getClient(currClient-4));
            currClient = currClient+2;    //curr west player
            server.setClient( currClient, server.getClient(currClient-4));
        }

        server.setClient( 1, lastTableEast);
        server.setClient( 3, lastTableWest);

        //board movement
        for(int i=0; i<server.getBoardsPerTable(); i++){  //taking virtual table boards to center
            ArrayList<Integer> temp = board.remove(totalBoard-1);
            board.add((totalTable/2) * totalBoard, temp);
        }

        for(int boardNo=0 ; boardNo<totalBoard-server.getBoardsPerTable() ; boardNo++){ //moving table boards downwards
            board.set(boardNo, board.get(boardNo+server.getBoardsPerTable()));
        }

        for(int i=0; i<server.getBoardsPerTable() ;i++){  //making first and last table boards same
            board.set(i, board.get(totalBoard-i-1));
        }

        for(int i=0; i<server.getBoardsPerTable() ;i++){  //sending virtual table to last
            ArrayList<Integer> temp = board.remove((totalTable/2) * totalBoard);
            board.add(temp);
        }
    }

}
