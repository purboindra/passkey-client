package com.purboyndradev.saferauth.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String = "",
    val displayName: String
)