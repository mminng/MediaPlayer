package com.easyfun.mediaplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.add(R.id.test_content, TestFragment())
        ft.commit()
    }

}