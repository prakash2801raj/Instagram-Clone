package com.example.instagramclone.Model

class Post {
    var likedByCurrentUser: Boolean = false
    private var posts: String = ""
    private var postimage: String = ""
    private var description: String = ""
    private var publisher: String = ""

    constructor()



    constructor(posts: String, postimage: String, description: String, publisher: String) {
        this.posts = posts
        this.postimage = postimage
        this.description = description
        this.publisher = publisher
    }

    fun getPostid(): String{
        return  posts
    }

    fun getPostimage(): String{
        return  postimage
    }

    fun getPublisher(): String{
        return  publisher
    }
    fun getDescription(): String{
        return  description
    }


    fun setPostid(posts: String){
        this.posts = posts
    }
    fun setPostimage(postimage: String){
        this.postimage = postimage
    }
    fun setPublisher(publisher: String){
        this.publisher = publisher
    }
    fun setDescription(description: String){
        this.description = description
    }


}