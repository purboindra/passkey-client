package com.purboyndradev.saferauth.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class OptionsDataClass(
    val rpId: String = "",
    val challenge: String,
    val rp: Rp,
    val user: User,
    val pubKeyCredParams: List<PubKeyCredParam>,
    val timeout: Long,
    val attestation: String,
    val excludeCredentials: List<ExcludeCredential>,
    val authenticatorSelection: AuthenticatorSelection,
    val extensions: ExtensionsDataClass,
    val hints: JsonArray = JsonArray(emptyList())
)

@Serializable
data class AllowCredential(
    val id: String,
    val type: String,
    val transports: List<String>,
)

@Serializable
data class AuthenticationOptionsDataClass(
    val rpId: String,
    val user: User? = null,
    val challenge: String,
    val allowCredentials: List<AllowCredential>,
    val userVerification: String,
    val timeout: Long
)
