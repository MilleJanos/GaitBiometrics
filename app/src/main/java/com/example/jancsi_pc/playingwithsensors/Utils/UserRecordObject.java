package com.example.jancsi_pc.playingwithsensors.Utils;

import com.google.firebase.firestore.IgnoreExtraProperties;


@IgnoreExtraProperties
public class UserRecordObject extends UserObject{

    public UserRecordObject(String date, String fileId, String downloadUrl) {
        super(date,fileId,downloadUrl);
    }
}
