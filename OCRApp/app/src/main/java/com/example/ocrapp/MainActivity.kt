package com.example.ocrapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(){

    private var mAuth: FirebaseAuth? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var myref: DatabaseReference
    private lateinit var pushedKey: DatabaseReference
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var receiptViewModel: ListReceiptViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        ///

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myref = database.getReference()
        pushedKey = mAuth!!.currentUser?.uid?.let { myref.child(it)}!!

        receiptViewModel = ViewModelProvider(this).get(ListReceiptViewModel::class.java)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener{ menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    goToFragmentShow()
                    true
                }
                R.id.add -> {
                    goToFragmentAdd()
                    true
                }
                R.id.listView -> {
                    goToFragmentList()
                    true
                }
                else -> false
            }
        }


        if (savedInstanceState == null) {
            goToFragmentShow()
        }

        /** Read from database */
        if (receiptViewModel.receiptList.value.isNullOrEmpty()) {
            loadDatafromFirebase()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Log-out")
                        .setMessage("Are you sure you want to continue?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            mAuth!!.signOut() // Deconectează utilizatorul din Firebase
                            goToLogin() // Navighează la ecranul de login
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun loadDatafromFirebase(){
        pushedKey.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (data in dataSnapshot.children) {
                    val id = data.key
                    val location = data.child("location").getValue().toString()
                    val sum = data.child("sum").getValue()
                    val date = data.child("date").getValue().toString()
                    val img = data.child("img").getValue().toString()
                    val imgName = data.child("imgName").getValue().toString()
                    val receipt = Receipt(id, location, sum.toString().toFloat(), date, img,imgName)
                    receiptViewModel.addStore(receipt)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun goToFragmentAdd() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, AddFragment())
                .addToBackStack(null)  // Adăugăm tranzacția la back stack pentru navigare înapoi
                .commit()
    }

    private fun goToFragmentShow() {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, ShowFragment())
                    .addToBackStack(null)  // Adăugăm tranzacția la back stack pentru navigare înapoi
                    .commit()
    }

    private fun goToFragmentList() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, ListFragment())
                .addToBackStack(null)  // Adăugăm tranzacția la back stack pentru navigare înapoi
                .commit()
    }



    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java) // go - to
        startActivity(intent) // start intent
        finish()
    }

}