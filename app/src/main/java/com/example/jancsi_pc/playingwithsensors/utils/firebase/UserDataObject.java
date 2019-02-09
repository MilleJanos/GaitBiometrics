package com.example.jancsi_pc.playingwithsensors.utils.firebase;

import com.google.firebase.firestore.IgnoreExtraProperties;


/**
 * This class is used to store Users data in Firebase firestore.
 *
 * @author Fulop Timea
 */
@IgnoreExtraProperties
public class UserDataObject extends UserObject {

    public String userName;

    public UserDataObject() {
        super();
        userName = "";
    }

    public UserDataObject(String date, String fileId, String downloadUrl, String userName) {
        super(date, fileId, downloadUrl); //FileId = Image
        this.userName = userName;
    }

}
