package com.example.instagramclone.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Model.Comment
import androidx.annotation.NonNull
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.events.Publisher
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(private  val mContext: Context,
    private val mComment: MutableList<Comment>?): RecyclerView.Adapter<CommentAdapter.ViewHolder>(){
        private var firebaseUser: FirebaseUser?=null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
            val view = LayoutInflater.from(mContext).inflate(R.layout.comments_items_layout, parent, false)
            return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return mComment!!.size
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val comment = mComment!![position]
        holder.commentTv.text = comment.getComment()
        getUserInfo(holder.imageProfile, holder.usernameTv, comment.getPublisher())
    }

    private fun getUserInfo(imageProfile: CircleImageView, usernameTv: TextView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance()
            .reference.child("Users").child(publisher)
        userRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    if (user != null && !user.getImage().isNullOrEmpty()) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(imageProfile)
                    } else {
                        // Handle the case where user or image path is null or empty
                        // For example, you can load a default image
                        Picasso.get().load(R.drawable.profile).into(imageProfile)
                    }

                    usernameTv.text = user?.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }



    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        var imageProfile: CircleImageView
        var usernameTv: TextView
        var commentTv: TextView

        init{
            imageProfile = itemView.findViewById(R.id.user_profile_image_comment)
            usernameTv = itemView.findViewById(R.id.user_name_comment)
            commentTv = itemView.findViewById(R.id.comment_comment)
        }

    }

}