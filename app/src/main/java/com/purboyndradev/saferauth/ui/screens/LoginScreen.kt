package com.purboyndradev.saferauth.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purboyndradev.saferauth.data.AppCredentialManager
import com.purboyndradev.saferauth.ui.screens.viewmodel.LoginViewModel

private const val TAG = "LoginScreen"

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SuppressLint("PublicKeyCredential")
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = LoginViewModel()
) {
    val context = LocalContext.current
    val activityContext = LocalActivity.current
    
    val loading by loginViewModel.loading.collectAsState()
    val errorMessage by loginViewModel.errormEssage.collectAsState()
    val email by loginViewModel.email.collectAsState()
    
    val appCredentialManager = remember {
        AppCredentialManager(context = context)
    }
    
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotBlank()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
    
    fun loginWithPasskey() {
        loginViewModel.loginWithPasskey(
            appCredentialManager = appCredentialManager,
            activityContext = activityContext!!.applicationContext
        )
    }
    
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text("MyApp", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("Sign in to continue", fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
                
                Spacer(Modifier.height(32.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        loginViewModel.onEmailChange(it)
                    },
                    label = {
                        Text("Email")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                    ),
                    isError = loginViewModel.emailHasErrors,
                    supportingText = {
                        if (loginViewModel.emailHasErrors) {
                            Text("Incorrect email format.")
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))
                ElevatedButton(
                    onClick = {
                        loginViewModel.createPasskey(
                            preferImmediatelyAvailableCredentials = false,
                            appCredentialManager = appCredentialManager,
                            activityContext = activityContext!!.applicationContext
                        )
                    },
                ) {
                    Text(
                        if (loading) "Loading..." else
                            "Create a passkey"
                    )
                }
                Spacer(Modifier.height(16.dp))
                ElevatedButton(
                    onClick = {
                        loginWithPasskey()
                    },
                ) {
                    Text("Sign in with Passkey")
                }
                
                TextButton(onClick = { }) {
                    Text("Or use password instead")
                }
            }
        }
    }
}