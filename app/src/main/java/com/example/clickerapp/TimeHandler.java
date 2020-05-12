package com.example.clickerapp;

public class TimeHandler {

    private double easy = 10000;
    private double medium = 5000;
    private double hard=  1000;


    public static double startTime = 0.0d;
    public static double endTime = 0.0d;


    public static void printTime() {
        double elapsedTime = endTime - startTime;
        System.out.println("elapsed time: " + elapsedTime);
    }
}
