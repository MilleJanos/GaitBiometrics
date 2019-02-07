package com.example.jancsi_pc.playingwithsensors.Utils.Firebase;


import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * This class is used to help storing in Firebase firestore.
 *
 * @author Mille Janos
 */
@IgnoreExtraProperties
public class UserObject {

    public String date;             // if you make changes make
    public String fileId;           // sore that FirebaseUtil
    public String downloadUrl;      // is updated too !

    public UserObject(){ }

    public UserObject(String date, String fileId, String downloadUrl) {
        this.date = date;
        this.fileId = fileId;
        this.downloadUrl = downloadUrl;
    }
}
