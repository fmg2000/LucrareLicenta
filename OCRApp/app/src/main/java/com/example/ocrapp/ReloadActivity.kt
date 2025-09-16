package com.example.ocrapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ReloadActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reload)
        mAuth = FirebaseAuth.getInstance() //firebase init
        val reloadAnimation: Thread = object : Thread() {
            override fun run() {
                try {
                    super.run()
                    sleep(2000) //Delay of 10 seconds
                } catch (e: Exception) {
                } finally {
                    val currentUser = mAuth!!.currentUser // verify Login  is on or off
                    if (currentUser != null) goToMain() else goToLogin()
                }
            }
        }
        reloadAnimation.start()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java) // go - to
        startActivity(intent) // start intent
        finish() // delete history activity
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java) // go - to
        startActivity(intent) // start intent
        finish()
    }
}
