package com.plantsnap.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.plantsnap.ui.screens.history.HistoryScreen
import com.plantsnap.ui.screens.home.HomeScreen
import com.plantsnap.ui.screens.profile.ProfileScreen
import com.plantsnap.ui.screens.identify.CameraScreen
import com.plantsnap.ui.screens.identify.IdentificationScreen
import com.plantsnap.ui.screens.identify.PlantDetailScreen

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home),
    IDENTIFY("identify", "Identify", Icons.Filled.Home),
    HISTORY("history", "History", Icons.AutoMirrored.Filled.List),
    PROFILE("profile", "Profile", Icons.Filled.Person)
}

enum class IdentifyNavItem(
    val route: String,
) {
    CAMERA("camera"),
    IDENTIFICATION("identification"),
    PLANT_DETAILS("plant_details")
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.HOME.route) {
                HomeScreen()
            }

            navigation(
                startDestination = IdentifyNavItem.CAMERA.route,
                route = BottomNavItem.IDENTIFY.route
            ) {
                composable(IdentifyNavItem.CAMERA.route) {
                    CameraScreen(
                        onPhotoCaptured = {
                            navController.navigate(IdentifyNavItem.IDENTIFICATION.route)
                        }
                    )
                }
                composable(IdentifyNavItem.IDENTIFICATION.route) {
                    IdentificationScreen(
                        onBack = { navController.popBackStack() },
                        onPlantSelected = { plantId -> navController.navigate("${IdentifyNavItem.PLANT_DETAILS.route}/${plantId}") }
                    )
                }
                composable("${IdentifyNavItem.PLANT_DETAILS.route}/{plantId}") { backStackEntry ->
                    PlantDetailScreen(
                        plantId = backStackEntry.arguments?.getString("plantId") ?: "",
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(BottomNavItem.HISTORY.route) {
                HistoryScreen()
            }

            composable(BottomNavItem.PROFILE.route) {
                ProfileScreen()
            }
        }
    }
}

