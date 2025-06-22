package com.purboyndradev.saferauth.data

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticatorSelection(
    val residentKey: String,
    val userVerification: String,
    val requireResidentKey: Boolean,
    val authenticatorAttachment: String,
)