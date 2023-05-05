package com.example.attendence_nfsu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun check(view: View) {//method for click on button
        val username=findViewById<EditText>(R.id.username)
        val uname= username.editableText.toString()

        val password=findViewById<EditText>(R.id.Pass)
        val pass= password.editableText.toString()

        val unames= listOf("jkjryuk" to "12345","ramu" to "qwertyuiop")//will be obtained by get request

        run outsideforeach@{
            unames.forEach { (name, Pass) ->
                //Toast.makeText(this, "hey! $uname", Toast.LENGTH_LONG).show()
                if (name == uname) {
                    if (Pass == pass) {
                        Toast.makeText(this, "welcome $uname", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, selectingclassforattendence::class.java)
                        intent.putExtra("displayname", uname)
                        startActivity(intent)//new activity call via intent
                        return@outsideforeach
                    }
                    Toast.makeText(this, "wrong Password dear $uname", Toast.LENGTH_LONG).show()
                }

            }
        }
        }

    fun selectingfeatures(view: View) {}
}