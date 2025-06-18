package com.purboyndradev.saferauth.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.purboyndradev.saferauth.data.AppCredentialManager
import com.purboyndradev.saferauth.ui.MyIconPack
import com.purboyndradev.saferauth.ui.myiconpack.IconPasskey
import com.purboyndradev.saferauth.ui.navigation.SignInOptions
import com.purboyndradev.saferauth.ui.screens.viewmodel.LoginViewModel

private const val TAG = "LoginScreen"

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SuppressLint("PublicKeyCredential")
@Composable
fun SignUpScreen(
    loginViewModel: LoginViewModel = LoginViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val activityContext = LocalActivity.current
    
    val loading by loginViewModel.loading.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()
    val email by loginViewModel.email.collectAsState()
    
    val appCredentialManager = remember {
        AppCredentialManager(context = context)
    }
    
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotBlank()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            loginViewModel.setEmptyErrorMessage()
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
                .padding(vertical = 24.dp, horizontal = 16.dp),
        ) {
            item {
                Text("My App", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(32.dp))
                Text("Email", fontWeight = FontWeight.W500)
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    isError = loginViewModel.emailHasErrors,
                    supportingText = {
                        if (loginViewModel.emailHasErrors) {
                            Text("Incorrect email format.")
                        }
                    }
                )
                Spacer(Modifier.height(28.dp))
                Text("Signing in", fontWeight = FontWeight.SemiBold)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    SigningInContent(navController)
                }
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(
                    onClick = {
                       loginViewModel.loginWithPasskey(
                           appCredentialManager = appCredentialManager,
                           activityContext = activityContext!!.applicationContext
                       )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (loading) "Loading..." else "Sign In")
                }
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(
                    onClick = {
                        loginViewModel.createPasskey(
                            false,
                            appCredentialManager = appCredentialManager,
                            context,
                            onNavigate = { result ->
                                navController.navigate(
                                   "main_screen/${email}",
                                ) {
                                    popUpTo(0)
                                    launchSingleTop = true
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (loading) "Loading..." else "Sign Up")
                }
            }
        }
    }
}

@Composable
fun SigningInContent(navController: NavController) {
    val annotatedString = buildAnnotatedString {
        append("Passkey is a faster and safer way to sign in than a password. Your account is created with one unless you choose another option. ")
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
            )
        ) {
            append("How passkeys work")
        }
    }
    
    Column(modifier = Modifier.padding(8.dp)) {
        Row {
            Text(
                text = annotatedString,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = MyIconPack.IconPasskey,
                contentDescription = "Icon Passkey",
                modifier = Modifier.size(72.dp),
            )
        }
        TextButton(
            onClick = {
                navController.navigate(SignInOptions)
            },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                "Other ways to sign in",
                fontWeight = FontWeight.W500,
                color = Color.Blue,
            )
        }
    }
}