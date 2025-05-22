package com.purboyndradev.saferauth.data

import kotlinx.serialization.Serializable

@Serializable
data class ExcludeCredential(
    val type: String,
)