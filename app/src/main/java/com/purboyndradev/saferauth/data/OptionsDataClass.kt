package com.purboyndradev.saferauth.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class OptionsDataClass(
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

