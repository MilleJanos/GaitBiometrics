package com.example.jancsi_pc.playingwithsensors.Utils;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A class that model the user_stats Firebase/Firestore collection's structure in order to update
 * that data
 */
public class UserStatsObject {
    private List<String> devices;
    private String email;
    private int files;
    private long last_session;
    private int sessions;
    private int steps;

    /**
     * Creates an objects with the given attributes
     *
     * @param devices      list of strings that represent the devices ID's
     * @param email        user's email address
     * @param files        number of files the user has uploaded
     * @param last_session timestamp of the last session
     * @param sessions     number of sessions the user has recorded
     * @param steps        number of steps the user has made
     */
    public UserStatsObject(List<String> devices, String email, int files, long last_session, int sessions, int steps) {
        this.devices = devices;
        this.email = email;
        this.files = files;
        this.last_session = last_session;
        this.sessions = sessions;
        this.steps = steps;
    }

    /**
     * Adds a new device in the list of devices if it doesn't already contain it
     *
     * @param device the new device's string representation
     */
    public void addDevice(String device) {
        if (!this.devices.contains(device)) {
            devices.add(device);
        }
    }

    public void incrementFiles() {
        this.files++;
    }

    public long getLast_session() {
        return last_session;
    }

    public void setLast_session(long last_session) {
        this.last_session = last_session;
    }

    public void incrementSessions() {
        this.sessions++;
    }

    public void incrementSteps(int steps) {
        this.steps += steps;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserStatsObject{" +
                "devices=" + devices +
                ", email='" + email + '\'' +
                ", files=" + files +
                ", last_session=" + last_session +
                ", sessions=" + sessions +
                ", steps=" + steps +
                '}';
    }

    /**
     * Function that compares the objects last session timestamp with the parameter timestamp and
     * checks if the two are in the same day
     *
     * @param sessionTimestamp timestamp that has to be compared with the object's one
     * @return true if the parameter is not in the same day as the last_session property
     */
    public boolean isNewSession(long sessionTimestamp) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date(this.last_session));
        cal2.setTime(new Date(sessionTimestamp)); //now
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        return !sameDay;
    }
}
