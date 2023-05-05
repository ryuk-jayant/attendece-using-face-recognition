package com.example.attendence_nfsu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
//import android.widget.AdapterView.OnItemSelectedListener;

class selectingclassforattendence : AppCompatActivity(){
    var infoaboutuser= arrayOf<String>("Select school","select course","select subject")
    var displayname:String?=null//info we need in button click event


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectingclassforattendence)
        val Heading=findViewById<TextView>(R.id.Heading)
        displayname=intent.getStringExtra("displayname")
        Heading.text="Welcome $displayname"//setting heading of page

        val school=resources.getStringArray(R.array.array_school)//importing array

        val schooladapter=ArrayAdapter(this,android.R.layout.simple_spinner_item,school)
        val spinnerschool=findViewById<Spinner>(R.id.spinner_school)
        schooladapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerschool.adapter=schooladapter

        spinnerschool.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{//implementing interface in an anonymous class
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            //Toast.makeText(this@selectingclassforattendence, school[p2]+" is your school", Toast.LENGTH_SHORT).show()
            infoaboutuser[0]=school[p2]
            var temp=R.array.array_default
            when(school[p2]){
                    "SCSDFS"->temp=R.array.array_course_SCSDFS
                    "SBS"->temp=R.array.array_course_SBS
                    "SFS"->temp=R.array.array_course_SFS
                }

            val course_name=resources.getStringArray(temp)
            val courseadapter=ArrayAdapter(this@selectingclassforattendence,android.R.layout.simple_spinner_item,course_name)
            val spinnercourse=findViewById<Spinner>(R.id.spinner_course)
            courseadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnercourse.adapter=courseadapter
            spinnercourse.onItemSelectedListener=object: AdapterView.OnItemSelectedListener{//implementing anonymous object of spinnercourse
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    infoaboutuser[1]=course_name[p2]
                   // Toast.makeText(this@selectingclassforattendence, course_name[p2], Toast.LENGTH_SHORT).show()
                    var new_temp=R.array.Subjects
                    when(course_name[p2]){
                        "Btech-Mtech CSE 21-26"->new_temp=R.array.Subjects_sem4
                        "Btech-Mtech CSE 22-27"->new_temp=R.array.Subjects_sem2
                    }
                    val subjects=resources.getStringArray(new_temp)
                    val subjectadapter=ArrayAdapter(this@selectingclassforattendence,android.R.layout.simple_spinner_item,subjects)
                    val spinnersubject=findViewById<Spinner>(R.id.spinner_subject)
                    subjectadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnersubject.adapter=subjectadapter
                    spinnersubject.onItemSelectedListener=object: AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                           infoaboutuser[2]=subjects[p2]
                            Toast.makeText(this@selectingclassforattendence, subjects[p2], Toast.LENGTH_SHORT).show()
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            //interface callback
                        }
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                 //interface callback
                }
                    }

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

    }
    fun selectingfeatures(view: View){
        Toast.makeText(this@selectingclassforattendence, infoaboutuser[0]+" "+infoaboutuser[1], Toast.LENGTH_SHORT).show()
        if(infoaboutuser[0]!="Select your course"){
           if(infoaboutuser[1]!="Select your course"){
               if(infoaboutuser[2]!="Select Your Subjects"){
                   val intent = Intent(this@selectingclassforattendence, selectingfeaturesofapp::class.java)
                   intent.putExtra("displayname", displayname)
                   startActivity(intent)
               }
               else{
                   Toast.makeText(this@selectingclassforattendence, infoaboutuser[2], Toast.LENGTH_SHORT).show()
               }
           }
           else{
               Toast.makeText(this@selectingclassforattendence, infoaboutuser[1], Toast.LENGTH_SHORT).show()
           }
        }
        else{
            Toast.makeText(this@selectingclassforattendence, infoaboutuser[0], Toast.LENGTH_SHORT).show()
        }


    }
}