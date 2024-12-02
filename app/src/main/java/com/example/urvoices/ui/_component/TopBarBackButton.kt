package com.example.urvoices.ui._component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urvoices.R

@Composable
fun TopBarBackButton(
    navController: NavController,
    title: String = "",
    backButtonAction: (() -> Unit)? = null,
    endIcon: Int? = null,
    endIconAction: () -> Unit = {},
    modifier: Modifier = Modifier,
    child: @Composable () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = colorScheme.onSurfaceVariant,
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
                if(backButtonAction != null) backButtonAction()
                else navController.popBackStack()
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
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
            ){
                IconButton(
                    onClick = {
                        endIconAction()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = endIcon),
                        contentDescription = "End Icon"
                    )
                }
                child()
            }
        }
    }
}