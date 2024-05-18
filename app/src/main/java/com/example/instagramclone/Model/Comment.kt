package com.example.instagramclone.Model

class Comment {
    private var comment: String = ""
    private var publisher: String = ""
    private var postid: String = ""

    constructor()

    constructor(comment: String, publisher: String, postid: String) {
        this.comment = comment
        this.publisher = publisher
        this.postid = postid
    }

    fun getComment(): String {
        return comment
    }

    fun setComment(comment: String) {
        this.comment = comment
    }

    fun getPublisher(): String {
        return publisher
    }

    fun setPublisher(publisher: String) {
        this.publisher = publisher
    }

    fun getPostId(): String {
        return postid
    }

    fun setPostId(postid: String) {
        this.postid = postid
    }
}
