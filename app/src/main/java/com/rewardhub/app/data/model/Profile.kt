package com.rewardhub.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("user_id")
    val userId: String,
    val email: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
