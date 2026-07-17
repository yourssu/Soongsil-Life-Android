package com.yourssu.soongsil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yourssu.data.nav.Chapel
import com.yourssu.data.nav.Dashboard
import com.yourssu.data.nav.Grade
import com.yourssu.data.nav.Graduate
import com.yourssu.data.nav.Login
import com.yourssu.data.nav.MyPage
import com.yourssu.data.nav.Scholarship
import com.yourssu.soongsil.ui.login.LoginScreen
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val navController = rememberNavController()
            SoongsilLifeAndroidTheme(
                darkTheme = true,
                dynamicColor = false
            ) {
                NavHost(navController = navController, startDestination = Login) {
                    composable<Login> {
                        LoginScreen()
                    }
                    composable<Dashboard> {
                        // TODO: Implement DashboardScreen
                    }
                    composable<Grade> {
                        // TODO: Implement GraduateScreen
                    }
                    composable<Graduate> {
                        // TODO: Implement GraduateScreen
                    }
                    composable<MyPage> {
                        // TODO: Implement MyPageScreen
                    }
                    composable<Scholarship> {
                        // TODO: Implement MyPageScreen
                    }
                    composable<Chapel> {
                        // TODO: Implement MyPageScreen
                    }
                }
            }
        }
    }
}
