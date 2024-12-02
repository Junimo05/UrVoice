package com.example.urvoices.ui._component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urvoices.R
import com.example.urvoices.data.service.FirebaseBlockService

@Composable
fun DeletedPostScreen() {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
	){
		Icon(
			painterResource(id = R.drawable.crying_emoticon_rounded_square_face_svgrepo_com),
			contentDescription = "Blocked",
			modifier = Modifier.size(100.dp)
		)
		Text(
			text = "This post have been deleted for now.",
			style = TextStyle(
				fontWeight = FontWeight.Bold,
				fontSize = 24.sp
			),
			modifier = Modifier.padding(16.dp)
		)
	}
}