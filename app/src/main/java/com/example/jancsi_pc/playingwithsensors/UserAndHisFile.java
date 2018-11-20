package com.example.jancsi_pc.playingwithsensors;

import com.google.firebase.firestore.IgnoreExtraProperties;


@IgnoreExtraProperties
public class UserAndHisFile {

    public String devId;
    public String userId;
    public String fileId;

    public UserAndHisFile(String devId, String userId, String fileId) {
        this.devId = devId;
        this.userId = userId;
        this.fileId = fileId;
    }
}
