package com.purboyndradev.saferauth.data

import kotlinx.serialization.Serializable

@Serializable
data class Rp(
    val name: String,
    val id: String
)