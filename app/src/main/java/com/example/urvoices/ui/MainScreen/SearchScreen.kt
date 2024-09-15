package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui.noti_msg.SearchBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchScreen(
    navController: NavController,
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                Text(
                    text = "Explore",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 35.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            SearchBar()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TagSection(listOf("life", "sing", "story", "dark", "trend"))
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSection(tags: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)// Light pink background
            .padding(16.dp)
    ) {
        Text(
            text = "Tag",
            style = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            overflow = FlowRowOverflow.Clip,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                Tag(
                    text = tag
                )
            }
        }
    }
}

@Composable
fun Tag(
    text: String,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.inverseSurface // Dark gray background
    ) {
        Text(
            text = "#$text",
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
fun TagSectionPreview() {
    MyTheme {
        TagSection(listOf("life", "sing", "story", "dark", "trend"))
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    var navController = rememberNavController()
    MyTheme {
        SearchScreen(navController = navController)
    }
}