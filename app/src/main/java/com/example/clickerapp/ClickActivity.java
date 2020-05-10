package com.example.clickerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ClickActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    public static final int UPPER_LEFT = 0;
    public static final int UPPER_RIGHT = 1;
    public static final int BOTTOM_LEFT = 2;
    public static final int BOTTOM_RIGHT = 3;

    List<Corner> cornerList = new ArrayList<>();

    Button uLeft;
    Button uRight;
    Button bLeft;
    Button bRight;

    TextView remaining;
    Spinner difficultySpinner;

    Random random;

    double startTime;
    Corner corner;

    int currentRound = 0;
    int rounds = 10;

    int difficulty = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click);

        Intent intent = getIntent();

        try {
            if(intent.getStringExtra(MainActivity.TIMES) != null) {
                rounds = Integer.parseInt(intent.getStringExtra(MainActivity.TIMES));
            }
        }
        catch(Exception e) {
            System.out.println("something went wrong with data package");
        }

        random = new Random();

        difficultySpinner = findViewById(R.id.difficultySpinner);
        remaining = findViewById(R.id.remaining);

        uLeft = findViewById(R.id.uLeft);
        uRight = findViewById(R.id.uRight);
        bLeft = findViewById(R.id.bLeft);
        bRight = findViewById(R.id.bRight);

        uLeft.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
        uRight.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
        bLeft.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
        bRight.setBackgroundColor(getResources().getColor(R.color.inactiveButton));

        spinner();
    }

    public void startSession(View view) {
        currentRound = 0;
        startTime = System.currentTimeMillis();
        round();
    }

    double cornerTimer;

    public void round() {
        String countdown = "remaining: " + (rounds - currentRound);
        remaining.setText(countdown);
        currentRound++;

        corner = new Corner();

        int rCorner = random.nextInt(4);
        cornerTimer = System.currentTimeMillis();

        if(rCorner == UPPER_LEFT) {
            uLeft.setBackgroundColor(getResources().getColor(R.color.buttonColor));
            corner.setCorner("Upper left");
        }
        else if(rCorner == UPPER_RIGHT) {
            uRight.setBackgroundColor(getResources().getColor(R.color.buttonColor));
            corner.setCorner("Upper right");
        }
        else if(rCorner == BOTTOM_LEFT) {
            bLeft.setBackgroundColor(getResources().getColor(R.color.buttonColor));
            corner.setCorner("Bottom left");
        }
        else if(rCorner == BOTTOM_RIGHT) {
            bRight.setBackgroundColor(getResources().getColor(R.color.buttonColor));
            corner.setCorner("Bottom right");
        }
        else {
            System.out.println("Something went wrong");
        }
    }

    public void stop(View view) {
        uLeft.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
        uRight.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
        bLeft.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
        bRight.setBackgroundColor(getResources().getColor(R.color.inactiveButton));

        double stopTime = System.currentTimeMillis();
        double elapsedTime = (stopTime - cornerTimer) / 1000;

        corner.setTime(elapsedTime);

        if(currentRound < rounds) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    round();
                }
            }, difficulty);
        }
        else {
            cornerList.add(corner);
            remaining.setText("Complete!");
        }
    }

    private void spinner() {
        difficultySpinner = findViewById(R.id.difficultySpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.difficulty, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);
        difficultySpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();

        if(text.equalsIgnoreCase("easy")) {
            difficulty = 2000;
        }
        else if(text.equalsIgnoreCase("medium")) {
            difficulty = 1000;
        }
        else if(text.equalsIgnoreCase("hard")) {
            difficulty = 500;
        }
        else {
            System.out.println("something went wrong with the difficulty");
        }

        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}