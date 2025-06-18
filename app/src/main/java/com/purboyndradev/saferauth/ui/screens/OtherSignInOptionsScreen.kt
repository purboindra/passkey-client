package com.purboyndradev.saferauth.ui.screens

import android.R.attr.offset
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.purboyndradev.saferauth.ui.MyIconPack
import com.purboyndradev.saferauth.ui.components.UnavailableDialog
import com.purboyndradev.saferauth.ui.myiconpack.IconPasskey
import com.purboyndradev.saferauth.ui.navigation.PasskeyInformation
import com.purboyndradev.saferauth.ui.navigation.SignInOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherSignInOptionsScreen(
    navController: NavController
) {
    
    val openAlertDialog = remember { mutableStateOf(false) }
    
    fun onNavigateBack() {
        navController.popBackStack()
    }
    
    when {
        openAlertDialog.value -> UnavailableDialog(
            onConfirmation = {
                openAlertDialog.value = false
            },
            onDismissRequest = {
                openAlertDialog.value = false
            },
            dialogText = "Sign up with password is not available yet",
            dialogTitle = "Sorry"
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Other Sign In Ways",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Other Options", fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = {
                openAlertDialog.value = true
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign Up With Password")
            }
            Spacer(modifier = Modifier.height(24.dp))
            PasskeyBox(navController)
        }
    }
}

@Composable
fun PasskeyBox(navController: NavController) {
    val annotatedString = buildAnnotatedString {
        append("You can sign in securely your passkey using your fingerprint, face, or other screen lock method. ")
        
        withLink(
            link = LinkAnnotation.Clickable(
                tag = "PASSKEY_TAG",
                linkInteractionListener = {
                    navController.navigate(PasskeyInformation)
                }
            )
        ) {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                )
            ) {
                append("How passkeys work")
            }
        }
        
    }
    
    Column(
        modifier = Modifier.background(
            color = Color.LightGray.copy(alpha = 0.2f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        )
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
            Text(
                text = annotatedString,
                modifier = Modifier
                    .weight(
                        1f, fill = false
                    )
            )
            Icon(
                imageVector = MyIconPack.IconPasskey,
                contentDescription = "Icon Passkey",
                modifier = Modifier.size(72.dp),
            )
        }
        ElevatedButton(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text("Sign up with a passkey")
        }
    }
}