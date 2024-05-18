package com.example.instagramclone.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.AccountSettingActivity
import com.example.instagramclone.Adapter.MyImagesAdapter
import com.example.instagramclone.Model.Post
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Collections

class ProfileFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    var postList: List<Post>?= null
    var myImagesAdapter: MyImagesAdapter?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            profileId = pref.getString("profileId", "none").toString()
        }
        if (profileId == firebaseUser.uid) {
            binding.editAccountSettingsBtn.text = "Edit Profile"
        } else if(profileId!=firebaseUser.uid){
            checkFollowAndFollowingButtonStatus()
        }

        var recyclerViewUploadImages: RecyclerView
        recyclerViewUploadImages = view.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImages.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImagesAdapter = context?.let{ MyImagesAdapter(it, postList as ArrayList<Post>)}
        recyclerViewUploadImages.adapter = myImagesAdapter

        binding.editAccountSettingsBtn.setOnClickListener {
            val getButtonText = view?.findViewById<Button>(R.id.edit_account_settings_btn)?.text.toString()

            when{
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingActivity::class.java))
                getButtonText =="Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).setValue(true)
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).setValue(true)
                    }
                }

                getButtonText =="Following" -> {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).removeValue()
                    }
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).removeValue()
                    }
                }
            }
        }

        getFollowers()
        getFollowings()
        userInfo()
        myPhotos()
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1)
                .child("Following")
        }
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(profileId).exists()) {
                    binding.editAccountSettingsBtn.text = "Following"
                } else {
                    binding.editAccountSettingsBtn.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")
        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.totalFollowers.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun getFollowings() {
        val followingRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.totalFollowing.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun myPhotos(){
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    (postList as ArrayList<Post>).clear()

                    for(p0 in snapshot.children){
                        val post = snapshot.getValue(Post::class.java)
                        if (post!!.getPublisher().equals(profileId)){
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        myImagesAdapter!!.notifyDataSetChanged()

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.findViewById<CircleImageView>(R.id.pro_image_profile_frag))
                    view?.findViewById<TextView>(R.id.profile_fragment_username)?.text = user!!.getUsername()
                    view?.findViewById<TextView>(R.id.full_name_profile_frag)?.text = user!!.getFullname()
                    view?.findViewById<TextView>(R.id.bio_profile_frag)?.text = user!!.getBio()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
