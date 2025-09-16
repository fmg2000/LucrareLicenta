package com.example.ocrapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var editTextUserName: TextInputEditText? = null
    private var editTextPassword: TextInputEditText? = null
    private var mAuth: FirebaseAuth? = null // declare farebase instance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance() //firebase init
        editTextUserName = findViewById(R.id.loginUsernameEditText)
        editTextPassword = findViewById(R.id.loginUserpasswoardEditText)
        findViewById<View>(R.id.buttonRegister).setOnClickListener(this)
        findViewById<View>(R.id.buttonLogin).setOnClickListener(this)
        findViewById<View>(R.id.login).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.buttonRegister) {
            hideKeyboard(v)
            goToRegister()
        }
        if (v.id == R.id.buttonLogin) {
            hideKeyboard(v)
            if (editTextUserName!!.getText().toString().isEmpty() || editTextPassword!!.getText().toString().isEmpty())
                Toast.makeText(this, "Field is empty", Toast.LENGTH_SHORT).show() else signIn(editTextUserName!!.getText().toString(), editTextPassword!!.getText().toString())
        }
        if (v.id == R.id.login) {
            hideKeyboard(v)
        }
    }

    private fun signIn(email: String, password: String) {
        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this@LoginActivity, "Successful", Toast.LENGTH_SHORT).show()
                        val user = mAuth!!.currentUser
                        goToMain()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@LoginActivity, "Email or Password incorrect", Toast.LENGTH_SHORT).show()
                        editTextUserName!!.setText("")
                        editTextPassword!!.setText("")
                    }
                }
    }

    private fun goToRegister() {
        val intent = Intent(this, RegisterActivity::class.java) // go - to
        startActivity(intent) // start intent
        finish()
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java) // go - to
        startActivity(intent) // start intent
        finish()
    }

    private fun hideKeyboard(v: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }
}