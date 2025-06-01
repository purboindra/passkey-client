package com.purboyndradev.saferauth.ui.screens.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.credentials.CreateCredentialRequest
import androidx.credentials.CreateCredentialResponse
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purboyndradev.saferauth.data.AppCredentialManager
import com.purboyndradev.saferauth.data.AuthenticationOptionsDataClass
import com.purboyndradev.saferauth.data.OptionsDataClass
import com.purboyndradev.saferauth.data.User
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
import io.ktor.client.statement.bodyAsText
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
data class AuthenticationResponse(
    val message: String,
    val data: AuthenticationOptionsDataClass
)

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
    
    private val _errorMessage = MutableStateFlow("")
    val errormEssage: StateFlow<String> = _errorMessage.asStateFlow()
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    fun onEmailChange(email: String) {
        _email.value = email
    }
    
    fun setEmptyErrorMessage() {
        /// Reset Error Message
        _errorMessage.value = ""
    }
    
    val emailHasErrors by derivedStateOf {
        if (_email.value.isNotEmpty()) {
            // Email is considered erroneous until it completely matches EMAIL_ADDRESS.
            !android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()
        } else {
            false
        }
    }
    
    suspend fun fetchRegistrationOptions(): OptionsDataClass? {
        try {
            
            val httpResponse =
                client.get(urlString = "$URL/auth/generate-registration-options") {
                    url {
                        parameters.append("email", _email.value)
                    }
                }
            
            if (httpResponse.status.value != 200) {
                throw Exception("Error fetching registration options")
            }
            
            val response =
                httpResponse.bodyAsText()
            
            val decodeResponse =
                Json.decodeFromString<RegistrationResponse>(response)
            
            return decodeResponse.data
            
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error fetching registration options", e)
            _errorMessage.value = e.message.toString()
            return null
        }
    }
    
    suspend fun verifyRegistrationOptions(body: String): Boolean {
        try {
            
            val json = Json {
                ignoreUnknownKeys = true
            }
            
            val verifyBody = json.decodeFromString<VerifyBody>(body)
            
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
            _errorMessage.value = e.message.toString()
            return false
        }
    }
    
    suspend fun fetchAuthenticationOptions(): AuthenticationOptionsDataClass? {
        try {
            _loading.value = true
            
            val httpResponse =
                client.get(urlString = "$URL/auth/generate-authentication-options") {
                    url {
                        /// TODO: Get from local storage
                        parameters.append("email", _email.value)
                    }
                }
            
            Log.d(
                "LoginViewModel",
                "Authentication options response: ${httpResponse.bodyAsText()}"
            )
            
            if (httpResponse.status.value != 200) {
                Log.e("LoginViewModel", "Error fetching authentication options")
                throw Exception("Error fetching authentication options")
            }
            
            val response: AuthenticationResponse =
                httpResponse.body()
            Log.d("LoginViewModel", "Authentication options: $response")
            
            return response.data
            
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error fetching authentication options", e)
            return null
        }
    }
    
    @SuppressLint("PublicKeyCredential")
    fun loginWithPasskey(
        appCredentialManager: AppCredentialManager,
        activityContext: Context,
    ) {
        viewModelScope.launch {
            try {
                /// Reset Error Message
                _errorMessage.value = ""
                
                /// Set Loading
                _loading.value = true
                
                val loginJson = fetchAuthenticationOptions();
                
                if (loginJson == null) {
                    throw Exception("Error fetching authentication options, loginJson is null")
                }
                
                val modifyJson = loginJson.copy(
                    user = User(
                        name = _email.value.split("@")[0],
                        id = "",
                        displayName = _email.value.split("@")[0],
                    )
                )
                
                val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
                    requestJson = Json.encodeToString(modifyJson)
                )
                
                val getPasswordOption = GetPasswordOption()
                
                val getCredRequest = GetCredentialRequest(
                    listOf(getPasswordOption, getPublicKeyCredentialOption)
                )
                
                val result =
                    appCredentialManager.credentialManager.getCredential(
                        context = activityContext,
                        request = getCredRequest
                    )
                
                handleSignIn(result)
                
            } catch (e: Throwable) {
                Log.e(
                    "LoginViewModel",
                    "Error verifying registration options",
                    e
                )
                _errorMessage.value = e.message.toString()
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential
        
        when (credential) {
            is PublicKeyCredential -> {
                val responseJson = credential.authenticationResponseJson
                // Share responseJson i.e. a GetCredentialResponse on your server to
                // validate and  authenticate
                Log.d("LoginViewModel", "Response: $responseJson")
            }
            
            is PasswordCredential -> {
                val username = credential.id
                val password = credential.password
                // Share username and password i.e. a GetCredentialResponse on your server to
                // validate and  authenticate
                Log.d(
                    "LoginViewModel",
                    "Username: $username, Password: $password"
                )
            }
            
            else -> {
                Log.e("LoginViewModel", "Unexpected type of credential")
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SuppressLint("PublicKeyCredential")
    fun createPasskey(
        preferImmediatelyAvailableCredentials: Boolean,
        appCredentialManager: AppCredentialManager,
        activityContext: Context,
    ) {
        viewModelScope.launch {
            
            /// Set Loading
            _loading.value = true
            
            try {
                val responseRegistrationOptions = fetchRegistrationOptions();
                
                responseRegistrationOptions?.let {
                    Log.d("LoginViewModel", "Registration options: $it")
                    
                    val jsonForPasskey = Json {
                        encodeDefaults = true
                        prettyPrint = true
                        isLenient = true
                    }
                    
                    val requestJson =
                        jsonForPasskey.encodeToString(
                            responseRegistrationOptions
                        )
                    
                    val createPublicKeyCredentialRequest =
                        CreatePublicKeyCredentialRequest(
                            requestJson = requestJson,
                            preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials
                        )
                    
                    Log.d(
                        "LoginViewModel",
                        "Creating passkey with request: $createPublicKeyCredentialRequest"
                    )
                    
                    
                    registerPasskey(
                        appCredentialManager = appCredentialManager,
                        activityContext = activityContext,
                        request = createPublicKeyCredentialRequest
                    ) { result ->
                        if (result != null) {
                            val registrationResponseJsonKey =
                                "androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON"
                            
                            val bundle = result.data
                            
                            val credentialJson =
                                bundle.getString(
                                    registrationResponseJsonKey
                                )
                            
                            credentialJson?.let {
                                viewModelScope.launch {
                                    val verified =
                                        verifyRegistrationOptions(
                                            it,
                                        )
                                    
                                    if (verified) {
                                        Log.d(
                                            "LoginViewModel",
                                            "Passkey verified"
                                        )
                                    } else {
                                        Log.e(
                                            "LoginViewModel",
                                            "Passkey verification failed"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (pke: PasskeyCreationException) {
                Log.e(
                    "LoginViewModel",
                    "Passkey creation failed: ${pke.message}",
                    pke
                )
                _errorMessage.value = pke.message.toString()
            } catch (e: Exception) {
                Log.e(
                    "LoginViewModel",
                    "An unexpected error occurred during passkey creation.",
                    e
                )
                _errorMessage.value = e.message.toString()
            } finally {
                _loading.value = false
            }
        }
    }
    
    suspend fun registerPasskey(
        appCredentialManager: AppCredentialManager,
        activityContext: Context,
        request: CreateCredentialRequest,
        onResult: (CreateCredentialResponse?) -> Unit
    ) {
        try {
            val result =
                appCredentialManager.credentialManager.createCredential(
                    activityContext,
                    request,
                )
            
            onResult(result)
            
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error registering passkey", e)
            onResult(null)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun registerPassword(
        username: String,
        password: String,
        appCredentialManager: AppCredentialManager,
        activityContext: Context,
        request: CreateCredentialRequest,
        onResult: (CreateCredentialResponse?) -> Unit,
    ) {
        /// CreatePasswordRequest for save password credential
//        val result =
//            appCredentialManager.credentialManager.createCredential(
//                activityContext,
//                request
//            )
        
        viewModelScope.launch {
            try {
                val result =
                    appCredentialManager.credentialManager.createCredential(
                        activityContext,
                        request,
                    )
                Log.d("LoginViewModel", "Credential type: ${result.type}")
                Log.d(
                    "LoginViewModel",
                    "Credential data bundle: ${result.data}"
                )
                onResult(result)
            } catch (e: androidx.credentials.exceptions.CreateCredentialException) {
                Log.e(
                    "LoginViewModel",
                    "Error creating password credential: $e"
                )
                onResult(null)
            }
        }
    }
}


//                    registerPassword(
//                        username = _email.value.split("@")[0],
//                        password = "qwerty123",
//                        appCredentialManager = appCredentialManager,
//                        activityContext = activityContext,
//                        request = createPublicKeyCredentialRequest
//                    ) { result ->
//                        if (result != null) {
//                            try {
//
//                                Log.d(
//                                    "LoginViewModel",
//                                    "Result: ${result.data}"
//                                )
//
//                                val registrationResponseJsonKey =
//                                    "androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON"
//
//                                val bundle = result.data
//
//                                val credentialJson =
//                                    bundle.getString(registrationResponseJsonKey)
//
//                                Log.d(
//                                    "LoginViewModel",
//                                    "Credential JSON: $credentialJson"
//                                )
//
////
////                            val nonNullCredentialJson = credentialJson
////                                ?: throw PasskeyCreationException("Credential JSON is null, cannot proceed with passkey creation")
//
//                                credentialJson?.let {
//                                    viewModelScope.launch {
//                                        val verified =
//                                            verifyRegistrationOptions(
//                                                it,
//                                            )
//
//                                        if (verified) {
//                                            Log.d(
//                                                "LoginViewModel",
//                                                "Passkey verified"
//                                            )
//                                        } else {
//                                            Log.e(
//                                                "LoginViewModel",
//                                                "Passkey verification failed"
//                                            )
//                                        }
//                                    }
//                                }
//                            } catch (e: PasskeyCreationException) {
//                                Log.e(
//                                    "LoginViewModel",
//                                    "Error verifying registration options while creating passkey",
//                                    e
//                                )
//                                _errorMessage.value = e.message.toString()
//                            } catch (e: Exception) {
//                                Log.e(
//                                    "LoginViewModel",
//                                    "Error verifying registration options",
//                                    e
//                                )
//                                _errorMessage.value = e.message.toString()
//                            }
//
//                        }
//
//                    }