package com.rewardhub.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val type: String, // "earning" or "withdrawal"
    val amount: Double,
    val status: String = "pending", // "pending", "approved", "rejected"
    @SerialName("created_at")
    val createdAt: String? = null,
    val description: String? = null
)
