package com.purboyndradev.saferauth.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.purboyndradev.saferauth.ui.screens.OtherSignInOptionsScreen
import com.purboyndradev.saferauth.ui.screens.PasskeyInformationScreen
import com.purboyndradev.saferauth.ui.screens.SignUpScreen
import kotlinx.serialization.Serializable

@Serializable
object LoginScreen

@Serializable
object SignInOptions

@Serializable
object PasskeyInformation

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
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
            SignUpScreen(
                navController = navController
            )
        }
        composable<SignInOptions> {
            OtherSignInOptionsScreen(
                navController = navController
            )
        }
        composable<PasskeyInformation> {
            PasskeyInformationScreen(
                navController = navController
            )
        }
    }
}