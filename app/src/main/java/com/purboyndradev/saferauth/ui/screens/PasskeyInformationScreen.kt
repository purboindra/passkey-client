package com.purboyndradev.saferauth.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.purboyndradev.saferauth.ui.MyIconPack
import com.purboyndradev.saferauth.ui.myiconpack.IconPasskey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasskeyInformationScreen(navController: NavController) {
    
    fun onNavigateBack() {
        navController.popBackStack()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Passwordless Sign In",
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
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(vertical = 24.dp, horizontal = 12.dp)
        ) {
            item {
                Icon(
                    imageVector = MyIconPack.IconPasskey,
                    contentDescription = "Icon Passkey",
                    modifier = Modifier.size(96.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Passwordless sign-in with passkeys",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                TitleSubTitleText(
                    title = "What are passkeys?",
                    subTitle = "Passkeys are encrypted digital keys you created using your fingerprint, face, or other screen-lock method."
                )
                Spacer(modifier = Modifier.height(12.dp))
                TitleSubTitleText(
                    title = "Where are passkeys saved?",
                    subTitle = "They are saved to a password manager, so you can sign in on other device."
                )
                Spacer(modifier = Modifier.height(12.dp))
                TitleSubTitleText(
                    title = "Are they better than password?",
                    subTitle = "Passkeys protect you from phishing attacks. Passkeys work only on your registered apps and websites, and your password manager protect passkeys from unauthorized access and use."
                )
            }
        }
    }
}


@Composable
fun TitleSubTitleText(
    title: String,
    subTitle: String,
) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
    Text(subTitle)
}