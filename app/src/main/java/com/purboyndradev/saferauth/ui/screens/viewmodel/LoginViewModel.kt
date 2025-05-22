package com.purboyndradev.saferauth.ui.screens.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purboyndradev.saferauth.data.AppCredentialManager
import com.purboyndradev.saferauth.data.OptionsDataClass
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RegistrationResponse(val message: String, val data: OptionsDataClass)

val URL: String
    //    get() = "http://192.168.1.7:3000"
    get() = "http://10.0.2.2:3000"

class LoginViewModel : ViewModel() {
    val client = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    suspend fun fetchRegistrationOptions(): OptionsDataClass? {
        Log.d("LoginViewModel", "Fetching registration options...")
        try {
            
            _loading.value = true
            
            val httpResponse =
                client.get(urlString = "$URL/auth/generate-registration-options") {
                    url {
                        parameters.append("email", "johndoe@gmail.com")
                        parameters.append("username", "johndoe")
                    }
                }
            
            Log.d(
                "LoginViewModel",
                "Registration options response: $httpResponse"
            )
            
            if (httpResponse.status.value != 200) {
                Log.e("LoginViewModel", "Error fetching registration options")
                throw Exception("Error fetching registration options")
            }
            
            val response: RegistrationResponse =
                httpResponse.body()
            Log.d("LoginViewModel", "Registration options: $response")
            
            return response.data
            
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error fetching registration options", e)
            return null
        }
    }
    
    @SuppressLint("PublicKeyCredential")
    fun createPasskey(
        preferImmediatelyAvailableCredentials: Boolean,
        appCredentialManager: AppCredentialManager,
        activityContext: Context,
    ) {
        viewModelScope.launch {
            
            val requestJson = fetchRegistrationOptions();
            
            if (requestJson == null) {
                Log.e("LoginViewModel", "Error fetching registration options")
                return@launch
            }
            
            Log.d("LoginViewModel", "Registration options: $requestJson")
            
            val modifyJson =
                requestJson.copy(user = requestJson.user.copy(name = "john doe"))
            
            val createPublicKeyCredentialRequest =
                CreatePublicKeyCredentialRequest(
                    requestJson = Json.encodeToString(modifyJson),
                    preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials
                )
            
            Log.d(
                "LoginViewModel",
                "Creating passkey with request: $createPublicKeyCredentialRequest, modifyJson: $modifyJson"
            )
            
            
            try {
                val result =
                    appCredentialManager.credentialManager.createCredential(
                        context = activityContext,
                        request = createPublicKeyCredentialRequest
                    )
                
                Log.d("LoginViewModel", "Passkey created: $result")
                
            } catch (e: Throwable) {
                Log.e("LoginViewModel", "Error creating passkey", e)
            }
        }
    }
}