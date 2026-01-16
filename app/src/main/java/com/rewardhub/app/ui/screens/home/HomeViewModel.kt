package com.rewardhub.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardhub.app.data.model.Transaction
import com.rewardhub.app.data.model.Wallet
import com.rewardhub.app.data.repository.RewardHubRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val wallet: Wallet? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val repository: RewardHubRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    val userId: Flow<String?> = repository.getUserId()
    
    fun loadWalletData(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Load wallet
            val walletResult = repository.getWallet(userId)
            if (walletResult.isFailure) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = walletResult.exceptionOrNull()?.message ?: "Failed to load wallet"
                    ) 
                }
                return@launch
            }
            
            // Load recent transactions
            val transactionsResult = repository.getTransactions(userId)
            
            _uiState.update {
                it.copy(
                    wallet = walletResult.getOrNull(),
                    recentTransactions = transactionsResult.getOrNull()?.take(5) ?: emptyList(),
                    isLoading = false,
                    error = null
                )
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }
}
