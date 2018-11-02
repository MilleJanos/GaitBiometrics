package com.example.jancsi_pc.playingwithsensors;

public class Accelerometer {
    private long timestamp;
    private float x;
    private float y;
    private float z;
    private int step;

    public Accelerometer(long tt, float xx, float yy, float zz, int step) {
        this.timestamp = tt;
        this.x = xx;
        this.y = yy;
        this.z = zz;
        this.step = step;
    }

    public int getStep() { return step; }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public long getTimeStamp(){ return this.timestamp; }

    @Override
    public String toString() {
        return timestamp +", "+ x +", "+ y +", "+ z + ", " + step;
    }
}
