package com.example.rahul.server;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Server server = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server = Server.getInstance();
        setMovementTypeSpinner();
        setScoringTypeSpinner();
    }

    private void setMovementTypeSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.MovementTypeDropDown);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.MovementType_Array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    private void setScoringTypeSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.ScoringTypeDropDown);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ScoringType_Array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    public void onClickStartServer(View view){
        Intent intent = new Intent(this, PairActivity.class);

        EditText serverNameEditText = findViewById(R.id.ServerNameEditText);
        String serverName = serverNameEditText.getText().toString();
        intent.putExtra("serverName",serverName);

        EditText numberOfPairEditText = findViewById(R.id.NumberOfPairEditText);
        int numberOfPair = Integer.parseInt(numberOfPairEditText.getText().toString());
        intent.putExtra("numberOfPair",numberOfPair);

        server.setNoOfPair(numberOfPair);

        Spinner movementTypeSpinner = (Spinner)findViewById(R.id.MovementTypeDropDown);
        String movementType = movementTypeSpinner.getSelectedItem().toString();
        intent.putExtra("movementType",movementType);

        server.setMovementType(movementType);

        EditText boardsPerTableEditText = findViewById(R.id.BoardsPerTableEditText);
        int boardsPerTable = Integer.parseInt(boardsPerTableEditText.getText().toString());
        intent.putExtra("boardsPerTable",boardsPerTable);

        server.setBoardsPerTable(boardsPerTable);

        Spinner scoringTypeSpinner = (Spinner)findViewById(R.id.ScoringTypeDropDown);
        String scoringType = scoringTypeSpinner.getSelectedItem().toString();
        intent.putExtra("scoringType",scoringType);

        startActivity(intent);
    }

}
