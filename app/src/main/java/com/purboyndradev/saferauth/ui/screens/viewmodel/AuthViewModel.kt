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
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialOption
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
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ErrorResponse(val message: String)

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
    val rk: Boolean? = null,
)

@Serializable
data class ClientExtensionResult(
    val credProps: CredProps? = null,
)

@Serializable
data class VerifyResponseBody(
    val clientDataJSON: String,
    val attestationObject: String? = null,
    val transports: List<String> = emptyList<String>(),
    val authenticatorData: String,
    val publicKeyAlgorithm: Int? = null,
    val publicKey: String? = null,
    val signature: String? = null,
    val userHandle: String? = null,
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

class AuthViewModel : ViewModel() {
    val client = HttpClient(CIO) {
        expectSuccess = false
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, request ->
                val clientException = exception as? ClientRequestException
                    ?: return@handleResponseExceptionWithRequest
                val exceptionResponse = clientException.response
                if (exceptionResponse.status == HttpStatusCode.NotFound) {
                    val exceptionResponseText = exceptionResponse.bodyAsText()
                    throw Exception(exceptionResponseText)
                }
            }
        }
    }
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()
    
    private val _errorTriggerId = MutableStateFlow(0)
    val errorTriggerId: StateFlow<Int> = _errorTriggerId.asStateFlow()
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    fun onEmailChange(email: String) {
        _email.value = email
    }
    
    fun setErrorMessage(message: String) {
        _errorMessage.value = message
        _errorTriggerId.update { it + 1 }
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
            
            Log.d(
                "LoginViewModel",
                "Registration options response: ${httpResponse.bodyAsText()}, ${httpResponse.status}"
            )
            
            if (httpResponse.status != HttpStatusCode.OK) {
                val errorMessage: ErrorResponse = httpResponse.body()
                throw Exception(errorMessage.message)
            }
            
            val response =
                httpResponse.bodyAsText()
            
            val decodeResponse =
                Json.decodeFromString<RegistrationResponse>(response)
            
            return decodeResponse.data
            
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error fetching registration options", e)
            setErrorMessage(e.message.toString())
            return null
        }
    }
    
    suspend fun verifyRegistrationOptions(body: String): Boolean {
        try {
            val json = Json {
                ignoreUnknownKeys = true
            }
            
            Log.d("LoginViewModel", "Body verify registration options: $body")
            
            val verifyBody = json.decodeFromString<VerifyBody>(body)
            
            val httpResponse: HttpResponse =
                client.post("$URL/auth/verify-registration-options") {
                    contentType(ContentType.Application.Json)
                    setBody(verifyBody)
                    url {
                        parameters.append("email", _email.value)
                    }
                }
            
            Log.d(
                "LoginViewModel",
                "Response verify registration options: ${httpResponse.bodyAsText()}"
            )
            
            if (httpResponse.status != HttpStatusCode.Created) {
                val errorMessage: ErrorResponse = httpResponse.body()
                throw Exception(errorMessage.message)
            }
            
            return true
        } catch (e: Throwable) {
            Log.e("LoginViewModel", "Error verifying registration options", e)
            setErrorMessage(e.message.toString())
            return false
        }
    }
    
    suspend fun fetchAuthenticationOptions(): AuthenticationOptionsDataClass? {
        try {
            
            val httpResponse =
                client.get(urlString = "$URL/auth/generate-authentication-options") {
                    url {
                        parameters.append("email", _email.value)
                    }
                }
            
            if (httpResponse.status != HttpStatusCode.OK) {
                val errorResponse: ErrorResponse = httpResponse.body()
                setErrorMessage(errorResponse.message)
                return null
            }
            
            val response: AuthenticationResponse =
                httpResponse.body()
            Log.d("LoginViewModel", "Authentication options: $response")
            
            return response.data
        } catch (e: Exception) {
            Log.e(
                "LoginViewModel",
                "Error fetching authentication options Exception",
                e
            )
            setErrorMessage(e.message ?: "Unknown Error")
            return null
        }
    }
    
    suspend fun verifyAuthenticationOptions(body: String): Boolean {
        try {
            val json = Json {
                ignoreUnknownKeys = true
            }
            val verifyBody = json.decodeFromString<VerifyBody>(body)
            
            
            val httpResponse: HttpResponse =
                client.post("$URL/auth/verify-authentication-options") {
                    contentType(ContentType.Application.Json)
                    setBody(verifyBody)
                    url {
                        parameters.append("email", _email.value)
                    }
                }
            
            Log.d(
                "LoginViewModel",
                "Response verify authentication options: ${httpResponse.bodyAsText()}, status code: ${httpResponse.status}"
            )
            
            if (httpResponse.status != HttpStatusCode.Created) {
                val errorMessage: ErrorResponse = httpResponse.body()
                throw Exception(errorMessage.message)
            }
            
            return true
        } catch (e: Throwable) {
            Log.e("LoginViewModel", "Error verifying registration options", e)
            setErrorMessage(e.message.toString())
            return false
        }
    }
    
    @SuppressLint("PublicKeyCredential")
    fun loginWithPasskey(
        appCredentialManager: AppCredentialManager,
        activityContext: Context,
        onNavigate: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                /// Set Loading
                _loading.value = true
                
                val requestOptionsJsonString = fetchAuthenticationOptions();
                
                Log.d(
                    "LoginViewModel",
                    "requestOptionsJsonString: $requestOptionsJsonString"
                )
                
                requestOptionsJsonString?.let {
                    val getPublicKeyCredentialOption =
                        GetPublicKeyCredentialOption(
                            requestJson = Json.encodeToString(it)
                        )
                    
                    val credentialOptions = mutableListOf<CredentialOption>()
                    credentialOptions.add(getPublicKeyCredentialOption)
                    credentialOptions.add(GetPasswordOption())
                    
                    val getCredRequest = GetCredentialRequest(credentialOptions)
                    
                    val result =
                        appCredentialManager.credentialManager.getCredential(
                            context = activityContext,
                            request = getCredRequest
                        )
                    
                    handleSignIn(result, onNavigate)
                }
                
            } catch (e: Exception) {
                Log.e(
                    "LoginViewModel",
                    "Error verifying registration options exception",
                    e
                )
                setErrorMessage(e.message ?: "Unknown error")
            } finally {
                _loading.value = false
            }
        }
    }
    
    suspend fun handleSignIn(
        result: GetCredentialResponse,
        onNavigate: () -> Unit
    ) {
        // Handle the successfully returned credential.
        val credential = result.credential
        
        when (credential) {
            is PublicKeyCredential -> {
                val responseJson = credential.authenticationResponseJson
                // Share responseJson i.e. a GetCredentialResponse on your server to
                // validate and  authenticate
                Log.d("LoginViewModel", "Response: $responseJson")
                
                val response = verifyAuthenticationOptions(responseJson)
                
                if (response) {
                    Log.d("LoginViewModel", "Authentication successful")
                    onNavigate()
                } else {
                    Log.d("LoginViewModel", "Authentication failed")
                }
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
                
                onNavigate()
                
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
        onNavigate: (String) -> Unit
    ) {
        viewModelScope.launch {
            
            /// Set Loading
            _loading.value = true
            
            setEmptyErrorMessage()
            
            try {
                val responseRegistrationOptions = fetchRegistrationOptions();
                
                /// TODO: Condition for return error message
                responseRegistrationOptions?.let {
                    val requestJson =
                        Json.encodeToString(
                            responseRegistrationOptions
                        )
                    
                    val createPublicKeyCredentialRequest =
                        CreatePublicKeyCredentialRequest(
                            requestJson = requestJson,
                            preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials
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
                                        /// Navigate to home screen
                                        onNavigate(_email.value)
                                    } else {
                                        /// TODO: Handle error
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
                setErrorMessage(pke.message.toString())
            } catch (e: Exception) {
                Log.e(
                    "LoginViewModel",
                    "An unexpected error occurred during passkey creation.",
                    e
                )
                setErrorMessage(e.message.toString())
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
        val passwordRequest =
            CreatePasswordRequest(
                id = _email.value,
                password = "123456",
            )
        
        Log.d("LoginViewModel", "Password request: $passwordRequest")
        
        viewModelScope.launch {
            try {
                val result =
                    appCredentialManager.credentialManager.createCredential(
                        activityContext,
                        passwordRequest,
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
