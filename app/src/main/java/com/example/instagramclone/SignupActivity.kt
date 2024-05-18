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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val sign_in_link_btn = findViewById<TextView>(R.id.sign_in_link_btn)
        val signUp_btn = findViewById<Button>(R.id.signUp_btn)
        sign_in_link_btn.setOnClickListener(){
            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        signUp_btn.setOnClickListener{
            createAccount()
        }
    }


    private fun createAccount() {
        val fullName = findViewById<EditText>(R.id.fullname_text).text.toString()
        val userName = findViewById<EditText>(R.id.userName).text.toString()
        val email = findViewById<EditText>(R.id.email_signup).text.toString()
        val password = findViewById<EditText>(R.id.passwordSignUp).text.toString()
        val repassword = findViewById<EditText>(R.id.rewritepassword).text.toString()

        when {
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Write Full Name", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "Write UserName", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Write Email", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Write Password", Toast.LENGTH_SHORT).show()
            password != repassword -> Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            else -> {
                val progressDialog = ProgressDialog(this@SignupActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserInfo(fullName, userName, email, progressDialog)
                        } else {
                            val message = task.exception!!.message ?: "Unknown error occurred"
                            Toast.makeText(this@SignupActivity, message, Toast.LENGTH_SHORT).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }


    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()
        userMap["uid"]= currentUserId
        userMap["fullname"]= fullName.toLowerCase()
        userMap["username"]= userName.toLowerCase()
        userMap["email"]= email
        userMap["bio"]= "Hey I'm using Instagram clone App."
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagram-80510.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=90290d32-25dd-4214-a5d8-6edd64358ac3"
        usersRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener{
                task->
                if(task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account has been created Successfully", Toast.LENGTH_SHORT).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId)
                        .setValue(true)




                    val intent = Intent(this@SignupActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}