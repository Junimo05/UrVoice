package com.example.urvoices.ui._component

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.urvoices.R
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.viewmodel.HomeViewModel

@Composable
fun BottomBar(
    selectedPage: MutableIntState,
    navController: NavController,
    homeViewModel: HomeViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomBarItem(
            route = MainScreen.HomeScreen.route,
            selectedIcon = R.drawable.home,
            unselectedIcon = R.drawable.home,
            contentDescription = "Home"
        ),
        BottomBarItem(
            route = MainScreen.SearchScreen.route,
            selectedIcon = R.drawable.search,
            unselectedIcon = R.drawable.search,
            contentDescription = "Search"
        ),
        BottomBarItem(
            route = MainScreen.UploadScreen.route,
            selectedIcon = R.drawable.ic_actions_add,
            unselectedIcon = R.drawable.ic_actions_add,
            contentDescription = "Upload"
        ),
        BottomBarItem(
            route = MainScreen.ProfileScreen.MainProfileScreen.route,
            selectedIcon = R.drawable.person,
            unselectedIcon = R.drawable.person,
            contentDescription = "Profile"
        ),
        BottomBarItem(
            route = MainScreen.SettingsScreen.MainSettingsScreen.route,
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
            val isSelected = when(item.route){
                MainScreen.ProfileScreen.MainProfileScreen.route -> currentDestination?.route?.startsWith(MainScreen.ProfileScreen.route) == true
                MainScreen.SettingsScreen.MainSettingsScreen.route -> currentDestination?.route?.startsWith(MainScreen.SettingsScreen.route) == true
                else -> currentDestination?.route == item.route
            }
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if(isSelected) {
                        when(item.route){
                            MainScreen.HomeScreen.route -> {
                                homeViewModel.triggerScrollToTop()
                            }
                            MainScreen.SettingsScreen.MainSettingsScreen.route -> {

                            }
                        }
                    } else {
                        selectedPage.intValue = index
                        navController.navigate(item.route){
                            popUpTo(navController.graph.findStartDestination().id)
                        }
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
