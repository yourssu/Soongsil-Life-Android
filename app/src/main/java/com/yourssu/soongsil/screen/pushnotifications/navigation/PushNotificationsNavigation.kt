package com.yourssu.soongsil.life.screen.pushnotifications.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.yourssu.data.nav.PushNotifications
import com.yourssu.soongsil.screen.pushnotifications.PushNotificationsScreen

fun NavHostController.navigateToPushNotifications(navOptions: NavOptions? = null) =
    navigate(PushNotifications, navOptions)

fun NavGraphBuilder.pushNotificationsScreen() {
    composable<PushNotifications> {
        PushNotificationsScreen()
    }
}
