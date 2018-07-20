package com.example.alam.hdi

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_ajustes.*

class AjustesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)
        val myPreferences = "myPrefs"
        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        obtenerajustes()
        guardarajustes_btn.setOnClickListener {
            guardarajustes()
        }
        switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val editor = sharedPreferences.edit()
                editor.putString("debug", 1.toString())
                Log.d("Valores", "Check Si")
                editor.apply()
            } else {
                val editor = sharedPreferences.edit()
                editor.putString("debug", 0.toString())
                Log.d("Valores", "Check No")
                editor.apply()
            }
        }
    }


    fun guardarajustes() {
        val myPreferences = "myPrefs"
        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("displayname", displayname_txt.text.toString())
        editor.putString("username", user_txt.text.toString())
        editor.putString("host", host_txt.text.toString())
        editor.putString("puerto", puerto_txt.text.toString())
        editor.putString("numero", num_txt.text.toString())
        editor.apply()
        finish()
    }


    fun obtenerajustes() {
        var myPreferences = "myPrefs"
        var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        var displayname = sharedPreferences.getString("displayname", "John Doe")
        var username = sharedPreferences.getString("username", "1234")
        var host = sharedPreferences.getString("host", "amv.collaboratory.avaya.com")
        var puerto = sharedPreferences.getString("puerto", "443")
        var numero = sharedPreferences.getString("numero", "2681322102")
        var debug = sharedPreferences.getString("debug", "1")
        displayname_txt.setText(displayname)
        user_txt.setText(username)
        host_txt.setText(host)
        puerto_txt.setText(puerto)
        num_txt.setText(numero)
        Log.d("Valores", "Key1 $displayname")
        Log.d("Valores", "Key2 $username")
        Log.d("Valores", "Key3 $host")
        Log.d("Valores", "Key4 $puerto")
        Log.d("Valores", "Key5 $numero")
        Log.d("Valores", "Key6 $debug")
        when (Integer.parseInt(debug)) {
            1 -> switch1.isChecked = true
            else -> switch1.isChecked = false
        }

    }


}
