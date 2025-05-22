package com.purboyndradev.saferauth.data

import kotlinx.serialization.Serializable

@Serializable
data class PubKeyCredParam(
    val alg: Int,
    val type: String
)