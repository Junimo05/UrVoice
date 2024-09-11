package com.example.thevoices.presentations.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.thevoices.R

@Composable
fun TopBarBackButton(
    navController: NavController,
    title : String,
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Title
        Text(
            text = title,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontStyle = FontStyle.Normal
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
                contentDescription = "Back"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarBackButtonPreview() {
    val navController = rememberNavController()
    TopBarBackButton(navController, title = "Title")
}