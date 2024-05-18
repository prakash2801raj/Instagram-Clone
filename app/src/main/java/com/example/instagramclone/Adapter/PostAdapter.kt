package com.example.instagramclone.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Model.Post
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.instagramclone.CommentsActivity
import com.example.instagramclone.MainActivity
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class PostAdapter
    (private val mContext: Context, private val mPost:List<Post>):RecyclerView.Adapter<PostAdapter.ViewHolder>(){

        private var firebaseUser: FirebaseUser?=null

        inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
            var profieImage: CircleImageView
            var postImage: ImageView
            var likeButton: ImageView
            var commentButton: ImageView
            var saveButton: ImageView
            var userName: TextView
            var likes: TextView
            var publishers: TextView
            var descriptions: TextView
            var comments: TextView
            init {
                profieImage = itemView.findViewById(R.id.user_profile_image_search)
                postImage = itemView.findViewById(R.id.post_image_home)
                likeButton = itemView.findViewById(R.id.post_image_like_btn)
                commentButton = itemView.findViewById(R.id.post_image_comment_btn)
                saveButton = itemView.findViewById(R.id.post_save_btn)
                userName = itemView.findViewById(R.id.user_name_search)
                likes = itemView.findViewById(R.id.likes)
                publishers = itemView.findViewById(R.id.publisher)
                descriptions = itemView.findViewById(R.id.description)
                comments = itemView.findViewById(R.id.comments)
                var likedByCurrentUser: Boolean = false

            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        if(post.getDescription().equals("")){
            holder.descriptions.visibility = View.GONE
        }else{
            holder.descriptions.visibility = View.VISIBLE
            holder.descriptions.setText(post.getDescription())
        }

        publisherInfo(holder.profieImage, holder.userName, holder.publishers, post.getPublisher())

        isLikes(post.getPostid(),holder.likeButton)

        numberOfLikes(holder.likes, post.getPostid())
        numberOfComments(holder.comments, post.getPostid())


        holder.likeButton.setOnClickListener{
            if(holder.likeButton.tag == "Like"){
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .setValue(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            holder.likeButton.tag = "Liked"
                            holder.likeButton.setImageResource(R.drawable.heart_clicked)
                        }
                    }
            } else {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            holder.likeButton.tag = "Like"
                            holder.likeButton.setImageResource(R.drawable.heart_not_clicked)
                        }
                    }
            }


        }

        holder.commentButton.setOnClickListener{
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId",post.getPostid())
            intentComment.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.comments.setOnClickListener{
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId",post.getPostid())
            intentComment.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intentComment)
        }


    }




    private fun publisherInfo(profieImage: CircleImageView, userName: TextView, publishers: TextView, publisherID: String) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        usersRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profieImage)
                    userName.text = user!!.getUsername()
                    publishers.text = user!!.getFullname()



                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun isLikes(postid: String, likeButton: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val LikesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        LikesRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(firebaseUser!!.uid).exists()){
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                }else{
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Liked" // This should be "Like"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun numberOfLikes(likes: TextView, postid: String) {

        val LikesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        LikesRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    likes.text = snapshot.childrenCount.toString() + " likes"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun numberOfComments(comments: TextView, postid: String) {

        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postid)

        commentsRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    comments.text = "view all" + snapshot.childrenCount.toString() + " comments"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}