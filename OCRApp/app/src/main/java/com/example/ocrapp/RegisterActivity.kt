package com.example.ocrapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private var editTextEmail: TextInputEditText? = null
    private var editTextPassword: TextInputEditText? = null
    private var editTextPasswordVerify: TextInputEditText? = null
    private var mAuth: FirebaseAuth? = null // declare farebase instance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextEmail = findViewById(R.id.registerUsernameEditText)
        editTextPassword = findViewById(R.id.registerUserpasswoardEditText)
        editTextPasswordVerify = findViewById(R.id.verificationEditText)
        mAuth = FirebaseAuth.getInstance() //firebase init
        findViewById<View>(R.id.buttonRegister).setOnClickListener(this)
        findViewById<View>(R.id.buttonLogin).setOnClickListener(this)
        findViewById<View>(R.id.register).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.buttonRegister) {
            hideKeyboard(v)
            //editText
            if (editTextEmail!!.getText().toString().isEmpty() || editTextPassword!!.getText().toString().isEmpty() || editTextPasswordVerify!!.getText().toString().isEmpty()) Toast.makeText(this, "Field is empty", Toast.LENGTH_SHORT).show() else if (editTextPassword!!.getText().toString() != editTextPasswordVerify!!.getText().toString()) {
                Toast.makeText(this, "Password incorrect", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(editTextEmail!!.getText().toString(), editTextPassword!!.getText().toString())
            }
        }
        if (v.id == R.id.buttonLogin) {
            goToLogin()
        }
        if (v.id == R.id.register) {
            hideKeyboard(v)
        }
    }

    private fun createAccount(email: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth!!.currentUser
                        goToLogin()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@RegisterActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        editTextEmail!!.setText("")
                        editTextPassword!!.setText("")
                        editTextPasswordVerify!!.setText("")
                    }
                }
    }




    fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun hideKeyboard(v: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }
}