package com.example.urvoices.ui._component

import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.R

@Composable
fun TopBarBackButton(
    navController: NavController,
    title : String = "",
    endIcon: Int? = null,
    endIconAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxWidth().then(modifier)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
    ) {
        // Title
        Text(
            text = title,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontStyle = FontStyle.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.align(Alignment.Center)
        )

        // Back Button
        IconButton(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // End Icon
        endIcon?.let {
            IconButton(
                onClick = {
                    endIconAction()
                },
                modifier = Modifier.align(Alignment.CenterEnd).size(36.dp).padding(end = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = endIcon),
                    contentDescription = "End Icon"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarBackButtonPreview() {
    val navController = rememberNavController()
    TopBarBackButton(navController, title = "Title")
}