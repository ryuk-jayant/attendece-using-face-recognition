package com.example.attendence_nfsu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class selectingfeaturesofapp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectingfeaturesofapp)
        val displayname=intent.getStringExtra("displayname")
        val heading=findViewById<TextView>(R.id.greeting)
        heading.text="Hey $displayname"

        //logout functionality
        val activity:selectingfeaturesofapp=selectingfeaturesofapp()
        val logoutbtn=findViewById<Button>(R.id.logout)
        logoutbtn.setOnClickListener{
            activity.finish()
        }
    }
}