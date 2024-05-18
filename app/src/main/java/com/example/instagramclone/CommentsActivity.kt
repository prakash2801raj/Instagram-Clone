package com.example.instagramclone

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.CommentAdapter
import com.example.instagramclone.Model.Comment
import com.example.instagramclone.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsActivity : AppCompatActivity() {
    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser?= null

    private var commentAdapter: CommentAdapter?= null
    private var commentList: MutableList<Comment>?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val intent = intent
        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView:RecyclerView
        recyclerView = findViewById(R.id.recycler_view_comments)
        val linearLayoutManager =  LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()

        commentAdapter = CommentAdapter(this, commentList)

        recyclerView.adapter = commentAdapter

        userInfo()

        readComments()

        findViewById<TextView>(R.id.post_comment).setOnClickListener(View.OnClickListener {
            if (findViewById<EditText>(R.id.add_comment)!!.text.toString()==""){
                Toast.makeText(this@CommentsActivity, "Write the comment", Toast.LENGTH_SHORT).show()

            }else{
                addComment()
            }
        })

    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments")
            .child("Comments")
            .child(postId)

        val commentsMap =  HashMap<String, Any>()
        commentsMap["comment"] = findViewById<EditText>(R.id.add_comment)!!.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid
        commentsRef.push().setValue(commentsMap)

        findViewById<EditText>(R.id.add_comment)!!.text.clear()
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!! .uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(findViewById<CircleImageView>(R.id.profile_image_comment))

                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun readComments(){
        val commentsRef = FirebaseDatabase.getInstance()
            .reference.child("Comments")
            .child(postId)

        commentsRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    commentList!!.clear()
                    for (p0 in snapshot.children){
                        val comment = p0.getValue(com.example.instagramclone.Model.Comment::class.java)
                        commentList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}