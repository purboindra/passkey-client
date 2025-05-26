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
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RegistrationResponse(val message: String, val data: OptionsDataClass)

@Serializable
data class UserResponse(val name: String, val age: Int)

@Serializable
data class CredProps(
    val rk: Boolean,
)

@Serializable
data class ClientExtensionResult(
    val credProps: CredProps
)

@Serializable
data class VerifyResponseBody(
    val clientDataJSON: String,
    val attestationObject: String,
    val transports: List<String>,
    val authenticatorData: String,
    val publicKeyAlgorithm: Int,
    val publicKey: String,
)

@Serializable
data class VerifyBody(
    val rawId: String,
    val authenticatorAttachment: String,
    val type: String,
    val id: String,
    val response: VerifyResponseBody,
    val clientExtensionResults: ClientExtensionResult,
)

class PasskeyCreationException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

val URL: String
    get() = "https://passkey-server-production.up.railway.app"

class LoginViewModel : ViewModel() {
    val client = HttpClient(CIO) {
        expectSuccess = true
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
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
        } finally {
            _loading.value = false
        }
    }
    
    suspend fun verifyRegistrationOptions(body: String): Boolean {
        try {
            
            Log.d("LoginViewModel", "Verifying registration options: $body")
            
            val verifyBody = Json.decodeFromString<VerifyBody>(body)
            
            Log.d("LoginViewModel", "Verify body: $verifyBody")
            
            val httpResponse: HttpResponse =
                client.post("$URL/auth/verify-registration-options") {
                    contentType(ContentType.Application.Json)
                    setBody(verifyBody)
                }
            
            if (httpResponse.status != HttpStatusCode.Created) {
                throw Exception(httpResponse.status.description)
            }
            
            return true
        } catch (e: Throwable) {
            Log.e("LoginViewModel", "Error verifying registration options", e)
            return false
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
                
                val credentialJson =
                    result.data.getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON")
                
                Log.d(
                    "LoginViewModel",
                    "Passkey created credentialJson: $credentialJson"
                )
                
                val nonNullCredentialJson = credentialJson
                    ?: throw PasskeyCreationException("Credential JSON is null, cannot proceed with passkey creation")
                
                Log.d(
                    "LoginViewModel",
                    "Successfully obtained credential JSON, proceeding with passkey creation."
                )
                
                val verifyCredential =
                    verifyRegistrationOptions(nonNullCredentialJson)
                
                Log.d(
                    "LoginViewModel",
                    "Passkey verification result: $verifyCredential"
                )
                
            } catch (pke: PasskeyCreationException) {
                Log.e(
                    "LoginViewModel",
                    "Passkey creation failed: ${pke.message}",
                    pke
                )
            } catch (e: Exception) {
                Log.e(
                    "LoginViewModel",
                    "An unexpected error occurred during passkey creation.",
                    e
                )
            }
        }
    }
}