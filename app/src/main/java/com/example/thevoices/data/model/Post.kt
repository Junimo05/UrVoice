package com.example.thevoices.data.model

class Post {
    var id :Int = 0
    var userId :Int = 0
    var audioId :Int = 0
    var audio :Audio? = null
    var title :String? = null
    var body :String? = null
    var date :String? = null
    var likes :Int = 0
    var comments :Int = 0
    var shares :Int = 0
    var views :Int = 0
}