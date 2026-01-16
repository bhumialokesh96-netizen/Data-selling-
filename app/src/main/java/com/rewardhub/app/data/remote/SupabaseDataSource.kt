package com.rewardhub.app.data.remote

import at.favre.lib.crypto.bcrypt.BCrypt
import com.rewardhub.app.data.model.Profile
import com.rewardhub.app.data.model.Transaction
import com.rewardhub.app.data.model.Wallet
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseDataSource {
    private val client = SupabaseConfig.client
    
    // Authentication
    suspend fun signUp(phoneNumber: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Hash the password
            val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
            
            // Use phone number as email for Supabase auth (workaround)
            // Format: phone@rewardhub.app
            val authEmail = "${phoneNumber.replace("+", "").replace(" ", "")}@rewardhub.app"
            
            val user = client.auth.signUpWith(Email) {
                this.email = authEmail
                this.password = password
            }
            
            val userId = user?.id ?: return@withContext Result.failure(Exception("User ID not found"))
            
            // Create profile with phone number and hashed password
            client.from("profiles").insert(
                Profile(userId = userId, phoneNumber = phoneNumber, password = hashedPassword)
            )
            
            // Create wallet
            client.from("wallets").insert(
                Wallet(userId = userId)
            )
            
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(phoneNumber: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Use phone number as email for Supabase auth (workaround)
            val authEmail = "${phoneNumber.replace("+", "").replace(" ", "")}@rewardhub.app"
            
            client.auth.signInWith(Email) {
                this.email = authEmail
                this.password = password
            }
            val userId = client.auth.currentUserOrNull()?.id ?: return@withContext Result.failure(Exception("User ID not found"))
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUserId(): String? {
        return client.auth.currentUserOrNull()?.id
    }
    
    // Wallet Operations
    suspend fun getWallet(userId: String): Result<Wallet> = withContext(Dispatchers.IO) {
        try {
            val wallet = client.from("wallets")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<Wallet>()
            Result.success(wallet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Transaction Operations
    suspend fun getTransactions(userId: String): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            val transactions = client.from("transactions")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<Transaction>()
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createWithdrawal(userId: String, amount: Double): Result<Transaction> = withContext(Dispatchers.IO) {
        try {
            val transaction = Transaction(
                userId = userId,
                type = "withdrawal",
                amount = amount,
                status = "pending"
            )
            
            val result = client.from("transactions")
                .insert(transaction) {
                    select()
                }
                .decodeSingle<Transaction>()
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addEarning(userId: String, amount: Double, description: String): Result<Transaction> = withContext(Dispatchers.IO) {
        try {
            val transaction = Transaction(
                userId = userId,
                type = "earning",
                amount = amount,
                status = "approved",
                description = description
            )
            
            val result = client.from("transactions")
                .insert(transaction) {
                    select()
                }
                .decodeSingle<Transaction>()
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
