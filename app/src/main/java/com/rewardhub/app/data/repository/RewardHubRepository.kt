package com.rewardhub.app.data.repository

import com.rewardhub.app.data.local.UserPreferencesDataStore
import com.rewardhub.app.data.model.Transaction
import com.rewardhub.app.data.model.Wallet
import com.rewardhub.app.data.remote.SupabaseDataSource
import kotlinx.coroutines.flow.Flow

class RewardHubRepository(
    private val supabaseDataSource: SupabaseDataSource,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    
    // Authentication
    suspend fun signUp(phoneNumber: String, password: String): Result<String> {
        val result = supabaseDataSource.signUp(phoneNumber, password)
        if (result.isSuccess) {
            val userId = result.getOrNull()!!
            userPreferencesDataStore.saveUserInfo(userId, phoneNumber)
        }
        return result
    }
    
    suspend fun signIn(phoneNumber: String, password: String): Result<String> {
        val result = supabaseDataSource.signIn(phoneNumber, password)
        if (result.isSuccess) {
            val userId = result.getOrNull()!!
            userPreferencesDataStore.saveUserInfo(userId, phoneNumber)
        }
        return result
    }
    
    suspend fun signOut(): Result<Unit> {
        val result = supabaseDataSource.signOut()
        if (result.isSuccess) {
            userPreferencesDataStore.clearUserInfo()
        }
        return result
    }
    
    fun getUserId(): Flow<String?> = userPreferencesDataStore.userId
    
    fun getUserPhoneNumber(): Flow<String?> = userPreferencesDataStore.userEmail
    
    // Wallet Operations
    suspend fun getWallet(userId: String): Result<Wallet> {
        return supabaseDataSource.getWallet(userId)
    }
    
    // Transaction Operations
    suspend fun getTransactions(userId: String): Result<List<Transaction>> {
        return supabaseDataSource.getTransactions(userId)
    }
    
    suspend fun requestWithdrawal(userId: String, amount: Double): Result<Transaction> {
        return supabaseDataSource.createWithdrawal(userId, amount)
    }
    
    suspend fun addEarning(userId: String, amount: Double, description: String): Result<Transaction> {
        return supabaseDataSource.addEarning(userId, amount, description)
    }
}
