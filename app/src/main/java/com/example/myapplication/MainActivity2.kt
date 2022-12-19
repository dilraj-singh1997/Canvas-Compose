package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setContent {
            Text(text = "text")
            Log.d("TAG", "onCreate: lmao")
        }

//        println(intent?.extras?.getParcelable<Bundle>("tt")?.getParcelable<T>("tt2")?.name ?: "no value")
    }
}