package com.rewardhub.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Wallet(
    @SerialName("user_id")
    val userId: String,
    val balance: Double = 0.0,
    @SerialName("total_earnings")
    val totalEarnings: Double = 0.0,
    @SerialName("total_withdrawals")
    val totalWithdrawals: Double = 0.0,
    @SerialName("withdrawal_count")
    val withdrawalCount: Int = 0,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
