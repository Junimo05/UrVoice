package com.example.urvoices.utils




/*
	FollowState
*/
data object FollowState {
	const val FOLLOW = "FOLLOW"
	const val UNFOLLOW = "UNFOLLOW"
	const val REQUEST_FOLLOW = "REQUEST_FOLLOW"
}

/*
	NOTIFICATION
*/
data object TypeNotification {
	const val FOLLOW_USER = "FOLLOW_USER"
	const val REQUEST_FOLLOW = "REQUEST_FOLLOW"
	const val LIKE_POST = "LIKE_POST"
	const val LIKE_COMMENT = "LIKE_COMMENT"
	const val COMMENT_POST = "COMMENT_POST"
	const val REPLY_COMMENT = "REPLY_COMMENT"
}

data object MessageNotification {
	const val FOLLOW_USER = "followed you"
	const val REQUEST_FOLLOW = "requested to follow you"
	const val LIKE_POST = "liked your post"
	const val LIKE_COMMENT = "liked your comment"
	const val COMMENT_POST = "commented on your post"
	const val REPLY_COMMENT = "replied to your comment"
}

data object MessageNotificationAfterAction {
	const val ACCEPT_FOLLOW_USER = "is your follower now"
}