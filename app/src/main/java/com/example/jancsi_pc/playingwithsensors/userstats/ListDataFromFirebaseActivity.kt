package com.example.jancsi_pc.playingwithsensors.userstats

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.example.jancsi_pc.playingwithsensors.utils.firebase.FirebaseUtil
import com.example.jancsi_pc.playingwithsensors.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_list_data_from_firebase.*
import java.lang.Exception
import java.util.concurrent.CountDownLatch

/**
 * Activity that lists data from user_stats collection (Firebase) using a RecyclerView
 *
 * @author Nemeth Krisztian-Miklos
 */
class ListDataFromFirebaseActivity : AppCompatActivity() {
    private lateinit var mAdapter: ListDataAdapter
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_data_from_firebase)

        //gathering data
        //first attempt is without asynctask... (with asynctask it requires the usage of changeData() and notifyDatasetChanged())
        //var data = FirebaseUtil.queryUserDataFromFirebase()

        //Log.d(TAG,"Second attempt")
        var data = queryUserDataFromFireStore()

        //creating recycleView
        mRecyclerView = rv_list_data
        mRecyclerView.setHasFixedSize(true) //better performance
        mRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAdapter = ListDataAdapter(data, this)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.addItemDecoration(ItemDividerDecoration(this))
        //(rv_list_data.adapter as ListDataAdapter).notifyDataSetChanged()
    }

    companion object {
        const val TAG: String = "ListDataFromFirebaseAct"

        /**
         * Function that query the given user's statistics from Firebase/Firestore
         *
         * @param userId the ID of the user whose data needs to be returned
         * @return a FirebaseUserData object containing the resulting data,
         *          or null if the user does not exist
         * @author Krisztian-Miklos Nemeth
         */
        fun queryOneUsersDataFromFireStore(userId: String): FirebaseUserData? {
            Log.d(TAG, "queryUserDataFromFirebase")
            var doc = FirebaseFirestore.getInstance().collection(FirebaseUtil.FIRESTORE_STATS_NODE).document(userId)
            var data: FirebaseUserData? = null
            var done = CountDownLatch(1)
            doc.get()
                    .addOnSuccessListener { document ->
                        //Log.d(TAG,"Result: " + result.documents.toString())
                        if (document != null) {
                            Log.d(TAG, document.id + " => " + document.data)
                            var obj = FirebaseUserData(document.get("email").toString(), document.id,
                                    document.get("devices").toString().replace("[", "", false).replace("]", "", false),
                                    document.get("sessions").toString().toInt(),
                                    document.get("steps").toString().toInt(),
                                    document.get("files").toString().toInt())
                            //Log.d(TAG,obj.toString())
                            data = obj
                        } else {
                            Log.d(TAG, "No such document")
                            throw Exception("No such document in firebase")
                        }
                        done.countDown()
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                        done.countDown()
                    }
            done.await()
            return data
        }
    }

    private fun queryUserDataFromFireStore(): ArrayList<FirebaseUserData> {
        Log.d(TAG, "queryUserDataFromFirebase")
        var col = FirebaseFirestore.getInstance().collection(FirebaseUtil.FIRESTORE_STATS_NODE)
        var data = arrayListOf<FirebaseUserData>()

        col.get()
                .addOnSuccessListener { result ->
                    //Log.d(TAG,"Result: " + result.documents.toString())
                    for (document in result) {
                        //Log.d(TAG, document.id + " => " + document.data)
                        var obj = FirebaseUserData(document.get("email").toString(), document.id,
                                document.get("devices").toString().replace("[", "", false).replace("]", "", false),
                                document.get("sessions").toString().toInt(),
                                document.get("steps").toString().toInt(),
                                document.get("files").toString().toInt())
                        //Log.d(TAG,obj.toString())
                        data.add(obj)
                    }
                    mAdapter.changeItems(data)
                    mRecyclerView.adapter = mAdapter
                    (mRecyclerView.adapter as ListDataAdapter).notifyDataSetChanged()
                    //Log.d(TAG,"NotifiedDatasetChanged")
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }

        return data
    }
}