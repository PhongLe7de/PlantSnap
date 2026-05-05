package com.plantsnap.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.plantsnap.ui.screens.history.HistoryScreen
import com.plantsnap.ui.screens.garden.MyGardenScreen
import com.plantsnap.ui.screens.garden.detail.SavedPlantDetailScreen
import com.plantsnap.ui.screens.home.HomeCallbacks
import com.plantsnap.ui.screens.home.HomeScreen
import com.plantsnap.ui.screens.home.PlantOfTheDayDetailScreen
import com.plantsnap.ui.screens.onboarding.OnboardingScreen
import com.plantsnap.ui.screens.profile.AuthViewModel
import com.plantsnap.ui.screens.profile.AuthenticationScreen
import com.plantsnap.ui.screens.profile.ProfileScreen
import com.plantsnap.ui.screens.profile.ProfileViewModel
import com.plantsnap.ui.screens.identify.camera.CameraScreen
import com.plantsnap.ui.screens.identify.camera.CameraViewModel
import com.plantsnap.ui.screens.identify.identify.IdentificationScreen
import com.plantsnap.ui.screens.identify.detail.PlantDetailScreen
import com.plantsnap.ui.screens.identify.preview.ImagePreviewScreen
import com.plantsnap.ui.screens.settings.SettingsScreen
import com.plantsnap.ui.screens.settings.SettingsViewModel
import androidx.compose.ui.res.stringResource
import com.plantsnap.R

enum class BottomNavItem(
    val route: String,
    val label: Int,
    val icon: ImageVector
) {
    HOME("home", R.string.navigation_home, Icons.Filled.Home),
    IDENTIFY("identify", R.string.navigation_identify, Icons.Filled.CameraAlt),
    GARDEN("garden", R.string.navigation_my_garden, Icons.Filled.Favorite),
    PROFILE("profile", R.string.navigation_profile, Icons.Filled.Person)
}

private const val ROUTE_HOME_MAIN = "home_main"
const val ROUTE_HOME_PLANT_DETAILS = "home_plant_details"
private const val ROUTE_PROFILE_MAIN = "profile_main"
const val ROUTE_PROFILE_PLANT_DETAILS = "profile_plant_details"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_PLANT_OF_THE_DAY_DETAIL = "plant_of_the_day_detail"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_GARDEN_PLANT_DETAILS = "garden_plant_details"


