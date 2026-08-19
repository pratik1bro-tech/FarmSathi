package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.navigation.Screen
import com.example.data.local.FarmPreferences
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.FarmRepository
import com.example.data.repository.FarmRepositoryImpl
import com.example.features.agrosathi.FarmSathiAiScreen
import com.example.features.agrosathi.FarmSathiAiViewModel
import com.example.features.auth.AuthScreen
import com.example.features.auth.AuthViewModel
import com.example.features.buyers.BuyersScreen
import com.example.features.digital_twin.DigitalTwinScreen
import com.example.features.disease_detection.DiseaseDetectionScreen
import com.example.features.farm.FarmScreen
import com.example.features.farm.FarmViewModel
import com.example.features.forecasting.ForecastingScreen
import com.example.features.home.HomeScreen
import com.example.features.home.HomeViewModel
import com.example.features.irrigation.SmartIrrigationScreen
import com.example.features.logistics.LogisticsScreen
import com.example.features.market.MarketScreen
import com.example.features.market.MarketViewModel
import com.example.features.market.SellDecisionScreen
import com.example.features.notifications.NotificationsScreen
import com.example.features.onboarding.OnboardingScreen
import com.example.features.outbreak.OutbreakRadarScreen
import com.example.features.profile.ProfileScreen
import com.example.features.profile.ProfileViewModel
import com.example.features.setup.FarmSetupScreen
import com.example.features.setup.ProfileSetupScreen
import com.example.features.soil.SoilHealthScreen
import com.example.features.splash.SplashScreen
import com.example.features.telemetry.TelemetryScreen
import com.example.features.weather.WeatherScreen
import com.example.shared.components.FarmSathiBottomBar
import com.example.ui.theme.FarmSathiTheme

class MainActivity : ComponentActivity() {

    private val preferences: FarmPreferences by lazy { FarmPreferences(applicationContext) }
    private val authRepository: AuthRepository by lazy { AuthRepositoryImpl(preferences) }
    private val repository: FarmRepository by lazy { FarmRepositoryImpl() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FarmSathiTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val topLevelRoutes = remember {
                    setOf(
                        Screen.Home.route,
                        Screen.Farm.route,
                        Screen.AgroSathiAi.route,
                        Screen.Market.route,
                        Screen.Profile.route
                    )
                }

                val showBottomBar = currentRoute in topLevelRoutes

                // Shared ViewModels
                val homeViewModel: HomeViewModel = viewModel { HomeViewModel(repository) }
                val farmViewModel: FarmViewModel = viewModel { FarmViewModel(repository) }
                val aiViewModel: FarmSathiAiViewModel = viewModel { FarmSathiAiViewModel(application, repository) }
                val marketViewModel: MarketViewModel = viewModel { MarketViewModel(application, repository) }
                val profileViewModel: ProfileViewModel = viewModel { ProfileViewModel(repository, authRepository) }
                val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            FarmSathiBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 0. Splash Screen
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                onNavigateToNext = { _ ->
                                    val destination = when {
                                        !preferences.isOnboardingCompleted() -> Screen.Onboarding.route
                                        !preferences.isAuthenticated() -> Screen.Auth.route
                                        !preferences.isProfileSetupCompleted() -> Screen.ProfileSetup.route
                                        !preferences.isFarmSetupCompleted() -> Screen.FarmSetup.route
                                        else -> Screen.Home.route
                                    }
                                    navController.navigate(destination) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Onboarding (3-step interactive tour)
                        composable(Screen.Onboarding.route) {
                            OnboardingScreen(
                                onGetStarted = {
                                    val destination = if (preferences.isAuthenticated()) {
                                        if (preferences.isProfileSetupCompleted()) {
                                            if (preferences.isFarmSetupCompleted()) Screen.Home.route else Screen.FarmSetup.route
                                        } else Screen.ProfileSetup.route
                                    } else {
                                        Screen.Auth.route
                                    }
                                    navController.navigate(destination) {
                                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Authentication (Login + OTP verification)
                        composable(Screen.Auth.route) {
                            AuthScreen(
                                viewModel = authViewModel,
                                onAuthSuccess = {
                                    navController.navigate(Screen.ProfileSetup.route) {
                                        popUpTo(Screen.Auth.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Farmer Profile Setup (Step 1)
                        composable(Screen.ProfileSetup.route) {
                            ProfileSetupScreen(
                                repository = repository,
                                onProfileSetupComplete = {
                                    navController.navigate(Screen.FarmSetup.route) {
                                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Farm & Soil Setup (Step 2)
                        composable(Screen.FarmSetup.route) {
                            FarmSetupScreen(
                                repository = repository,
                                onFarmSetupComplete = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.FarmSetup.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 1. Home
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigate = { screen -> navController.navigate(screen.route) },
                                onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                                onOpenLanguageSelector = {
                                    navController.navigate(Screen.Profile.route)
                                    profileViewModel.setLanguageDialogVisible(true)
                                }
                            )
                        }

                        // 2. My Farm
                        composable(Screen.Farm.route) {
                            FarmScreen(
                                viewModel = farmViewModel,
                                onNavigate = { screen -> navController.navigate(screen.route) },
                                onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                                onOpenLanguageSelector = {
                                    navController.navigate(Screen.Profile.route)
                                    profileViewModel.setLanguageDialogVisible(true)
                                }
                            )
                        }

                        // 3. FarmSathi AI
                        composable(Screen.AgroSathiAi.route) {
                            FarmSathiAiScreen(
                                viewModel = aiViewModel,
                                onNavigate = { screen -> navController.navigate(screen.route) },
                                onOpenLanguageSelector = {
                                    navController.navigate(Screen.Profile.route)
                                    profileViewModel.setLanguageDialogVisible(true)
                                }
                            )
                        }

                        // 4. Mandi Market
                        composable(Screen.Market.route) {
                            MarketScreen(
                                viewModel = marketViewModel,
                                onNavigate = { screen -> navController.navigate(screen.route) },
                                onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                                onOpenLanguageSelector = {
                                    navController.navigate(Screen.Profile.route)
                                    profileViewModel.setLanguageDialogVisible(true)
                                }
                            )
                        }

                        // 5. Profile & Preferences
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                onNavigate = { screen -> navController.navigate(screen.route) },
                                onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                                onLogout = {
                                    navController.navigate(Screen.Auth.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Sub-features
                        composable(Screen.Telemetry.route) {
                            TelemetryScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.DiseaseDetection.route) {
                            DiseaseDetectionScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() },
                                onNavigateToAi = { navController.navigate(Screen.AgroSathiAi.route) }
                            )
                        }

                        composable(Screen.SoilHealth.route) {
                            SoilHealthScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() },
                                onNavigateToAi = { navController.navigate(Screen.AgroSathiAi.route) }
                            )
                        }

                        composable(Screen.SmartIrrigation.route) {
                            SmartIrrigationScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() },
                                onNavigateToAi = { navController.navigate(Screen.AgroSathiAi.route) }
                            )
                        }

                        composable(Screen.WeatherIntelligence.route) {
                            WeatherScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Forecasting.route) {
                            ForecastingScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Buyers.route) {
                            BuyersScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Logistics.route) {
                            LogisticsScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.OutbreakRadar.route) {
                            OutbreakRadarScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.DigitalTwin.route) {
                            DigitalTwinScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.SellDecision.route) {
                            SellDecisionScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() },
                                onNavigateToAiChat = { navController.navigate(Screen.AgroSathiAi.route) }
                            )
                        }

                        composable(Screen.Notifications.route) {
                            NotificationsScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
