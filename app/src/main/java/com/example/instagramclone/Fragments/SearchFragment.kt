package com.example.instagramclone.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.UserAdapter
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter

        val searchEditText = view.findViewById<EditText>(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isEmpty()) {
                    recyclerView?.visibility = View.GONE
                    retrieveUser()
                } else {
                    recyclerView?.visibility = View.VISIBLE
                    searchUser(searchText.toLowerCase())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            }else {
                false
            }

        }

        return view
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .orderByChild("fullname")
            .startAt(input).endAt(input + "\uf0ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUser?.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        mUser?.add(user)
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun retrieveUser() {
        val userRef = FirebaseDatabase.getInstance().getReference().child("User")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUser?.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        mUser?.add(user)
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun performSearch() {
        val searchText = view?.findViewById<EditText>(R.id.search_edit_text)?.text.toString().trim()
        if (!searchText.isEmpty()) {
            recyclerView?.visibility = View.VISIBLE
            searchUser(searchText.toLowerCase())
        }
    }
}
