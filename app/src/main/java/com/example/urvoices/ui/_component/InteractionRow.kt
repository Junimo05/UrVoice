package com.example.urvoices.ui._component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class Interaction(
    val icon: Int,
    val iconAfterAct: Int,
    val count: Int?,
    val contentDescription: String,
    val action: () -> Unit
)

@Composable
fun InteractionRow(
    //isStared: Boolean,
    interactions: List<Interaction>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .then(modifier)
           ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        interactions.forEachIndexed { index, interaction ->
            Row (
                modifier = Modifier
                    .clickable { interaction.action() }
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(2.dp),
                    onClick = { interaction.action }
                ) {
                    Icon(
                        painter = painterResource(id = interaction.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(2.dp)
                            .size(24.dp)
                            ,
                    )
                }
                Spacer(modifier = Modifier.width(3.dp))
                if(interaction.count != null){
                    Text(
                        text = interaction.count.toString(),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InteractionRowPreview() {
    InteractionRow(
        interactions = listOf(
            Interaction(
                icon = android.R.drawable.star_on,
                iconAfterAct = android.R.drawable.star_off,
                count = 10,
                contentDescription = "Star",
                action = { /*TODO*/ }
            ),
            Interaction(
                icon = android.R.drawable.star_on,
                iconAfterAct = android.R.drawable.star_off,
                count = 20,
                contentDescription = "Star",
                action = { /*TODO*/ }
            ),
            Interaction(
                icon = android.R.drawable.star_on,
                iconAfterAct = android.R.drawable.star_off,
                count = 30,
                contentDescription = "Star",
                action = { /*TODO*/ }
            ),
        )
    )
}