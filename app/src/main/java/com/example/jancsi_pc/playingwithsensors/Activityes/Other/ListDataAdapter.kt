package com.example.jancsi_pc.playingwithsensors.Activityes.Other

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jancsi_pc.playingwithsensors.R
import com.example.jancsi_pc.playingwithsensors.Utils.FirebaseUserData
import kotlinx.android.synthetic.main.data_list_item.view.*
import java.util.*


/**
 * An implementation of RecyclerView.Adapter meant for the ListDataFromFirebaseActivity
 *
 * @author Nemeth Krisztian-Miklos
 */
class ListDataAdapter(var items: ArrayList<FirebaseUserData>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvUserEmail.text = items[position].mEmail
        holder.tvUserId.text = items[position].mUserId
        holder.tvDevices.text = items[position].deviceId
        holder.tvSteps.text = items[position].mNumberOfStepsMade.toString()
        holder.tvSessions.text = items[position].mNumberOfSessions.toString()
        holder.tvFiles.text = items[position].mNumberOfRawDataFiles.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.data_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Function meant to set the data set that is inflated
     *
     *  @param items the new data set
     * @author Nemeth Krisztian-Miklos
     */
    fun changeItems(items: ArrayList<FirebaseUserData>) {
        this.items = items
    }

}


/**
 * An implementation of RecyclerView.ViewHolder used to inflate the elements of the RecyclerView
 * used in ListDataFromFirebaseActivity
 * @author Nemeth Krisztian-Miklos
 */
class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvUserEmail: TextView = view.tv_userEmail
    val tvUserId: TextView = view.tv_userId
    val tvDevices: TextView = view.tv_deviceId
    val tvSteps: TextView = view.tv_number_of_steps_made
    val tvSessions: TextView = view.tv_number_of_sessions
    val tvFiles: TextView = view.tv_number_of_records
}