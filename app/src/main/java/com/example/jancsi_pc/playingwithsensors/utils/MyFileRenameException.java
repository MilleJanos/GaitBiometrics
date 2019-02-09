package com.example.jancsi_pc.playingwithsensors.utils;

public class MyFileRenameException extends Exception {

    private static String mLastMessage;

    public MyFileRenameException(String message) {
        super(message);
        mLastMessage = message;
    }

    public String getLastErrorMessage() {
        return mLastMessage;
    }

}
