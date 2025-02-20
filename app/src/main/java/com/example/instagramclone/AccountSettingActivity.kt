package com.example.instagramclone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.instagramclone.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView

class AccountSettingActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var checker =""
    private var myUrl = ""
    private var imageUri: Uri ?=null
    private var storageProfilePicRef: StorageReference?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("profile Picture")

        val logout_profile_btn = findViewById<Button>(R.id.logout_profile_btn)
        logout_profile_btn.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        findViewById<TextView>(R.id.change_image_text_btn).setOnClickListener {
            checker="clicked"
            CropImage.activity().setAspectRatio(1,1)
                .start(this@AccountSettingActivity)
        }

        val saveInfo = findViewById<ImageView>(R.id.save_infor_profile_btn)
        saveInfo.setOnClickListener{
            if(checker=="clicked"){
                uploadImageAndUpdateInfo()
            }else{
                updateUserInfoOnly()
            }
        }
        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== Activity.RESULT_OK && data!=null){
            val result =  CropImage.getActivityResult(data)
            imageUri = result.uri
            findViewById<CircleImageView>(R.id.profile_image_view_profile_frag).setImageURI(imageUri)
        }
    }

    private fun updateUserInfoOnly() {
        if(TextUtils.isEmpty(findViewById<EditText>(R.id.full_name_profile)?.text.toString())){
            Toast.makeText(this, "Please write full name first.", Toast.LENGTH_SHORT).show()
        }else if(TextUtils.isEmpty(findViewById<EditText>(R.id.username_profile_frag)?.text.toString())){
            Toast.makeText(this, "Please write Username first.", Toast.LENGTH_SHORT).show()
        }else if(TextUtils.isEmpty(findViewById<EditText>(R.id.bio_profile_frag)?.text.toString())){
            Toast.makeText(this, "Please bio first.", Toast.LENGTH_SHORT).show()
        }else {
            val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
            val userMap = HashMap<String, Any>()
            userMap["fullname"] =
                findViewById<EditText>(R.id.full_name_profile)?.text.toString().lowercase()
            userMap["username"] =
                findViewById<EditText>(R.id.username_profile_frag)?.text.toString().lowercase()
            userMap["bio"] =
                findViewById<EditText>(R.id.bio_profile_frag)?.text.toString().lowercase()

            userRef.child(firebaseUser.uid).updateChildren(userMap)
            Toast.makeText(this, "Account Information has been Updated Successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@AccountSettingActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(findViewById<CircleImageView>(R.id.profile_image_view_profile_frag))
                    findViewById<TextView>(R.id.profile_fragment_username)?.setText(user.getUsername())
                    findViewById<TextView>(R.id.full_name_profile_frag)?.setText(user.getFullname())
                    findViewById<TextView>(R.id.bio_profile_frag)?.setText(user.getBio())
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun uploadImageAndUpdateInfo() {
        val progressBar = ProgressDialog(this)
        progressBar.setTitle("Account Settings")
        progressBar.setMessage("Please wait, we are updating your profile...")
        progressBar.show()
        when {
            imageUri == null -> Toast.makeText(this, "Please select Image first.", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(findViewById<EditText>(R.id.full_name_profile)?.text.toString()) ->
                Toast.makeText(this, "Please write full name first.", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(findViewById<EditText>(R.id.username_profile_frag)?.text.toString()) ->
                Toast.makeText(this, "Please write Username first.", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(findViewById<EditText>(R.id.bio_profile_frag)?.text.toString()) ->
                Toast.makeText(this, "Please bio first.", Toast.LENGTH_SHORT).show()
            else -> {


                val fileref = storageProfilePicRef!!.child(firebaseUser.uid+".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileref.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (task.isSuccessful){
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri>{ task ->
                    if(task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()
                        val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] =
                            findViewById<EditText>(R.id.full_name_profile)?.text.toString().lowercase()
                        userMap["username"] =
                            findViewById<EditText>(R.id.username_profile_frag)?.text.toString().lowercase()
                        userMap["bio"] =
                            findViewById<EditText>(R.id.bio_profile_frag)?.text.toString().lowercase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Account Information has been Updated Successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@AccountSettingActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        progressBar.dismiss()
                    }
                })
            }
        }
    }
}
