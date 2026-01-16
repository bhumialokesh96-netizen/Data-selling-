package com.rewardhub.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val key: String,
    val value: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
