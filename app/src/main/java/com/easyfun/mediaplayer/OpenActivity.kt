package com.easyfun.mediaplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class OpenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open)
        findViewById<Button>(R.id.open).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}