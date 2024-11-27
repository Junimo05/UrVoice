package com.example.urvoices.ui._component.MoreAction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.urvoices.R
import com.example.urvoices.data.service.FirebaseBlockService

@Composable
fun DropDownMenu(
    expand: MutableState<Boolean>,
    actions: List<DropDownAction>,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expand.value,
        onDismissRequest = {
            expand.value = false
        },
        offset = offset,
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .shadow(1.dp, shape = MaterialTheme.shapes.small)
    ) {
        for (action in actions) {
            DropdownMenuItem(
                text = {
                    Text(
                    text = action.title,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = MaterialTheme.typography.labelMedium.fontWeight
                    )
                ) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = action.icon),
                        contentDescription = action.title,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    action.action()
                },
            )
        }
    }
}

fun UserAction(

    isBlock: Boolean,
    blockUser: () -> Unit
): List<DropDownAction> {
    return if (isBlock) {
        listOf(
            DropDownAction(
                icon = R.drawable.ic_visibility_on,
                title = "Unblock User",
                action = {
                    blockUser()
                }
            )
        )
    } else {
        listOf(
            DropDownAction(
                icon = R.drawable.ic_visibility_off,
                title = "Block User",
                action = {
                    blockUser()
                }
            )
        )
    }
}

fun PostAction(
    isCurrentUserPost: Boolean,
    addToPlaylist: () -> Unit,
    changeInfo: () -> Unit,
    goToPost: (() -> Unit)? = null,
    goToUser: (() -> Unit)? = null,
    copyLink: () -> Unit,
    isSaved: Boolean,
    savePost: () -> Unit,
    deletePost: (() -> Unit)? = null,
    blockInfo: MutableState<String>,
    blockUser: () -> Unit,
    unblockUser: () -> Unit
): List<DropDownAction> {

    return if (blockInfo.value != FirebaseBlockService.BlockInfo.NO_BLOCK) { // Block = true
        val actions = mutableListOf<DropDownAction>()

        goToUser?.let {
            actions.add(
                DropDownAction(
                    icon = R.drawable.ic_actions_user,
                    title = "Go to User",
                    action = it
                )
            )
        }

        if (blockInfo.value != FirebaseBlockService.BlockInfo.BLOCK) { // This user is blocked by current user
            actions.add(
                DropDownAction(
                    icon = R.drawable.ic_visibility_on,
                    title = "Unblock User",
                    action = unblockUser
                )
            )
        }

        actions
    } else {
        val actions = mutableListOf<DropDownAction>()
        goToPost?.let {
            actions.add(
                DropDownAction(
                    icon = R.drawable.ic_actions_new_window,
                    title = "Go to Post",
                    action = goToPost
                ),
            )
        }

        goToUser?.let {
            actions.add(
                DropDownAction(
                    icon = R.drawable.ic_actions_user,
                    title = "Go to User",
                    action = it
                )
            )
        }
        actions.addAll(
            listOf(
                DropDownAction(
                    icon = R.drawable.playlist_add_svgrepo_com,
                    title = "Add To Playlist",
                    action = addToPlaylist
                ),
                DropDownAction(
                    icon = R.drawable.ic_actions_add_ribbon,
                    title = "Save Post",
                    action = savePost
                ),
                DropDownAction(
                    icon = R.drawable.ic_link,
                    title = "Share",
                    action = copyLink
                )
            )
        )


        // Check Post Owner
        if (!isCurrentUserPost) {
            actions.add(
                DropDownAction(
                    icon = R.drawable.ic_visibility_off,
                    title = "Block User",
                    action = blockUser
                )
            )
        } else {
            actions.add(
                DropDownAction(
                    icon = R.drawable.pencil_svgrepo_com,
                    title = "Edit",
                    action = changeInfo
                )
            )
            if(deletePost != null) {
                actions.add(
                    DropDownAction(
                        icon = R.drawable.delete_2_svgrepo_com,
                        title = "Delete Post",
                        action = deletePost
                    )
                )
            }
        }
        actions
    }
}

data class DropDownAction(
    val icon: Int,
    val title: String,
    val action: () -> Unit
)