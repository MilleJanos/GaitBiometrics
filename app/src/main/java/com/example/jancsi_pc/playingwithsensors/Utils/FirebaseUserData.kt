package com.example.jancsi_pc.playingwithsensors.Utils

/**
 * A class that stores data from Firebase/Firestore contained in the user_stats collection in order to be shown in a RecyclerView
 *
 * @property[email] user email
 * @property[userId] id of the user
 * @property[deviceId] a string containing the deviceID(s)
 * @property[mNumberOfSessions] the number of different sessions
 * @property[mNumberOfStepsMade] the number of steps made
 * @property[mNumberOfRawDataFiles] the number of files the user has stored
 * @constructor creates an object with the given parameters
 * @author Krisztian-Miklos Nemeth
 */
class FirebaseUserData(val email: String, val userId: String, val deviceId: String, numberOfSessions: Int, numberOfStepsMade: Int, numberOfRawDataFiles: Int) {
    internal var mEmail = email
        get() = field
        private set
    internal var mUserId = userId
        get() = field
        private set
    internal var mDeviceId = deviceId
        get() = field
        private set
    internal var mNumberOfSessions: Int = numberOfSessions
        get() = field
        private set
    internal var mNumberOfStepsMade: Int = numberOfStepsMade
        get() = field
        private set
    internal var mNumberOfRawDataFiles: Int = numberOfRawDataFiles
        get() = field
        private set

    override fun toString(): String {
        return "FirebaseUserData(email='$email', userId='$userId', deviceId='$deviceId')"
    }

}