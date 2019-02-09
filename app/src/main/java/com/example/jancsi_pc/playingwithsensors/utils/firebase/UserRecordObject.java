package com.example.jancsi_pc.playingwithsensors.utils.firebase;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * This class is used to store Users recorded data in Firebase firestore.
 *
 * @author Mille Janos
 */
@IgnoreExtraProperties
public class UserRecordObject extends UserObject {

    public UserRecordObject(String date, String fileId, String downloadUrl) {
        super(date, fileId, downloadUrl);
    }
}