enum class IdentifyNavItem(
    val route: String,
) {
    CAMERA("camera"),
    PREVIEW("preview"),
    IDENTIFICATION("identification"),
    PLANT_DETAILS("plant_details")
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()

    val hasCompletedOnboarding = authState.hasCompletedOnboarding ?: return

    val startDestination = if (hasCompletedOnboarding == true) BottomNavItem.HOME.route else ROUTE_ONBOARDING

    val showBottomBar = currentDestination?.route != ROUTE_ONBOARDING

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    val navigateToProfile = {
        navController.navigate(BottomNavItem.PROFILE.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true,
                        onClick = {
                            // Skip current destination
                            val alreadySelected = currentDestination?.hierarchy
                                ?.any { it.route == item.route } ?: false
                            if (!alreadySelected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.testTag("nav_${item.route}"),
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.label),
                            )
                        },
                        label = { Text(stringResource(item.label)) },
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(BottomNavItem.HOME.route) {
                            popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            navigation(
                startDestination = ROUTE_HOME_MAIN,
                route = BottomNavItem.HOME.route,
            ) {
                composable(ROUTE_HOME_MAIN) {
                    HomeScreen(
                        callbacks = HomeCallbacks(
                            onIdentifyPlantSelected = {
                                navController.navigate(BottomNavItem.IDENTIFY.route) {
                                    launchSingleTop = true
                                }
                            },
                            onViewAllScans = {
                                navController.navigate(ROUTE_HISTORY) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onScanSelected = { plantId, candidateIndex ->
                                navController.navigate("$ROUTE_HOME_PLANT_DETAILS/$plantId/$candidateIndex")
                            },
                            onLearnMorePlantOfTheDay = {
                                navController.navigate(ROUTE_PLANT_OF_THE_DAY_DETAIL) {
                                    launchSingleTop = true
                                }
                            },
                            onProfileSelected = navigateToProfile,
                        ),
                        profilePhotoUrl = authState.profilePhotoUrl,
                        authState = authState,
                    )
                }

                composable(ROUTE_PLANT_OF_THE_DAY_DETAIL) {
                    PlantOfTheDayDetailScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable("$ROUTE_HOME_PLANT_DETAILS/{plantId}/{candidateIndex}") { backStackEntry ->
                    PlantDetailScreen(
                        plantId = backStackEntry.arguments?.getString("plantId") ?: "",
                        candidateIndex = backStackEntry.arguments?.getString("candidateIndex")?.toIntOrNull() ?: 0,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            navigation(
                startDestination = IdentifyNavItem.CAMERA.route,
                route = BottomNavItem.IDENTIFY.route
            ) {
                composable(IdentifyNavItem.CAMERA.route) {
                    CameraScreen(
                        onReviewPhotos = {
                            navController.navigate("${IdentifyNavItem.PREVIEW.route}?page=0")
                        },
                        onNavigateToPreview = { page ->
                            navController.navigate("${IdentifyNavItem.PREVIEW.route}?page=$page")
                        }
                    )
                }
                composable("${IdentifyNavItem.PREVIEW.route}?page={page}") { backStackEntry ->
                    val initialPage =
                        backStackEntry.arguments?.getString("page")?.toIntOrNull() ?: 0
                    val cameraViewModel: CameraViewModel = hiltViewModel()
                    ImagePreviewScreen(
                        initialPage = initialPage,
                        onRetake = { navController.popBackStack() },
                        onUsePhotos = {
                            navController.navigate(IdentifyNavItem.IDENTIFICATION.route)
                        },
                        photosHolder = cameraViewModel.photosHolder
                    )
                }
                composable(IdentifyNavItem.IDENTIFICATION.route) {
                    IdentificationScreen(
                        onBack = {
                            navController.popBackStack(
                                route = IdentifyNavItem.CAMERA.route,
                                inclusive = false,
                            )
                        },
                        onPlantSelected = { plantId, candidateIndex ->
                            navController.navigate("${IdentifyNavItem.PLANT_DETAILS.route}/$plantId/$candidateIndex")
                        }
                    )
                }
                composable("${IdentifyNavItem.PLANT_DETAILS.route}/{plantId}/{candidateIndex}") { backStackEntry ->
                    PlantDetailScreen(
                        plantId = backStackEntry.arguments?.getString("plantId") ?: "",
                        candidateIndex = backStackEntry.arguments?.getString("candidateIndex")
                            ?.toIntOrNull() ?: 0,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            navigation(
                startDestination = ROUTE_PROFILE_MAIN,
                route = BottomNavItem.PROFILE.route
            ) {
                composable(ROUTE_PROFILE_MAIN) {
                    val profileViewModel: ProfileViewModel = hiltViewModel()
                    val statsState by profileViewModel.statsState.collectAsState()

                    Column(
                        modifier = Modifier.testTag("screen_profile")
                    ) {
                        when {
                            authState.isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            authState.isLoggedIn -> {
                                ProfileScreen(
                                    authState = authState,
                                    statsState = statsState,
                                    onSignOut = authViewModel::signOut,
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToHistory = { navController.navigate(ROUTE_HISTORY) },
                                    onProfileSelected = navigateToProfile,
                                )
                            }

                            else -> {
                                AuthenticationScreen(
                                    supabaseClient = authViewModel.supabaseClient,
                                    isLoading = false,
                                    errorMessage = authState.errorMessage,
                                    onClearError = authViewModel::clearError,
                                    onError = authViewModel::setError
                                )
                            }
                        }
                    }
                }

                composable(ROUTE_HISTORY) {
                    HistoryScreen(
                        onScanSelected = { plantId, candidateIndex ->
                            navController.navigate("$ROUTE_PROFILE_PLANT_DETAILS/$plantId/$candidateIndex")
                        },
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(ROUTE_SETTINGS) {
                    SettingsScreen(
                        settings = settings,
                        onBack = { navController.popBackStack() },
                        onThemeChange = settingsViewModel::setTheme,
                        onTemperatureUnitChange = settingsViewModel::setTemperatureUnit,
                        onLanguageChange = settingsViewModel::setLanguage,
                        onNotificationsChange = settingsViewModel::setNotificationsEnabled,
                        onPlantCareRemindersChange = settingsViewModel::setPlantCareReminders,
                    )
                }

                composable("$ROUTE_PROFILE_PLANT_DETAILS/{plantId}/{candidateIndex}") { backStackEntry ->
                    PlantDetailScreen(
                        plantId = backStackEntry.arguments?.getString("plantId") ?: "",
                        candidateIndex = backStackEntry.arguments?.getString("candidateIndex")?.toIntOrNull() ?: 0,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            navigation(
                startDestination = "garden_main",
                route = BottomNavItem.GARDEN.route,
            ) {
                composable("garden_main") {
                    MyGardenScreen(
                        onAddSpecimen = {
                            navController.navigate(BottomNavItem.IDENTIFY.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onPlantClick = { savedPlantId ->
                            navController.navigate("$ROUTE_GARDEN_PLANT_DETAILS/$savedPlantId")
                        },
                    )
                }

                composable("$ROUTE_GARDEN_PLANT_DETAILS/{savedPlantId}") { backStackEntry ->
                    SavedPlantDetailScreen(
                        savedPlantId = backStackEntry.arguments?.getString("savedPlantId").orEmpty(),
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

