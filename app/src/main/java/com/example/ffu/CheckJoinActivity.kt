package com.example.ffu

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CheckJoinActivity : AppCompatActivity() {

    private lateinit var userDB: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        if (auth.currentUser != null) { // 로그인 상태
            userDB = Firebase.database.reference.child("profile").child(auth.uid.toString())
            userDB.get().addOnSuccessListener {
                for (ds in it.children) {
                    if (ds.key.toString().equals("join")) {
                        if (ds.value.toString().equals("true")) { // 회원가입 o
                            val intent = Intent(this@CheckJoinActivity, BackgroundActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else { // 회원가입 x
                            val intent = Intent(this@CheckJoinActivity, JoinActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        } else { // 로그아웃 상태
            val intent = Intent(this@CheckJoinActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}