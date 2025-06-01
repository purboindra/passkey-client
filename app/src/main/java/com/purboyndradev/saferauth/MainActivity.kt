package com.purboyndradev.saferauth

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.purboyndradev.saferauth.ui.navigation.MyAppNavHost
import com.purboyndradev.saferauth.ui.theme.SaferAuthTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaferAuthTheme {
                MyAppNavHost(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}