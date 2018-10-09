package com.example.jancsi_pc.playingwithsensors;

public class Accelerometer {
    private long timestamp;
    private float x;
    private float y;
    private float z;

    public Accelerometer(long tt, float xx, float yy, float zz) {   //TODO private ?
        this.timestamp = tt;
        this.x = xx;
        this.y = yy;
        this.z = zz;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public long getTimeStamp(){ return this.timestamp; }

    public void setTimeStamp( long t ){ this.timestamp = t; }

    @Override
    public String toString() {
        return timestamp +", "+ x +", "+ y +", "+ z;
    }
}
