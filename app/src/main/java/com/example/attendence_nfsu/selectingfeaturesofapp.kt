package com.example.attendence_nfsu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlin.system.exitProcess

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
            exitProcess(0);
        }
    }

    fun launchcamera(view: View) {
        val infoaboutuser=intent.getStringArrayExtra("infoaboutuser")
        //Toast.makeText(this@selectingfeaturesofapp, (infoaboutuser?.get(0) ?: "data1") + (infoaboutuser?.get(1) ?: "data2"), Toast.LENGTH_SHORT).show()
        val intent = Intent(this@selectingfeaturesofapp, takingattendencebycamera::class.java)
        intent.putExtra("infoaboutuser",infoaboutuser)
        startActivity(intent)
    }
}