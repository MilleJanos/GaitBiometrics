package com.example.jancsi_pc.playingwithsensors.Utils;

import com.google.firebase.firestore.IgnoreExtraProperties;


@IgnoreExtraProperties
public class UserRecordObject {

    public String date;             // if you make changes make
    public String fileId;           // sore that FirebaseUtil
    public String downloadUrl;      // is updated too !

    public UserRecordObject(String date, String fileId, String downloadUrl) {
        this.date = date;
        this.fileId = fileId;
        this.downloadUrl = downloadUrl;
    }
}
