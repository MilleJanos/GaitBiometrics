package com.example.jancsi_pc.playingwithsensors.Utils;

import com.google.firebase.firestore.IgnoreExtraProperties;


@IgnoreExtraProperties
public class UserAndHisFile {

    public String date;
    public String fileId;

    public UserAndHisFile(String date, String fileId) {
        this.date = date;
        this.fileId = fileId;
    }
}
