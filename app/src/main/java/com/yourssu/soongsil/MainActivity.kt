package com.yourssu.soongsil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.yourssu.data.nav.Chapel
import com.yourssu.data.nav.Dashboard
import com.yourssu.data.nav.Grade
import com.yourssu.data.nav.Graduate
import com.yourssu.data.nav.Login
import com.yourssu.data.nav.MyPage
import com.yourssu.data.nav.OnBoardingComplete
import com.yourssu.data.nav.OnBoardingTerms
import com.yourssu.data.nav.PushNotifications
import com.yourssu.data.nav.Scholarship
import com.yourssu.data.nav.Timetable
import com.yourssu.soongsil.screen.chapel.ChapelScreen
import com.yourssu.soongsil.screen.dashboard.DashboardScreen
import com.yourssu.soongsil.screen.dashboard.DashboardViewModel
import com.yourssu.soongsil.screen.grade.GradeDetailScreen
import com.yourssu.soongsil.screen.login.LoginScreen
import com.yourssu.soongsil.screen.login.LoginViewModel
import com.yourssu.soongsil.screen.mypage.MyPageScreen
import com.yourssu.soongsil.screen.mypage.MyPageViewModel
import com.yourssu.soongsil.screen.onboard.OnBoardingCompleteScreen
import com.yourssu.soongsil.screen.onboard.OnBoardingScreen
import com.yourssu.soongsil.screen.pushnotifications.PushNotificationsScreen
import com.yourssu.soongsil.screen.timetable.TimetableScreen
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.components.MainBottomBar
import com.yourssu.soongsil.ui.components.MainTab
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            SoongsilLifeAndroidTheme(
                dynamicColor = false
            ) {
                val currentRoute = navController
                    .currentBackStackEntryAsState()
                    .value
                    ?.destination
                    ?.route
                val showBottomBar = currentRoute !in setOf(
                    Login::class.qualifiedName,
                    OnBoardingTerms::class.qualifiedName,
                    OnBoardingComplete::class.qualifiedName
                )
                val selectedTab = when {
                    currentRoute == Timetable::class.qualifiedName -> MainTab.TIMETABLE
                    currentRoute == PushNotifications::class.qualifiedName -> MainTab.NOTIFICATIONS
                    currentRoute == MyPage::class.qualifiedName -> MainTab.MY_PAGE
                    else -> MainTab.HOME
                }
                var bottomBarHeightPx by remember { mutableIntStateOf(0) }
                val bottomBarHeight = with(LocalDensity.current) { bottomBarHeightPx.toDp() }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val bottomBarGap = maxHeight * 0.01f
                    val navHostInsets = if (showBottomBar) {
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                        )
                    } else {
                        WindowInsets.safeDrawing
                    }

                    CompositionLocalProvider(
                        LocalMainBottomBarPadding provides if (showBottomBar) {
                            bottomBarHeight
                        } else {
                            0.dp
                        }
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Dashboard,
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(navHostInsets)
                        ) {
                        composable<Login> {
                            val viewModel: LoginViewModel = hiltViewModel()
                            val uiState by viewModel.uiState.collectAsState()

                            LaunchedEffect(uiState.isLoginSuccessful) {
                                if (uiState.isLoginSuccessful) {
                                    val destination = if (uiState.isOnboardingRequired) {
                                        OnBoardingTerms
                                    } else {
                                        Dashboard
                                    }
                                    navController.navigate(destination) {
                                        popUpTo<Login> { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    viewModel.onLoginNavigationHandled()
                                }
                            }

                            LoginScreen(
                                isLoading = uiState.isLoading,
                                errorMessage = uiState.error,
                                onLoginClick = viewModel::login
                            )
                        }
                        composable<OnBoardingTerms> {
                            OnBoardingScreen(
                                onTermsAgreementCompleted = {
                                    navController.navigate(OnBoardingComplete) {
                                        popUpTo<OnBoardingTerms> { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable<OnBoardingComplete> {
                            OnBoardingCompleteScreen(
                                onStartClick = {
                                    navController.navigate(Dashboard) {
                                        popUpTo<OnBoardingComplete> { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable<Dashboard> {
                            val viewModel: DashboardViewModel = hiltViewModel()
                            val uiState by viewModel.uiState.collectAsState()
                            val dashboardData = uiState.dashboardData

                            LaunchedEffect(uiState.loginRequired) {
                                if (uiState.loginRequired) {
                                    navController.navigate(Login) {
                                        popUpTo<Dashboard> { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    viewModel.onLoginNavigationHandled()
                                }
                            }

                            DashboardScreen(
                                greetingName = dashboardData?.studentName.orEmpty(),
                                profileName = dashboardData?.studentName.orEmpty(),
                                department = dashboardData?.department.orEmpty(),
                                studentId = dashboardData?.studentId.orEmpty(),
                                gpa = dashboardData?.overallGpa.orEmpty(),
                                semesterGrades = dashboardData?.semesterGrades.orEmpty(),
                                chapelSeat = dashboardData?.chapel?.seat.orEmpty(),
                                chapelSeatDescription = dashboardData?.chapel?.seatDescription.orEmpty(),
                                chapelRemaining = dashboardData?.chapel?.remaining ?: 0,
                                chapelRequired = dashboardData?.chapel?.required ?: 0,
                                chapelAttended = dashboardData?.chapel?.attended ?: 0,
                                chapelLate = dashboardData?.chapel?.late ?: 0,
                                chapelAbsent = dashboardData?.chapel?.absent ?: 0,
                                chapelProgress = dashboardData?.chapel?.progress ?: 0f,
                                refreshStatus = uiState.refreshStatus,
                                refreshStep = uiState.refreshStep,
                                refreshErrorMessage = uiState.error,
                                isPullRefreshing = uiState.isPullRefreshing,
                                onPullToRefresh = viewModel::pullToRefresh,
                                onRefreshRetryClick = viewModel::retryRefresh,
                                onGradeDetailClick = { navController.navigate(Grade) },
                                onChartDetailClick = { navController.navigate(Grade) },
                                onChapelClick = { navController.navigate(Chapel) },
                                onGraduateClick = { navController.navigate(Graduate) },
                                onScholarshipClick = { navController.navigate(Scholarship) }
                            )
                        }
                        composable<Grade> {
                            GradeDetailScreen()
                        }
                        composable<Graduate> {
                            // TODO: Implement GraduateScreen
                        }
                        composable<MyPage> {
                            val viewModel: MyPageViewModel = hiltViewModel()
                            val gradeNotificationEnabled by
                                viewModel.gradeNotificationEnabled.collectAsState()
                            val logoutCompleted by viewModel.logoutCompleted.collectAsState()

                            LaunchedEffect(logoutCompleted) {
                                if (logoutCompleted) {
                                    navController.navigate(Login) {
                                        popUpTo<Dashboard> { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    viewModel.onLogoutNavigationHandled()
                                }
                            }

                            MyPageScreen(
                                gradeNotificationEnabled = gradeNotificationEnabled,
                                onGradeNotificationToggle = viewModel::setGradeNotificationEnabled,
                                onLogoutClick = viewModel::logout
                            )
                        }
                        composable<Timetable> {
                            TimetableScreen()
                        }
                        composable<PushNotifications> {
                            PushNotificationsScreen()
                        }
                        composable<Scholarship> {
                            // TODO: Implement MyPageScreen
                        }
                        composable<Chapel> {
                            ChapelScreen()
                        }
                        }
                    }

                    if (showBottomBar) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .onSizeChanged { bottomBarHeightPx = it.height }
                                .navigationBarsPadding()
                                .padding(bottom = bottomBarGap)
                        ) {
                            MainBottomBar(
                                selectedTab = selectedTab,
                                onTabSelected = navController::navigateToMainTab
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun NavHostController.navigateToMainTab(tab: MainTab) {
    if (tab == MainTab.HOME) {
        if (currentDestination?.route == Dashboard::class.qualifiedName) return
        if (popBackStack<Dashboard>(inclusive = false)) return

        navigate(Dashboard) {
            popUpTo(graph.id)
            launchSingleTop = true
        }
        return
    }

    val options = navOptions {
        popUpTo<Dashboard> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    when (tab) {
        MainTab.HOME -> Unit
        MainTab.TIMETABLE -> navigate(Timetable, options)
        MainTab.NOTIFICATIONS -> navigate(PushNotifications, options)
        MainTab.MY_PAGE -> navigate(MyPage, options)
    }
}
