package com.example.instagramclone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri?=null
    private var storagePostPicRef: StorageReference?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        findViewById<ImageView>(R.id.save_new_post_btn).setOnClickListener{ uploadImage() }

        CropImage.activity().setAspectRatio(2,1)
            .start(this@AddPostActivity)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== Activity.RESULT_OK && data!=null){
            val result =  CropImage.getActivityResult(data)
            imageUri = result.uri
            findViewById<ImageView>(R.id.image_post).setImageURI(imageUri)
        }
    }
    private fun uploadImage(){
        when{
            imageUri == null -> Toast.makeText(this, "Please select the image", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(findViewById<EditText>(R.id.description_post).text.toString())-> Toast.makeText(
                this,
                "Please write the caption",
                Toast.LENGTH_SHORT
            ).show()
            else ->{
                val progressBar = ProgressDialog(this)
                progressBar.setTitle("Adding New Post")
                progressBar.setMessage("Please wait, we are adding...")
                progressBar.show()
                val fileref = storagePostPicRef!!.child(System.currentTimeMillis().toString() +".jpg")

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

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = ref.push().key
                        val postMap = HashMap<String, Any>()
                        postMap["posts"] = postId!!
                        postMap["description"] = findViewById<EditText>(R.id.description_post)?.text.toString().lowercase()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this, "Post has been uploaded Successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
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