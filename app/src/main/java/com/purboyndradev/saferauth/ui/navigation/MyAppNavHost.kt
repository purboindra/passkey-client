package com.purboyndradev.saferauth.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.purboyndradev.saferauth.ui.screens.LoginScreen
import kotlinx.serialization.Serializable

@Serializable
object LoginScreen

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = LoginScreen
    ) {
        composable<LoginScreen> {
            LoginScreen()
        }
    }
}