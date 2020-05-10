package com.example.clickerapp;

public class Corner {
    String corner;
    double time;

    public Corner() {

    }

    public Corner(String corner, double time) {
        this.corner = corner;
        this.time = time;
    }

    public String getCorner() {
        return corner;
    }

    public void setCorner(String corner) {
        this.corner = corner;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
