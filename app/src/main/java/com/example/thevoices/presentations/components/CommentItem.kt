package com.example.thevoices.presentations.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thevoices.R
import com.example.thevoices.utils.Comment_Interactions

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CommentItem(


    //interactions data
) {
    Card{
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.person),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "User Name",
                    maxLines = 1,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_actions_more_2),
                    contentDescription = "Menu",
                    modifier = Modifier.size(24.dp)
                )
            }
            // Comment content
            Text(
                text = "This is a comment",
                modifier = Modifier.padding(16.dp),
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            )

            // Comment interaction
            InteractionRow(interactions = Comment_Interactions())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommentItemPreview() {
    CommentItem()
}