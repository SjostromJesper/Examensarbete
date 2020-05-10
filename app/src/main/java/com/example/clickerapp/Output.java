package com.example.clickerapp;

import java.util.Random;

public class Output {
    private Random random = new Random();

    public void sendSignal() {
        for(int i = 0 ; i < 100 ; i++) {
            System.out.println(random.nextInt(4));
        }
    }

    public int sendSignalInternal() {
        return random.nextInt(4);
    }
}
