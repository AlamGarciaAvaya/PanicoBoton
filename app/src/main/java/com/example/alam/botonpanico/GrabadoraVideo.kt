package com.example.alam.botonpanico

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_grabadora_video.*

class GrabadoraVideo : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if (null == savedInstanceState) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, Camera2VideoFragment.newInstance())
                    .commit()
        }
    }

}
