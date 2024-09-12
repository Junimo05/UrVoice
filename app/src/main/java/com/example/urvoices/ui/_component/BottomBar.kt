package com.example.urvoices.ui._component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.utils.Navigator.AuthScreen
import com.example.urvoices.utils.Navigator.MainScreen

@Composable
fun BottomBar(
    selectedPage: MutableIntState,
    navController: NavController
) {
    val items = listOf(
        BottomBarItem(
            route = MainScreen.HomeScreen.route,
            selectedIcon = R.drawable.home,
            unselectedIcon = R.drawable.home,
            contentDescription = "Home"
        ),
        BottomBarItem(
            route = AuthScreen.SplashScreen.route,
            selectedIcon = R.drawable.search,
            unselectedIcon = R.drawable.search,
            contentDescription = "Search"
        ),
        BottomBarItem(
            route = "record",
            selectedIcon = R.drawable.ic_actions_add,
            unselectedIcon = R.drawable.ic_actions_add,
            contentDescription = "Record"
        ),
        BottomBarItem(
            route = "profile",
            selectedIcon = R.drawable.person,
            unselectedIcon = R.drawable.person,
            contentDescription = "Profile"
        ),
        BottomBarItem(
            route = "settings",
            selectedIcon = R.drawable.setting,
            unselectedIcon = R.drawable.setting,
            contentDescription = "Settings"
        ),
    )


    NavigationBar(
        modifier = Modifier
            .height(80.dp)
        ,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        items.forEachIndexed() { index, item ->
            val isSelected = selectedPage.intValue == index
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    selectedPage.intValue = index
                    navController.navigate(item.route){
                        popUpTo(navController.graph.findStartDestination().id)
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            if (selectedPage.intValue == index)
                                item.selectedIcon else item.unselectedIcon
                        ),
                        contentDescription = item.contentDescription,
                        modifier = Modifier.width(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                ,
            )
        }
    }
}

data class BottomBarItem(
    val route: String,
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val contentDescription: String,
)

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    MyTheme {
        BottomBar(navController = rememberNavController(), selectedPage = rememberSaveable { mutableIntStateOf(0) })
    }
}