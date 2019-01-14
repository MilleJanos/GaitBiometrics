package com.example.jancsi_pc.playingwithsensors.Utils;

import android.view.View;

import com.google.firebase.firestore.IgnoreExtraProperties;


@IgnoreExtraProperties
public class UserDataObject extends UserObject{

    private String userName;

    public UserDataObject(String date, String fileId, String downloadUrl, String userName) {
        super(date,fileId,downloadUrl); //FileId = Image
        this.userName = userName;
    }

    /*private View.OnClickListener awesomeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            awesomeButtonClicked();
        }
    };*/
}
