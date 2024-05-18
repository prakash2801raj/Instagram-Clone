package com.example.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val sign_up_link_btn = findViewById<TextView>(R.id.sign_up_link_btn)
        sign_up_link_btn.setOnClickListener(){
            val intent = Intent(this,SignupActivity::class.java)
            startActivity(intent)
            finish()
        }

        val login_btn = findViewById<Button>(R.id.login_btn)
        login_btn.setOnClickListener {
            loginUser()
        }

    }

    private fun loginUser() {
        val userName = findViewById<EditText>(R.id.username_signin).text.toString()
        val password = findViewById<EditText>(R.id.password_login).text.toString()

        when{
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "Write Email", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Write Password", Toast.LENGTH_SHORT).show()


            else ->{
                val progressDialog = ProgressDialog(this@SignInActivity)
                progressDialog.setTitle("logIn")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.signInWithEmailAndPassword(userName,password).addOnCompleteListener{task->
                    if(task.isSuccessful){
                        progressDialog.dismiss()

                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else{
                        val message = task.exception!!.toString()
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                        progressDialog.dismiss()
                    } 
                }
            }
        }
    }

    override fun onStart(){
        super.onStart()

        if(FirebaseAuth.getInstance().currentUser!=null){
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }else{

        }
    }
}