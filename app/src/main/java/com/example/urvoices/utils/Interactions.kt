package com.example.urvoices.utils

import android.util.Log
import com.example.urvoices.R
import com.example.urvoices.ui._component.Interaction

fun Post_Interactions(
    isLove: Boolean,
    loveCounts: Int,
    commentCounts: Int,
    love_act: (Boolean) -> Unit,
    comment_act: () -> Unit
): List<Interaction> {
    return listOf(
        Interaction(
            icon = if(isLove) R.drawable.ic_actions_heartfull else R.drawable.ic_actions_heart,
            iconAfterAct = R.drawable.ic_actions_star,
            count = loveCounts,
            contentDescription = "Love",
            action = {
//                Log.e("Post_Interactions", "isLove: $isLove")
                love_act(!isLove)
            }
        ),
        Interaction(
            icon = R.drawable.ic_comment,
            iconAfterAct = R.drawable.ic_comment,
            count = commentCounts,
            contentDescription = "Comment",
            action = {
                comment_act()
            }
        )
    )
}

fun Comment_Interactions(
    isLove: Boolean,
    loveCounts: Int,
    commentCounts: Int,
    love_act: (Boolean) -> Unit,
    comment_act: () -> Unit,
    reply_act: () -> Unit
) : List<Interaction> {
    return listOf(
        Interaction(
            icon = R.drawable.ic_comment,
            iconAfterAct = R.drawable.ic_comment,
            count = commentCounts, // number of comments
            contentDescription = "Comment",
            action = {
                comment_act()
            }
        ),
        Interaction(
            icon = if(isLove) R.drawable.ic_actions_heartfull else R.drawable.ic_actions_heart,
            iconAfterAct = R.drawable.ic_actions_heart,
            count = loveCounts, // number of likes
            contentDescription = "Like",
            action = {
                love_act(!isLove)
            }
        ),
        Interaction(
            icon = R.drawable.reply_svgrepo_com,
            iconAfterAct = R.drawable.reply_svgrepo_com,
            count = null,
            contentDescription = "Reply",
            action = {
                reply_act()
            }
        )
    )
}