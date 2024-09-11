package com.example.thevoices.utils

import com.example.thevoices.R
import com.example.thevoices.presentations.components.Interaction

fun Post_Interactions(
    //interactions data
): List<Interaction> {
    return listOf(
        Interaction(
            icon = R.drawable.ic_actions_star,
            count = 2,
            contentDescription = "Star",
            action = { /*TODO*/ }
        ),
        Interaction(
            icon = R.drawable.ic_comment,
            count = 204,
            contentDescription = "Comment",
            action = { /*TODO*/ }
        )
    )
}

fun Comment_Interactions(
    //interactions data
) : List<Interaction> {
    return listOf(
        Interaction(
            icon = R.drawable.ic_actions_heart,
            count = 2, // number of likes
            contentDescription = "Like",
            action = {

            }
        ),
        Interaction(
            icon = R.drawable.ic_comment,
            count = 2, // number of comments
            contentDescription = "Comment",
            action = {

            }
        ),
    )
}