package com.purboyndradev.saferauth.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption

class AppCredentialManager(private val context: Context) {
    val credentialManager = CredentialManager.create(context)
    
    /// TODO DUMMY REQUEST JSON
    val dummyRequestJson = """
{
  "challenge": "c29tZS1yYW5kb20tY2hhbGxlbmdl",
  "rp": {
    "name": "Passkey Demo",
    "id": "localhost"
  },
  "user": {
    "id": "dXNlcmlkMQ",
    "name": "james@example.com",
    "displayName": "James"
  },
  "pubKeyCredParams": [
    {
      "alg": -7,
      "type": "public-key"
    }
  ],
  "authenticatorSelection": {
    "residentKey": "preferred",
    "userVerification": "preferred"
  },
  "timeout": 60000,
  "attestation": "none"
}
""".trimIndent()
    
    val getPasswordOption = GetPasswordOption()
    
    val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
        requestJson = dummyRequestJson
    )
    
    val getCredRequest = GetCredentialRequest(
        listOf(getPasswordOption, getPublicKeyCredentialOption)
    )
    
}