package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.JournalRepository
import com.example.ui.screens.JournalViewModel
import com.example.ui.screens.archive.ArchiveScreen
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.SignUpScreen
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.editor.EditorScreen
import com.example.ui.screens.editor.EditorViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.lock.BiometricLockScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.stats.StatsScreen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    data object Home : BottomNavItem(Screen.Home.route, "Today", Icons.Rounded.HistoryEdu, "nav_home")
    data object Archive : BottomNavItem(Screen.Archive.route, "Archive", Icons.Rounded.AutoStories, "nav_archive")
    data object Calendar : BottomNavItem(Screen.Calendar.route, "Calendar", Icons.Rounded.CalendarMonth, "nav_calendar")
    data object Stats : BottomNavItem(Screen.Stats.route, "Insights", Icons.Rounded.Insights, "nav_stats")
    data object Profile : BottomNavItem(Screen.Profile.route, "Profile", Icons.Rounded.Person, "nav_profile")
}

@Composable
fun AkalabyaApp(
    authRepository: AuthRepository,
    journalRepository: JournalRepository,
    userPreferencesRepository: UserPreferencesRepository
) {
    val biometricLockEnabled by userPreferencesRepository.biometricLockFlow.collectAsState(initial = false)
    var isAppUnlocked by rememberSaveable { mutableStateOf(false) }

    if (biometricLockEnabled && !isAppUnlocked) {
        BiometricLockScreen(
            onUnlockSuccess = { isAppUnlocked = true }
        )
        return
    }

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(authRepository)
    )
    val currentUser by authViewModel.currentUser.collectAsState()

    val journalViewModel: JournalViewModel = viewModel(
        factory = JournalViewModel.Factory(journalRepository, authViewModel.currentUser)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Archive,
        BottomNavItem.Calendar,
        BottomNavItem.Stats,
        BottomNavItem.Profile
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Archive.route,
        Screen.Calendar.route,
        Screen.Stats.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("main_bottom_navigation_bar")
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Splash Destination
            composable(Screen.Splash.route) {
                SplashScreen(
                    isLoggedIn = currentUser != null,
                    onNavigateNext = {
                        val destination = if (currentUser != null) Screen.Home.route else Screen.Login.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Login Destination
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Sign Up Destination
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home Destination
            composable(Screen.Home.route) {
                HomeScreen(
                    journalViewModel = journalViewModel,
                    currentUser = currentUser,
                    onOpenEditor = { entryId, dateMillis ->
                        navController.navigate(Screen.Editor.createRoute(entryId, dateMillis))
                    },
                    onNavigateToArchive = { navController.navigate(Screen.Archive.route) },
                    onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }

            // Archive Destination
            composable(Screen.Archive.route) {
                ArchiveScreen(
                    journalViewModel = journalViewModel,
                    onOpenEditor = { entryId, dateMillis ->
                        navController.navigate(Screen.Editor.createRoute(entryId, dateMillis))
                    },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }

            // Calendar Destination
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    journalViewModel = journalViewModel,
                    onOpenEditor = { entryId, dateMillis ->
                        navController.navigate(Screen.Editor.createRoute(entryId, dateMillis))
                    }
                )
            }

            // Search Destination
            composable(Screen.Search.route) {
                SearchScreen(
                    journalViewModel = journalViewModel,
                    onOpenEditor = { entryId, dateMillis ->
                        navController.navigate(Screen.Editor.createRoute(entryId, dateMillis))
                    }
                )
            }

            // Stats Destination
            composable(Screen.Stats.route) {
                StatsScreen(
                    journalViewModel = journalViewModel
                )
            }

            // Profile Destination
            composable(Screen.Profile.route) {
                ProfileScreen(
                    currentUser = currentUser,
                    journalViewModel = journalViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            // Settings Destination
            composable(Screen.Settings.route) {
                SettingsScreen(
                    userPreferencesRepository = userPreferencesRepository,
                    authViewModel = authViewModel,
                    journalViewModel = journalViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Distraction-Free Journal Editor Destination
            composable(
                route = Screen.Editor.route,
                arguments = listOf(
                    navArgument("entryId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("dateMillis") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId")
                val dateMillisStr = backStackEntry.arguments?.getString("dateMillis")
                val dateMillis = dateMillisStr?.toLongOrNull()
                val uid = currentUser?.uid ?: "local_guest_user"

                val editorViewModel: EditorViewModel = viewModel(
                    key = "editor_${entryId ?: dateMillis ?: "new"}",
                    factory = EditorViewModel.Factory(
                        journalRepository = journalRepository,
                        userId = uid,
                        initialEntryId = entryId,
                        initialDateMillis = dateMillis
                    )
                )

                EditorScreen(
                    viewModel = editorViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
