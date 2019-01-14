package com.example.jancsi_pc.playingwithsensors.Utils;


/**
 * This class is used to help storing in Firebase firestore.
 *
 * @author Mille Janos
 */
public class UserObject {

    private String date;             // if you make changes make
    private String fileId;           // sore that FirebaseUtil
    private String downloadUrl;      // is updated too !


    public UserObject(String date, String fileId, String downloadUrl) {
        this.date = date;
        this.fileId = fileId;
        this.downloadUrl = downloadUrl;
    }
}
