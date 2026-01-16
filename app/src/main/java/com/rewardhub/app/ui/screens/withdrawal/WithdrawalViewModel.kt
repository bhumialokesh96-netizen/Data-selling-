package com.rewardhub.app.ui.screens.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardhub.app.data.model.Wallet
import com.rewardhub.app.data.repository.RewardHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WithdrawalUiState(
    val wallet: Wallet? = null,
    val withdrawalAmount: String = "",
    val processingFee: Double = 0.0,
    val isFirstWithdrawal: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class WithdrawalViewModel(
    private val repository: RewardHubRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WithdrawalUiState())
    val uiState: StateFlow<WithdrawalUiState> = _uiState.asStateFlow()
    
    private val PROCESSING_FEE_PERCENTAGE = 0.02 // 2%
    private val MIN_WITHDRAWAL = 10.0
    
    fun loadWalletData(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val walletResult = repository.getWallet(userId)
            if (walletResult.isSuccess) {
                val wallet = walletResult.getOrNull()!!
                _uiState.update {
                    it.copy(
                        wallet = wallet,
                        isFirstWithdrawal = wallet.withdrawalCount == 0,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = walletResult.exceptionOrNull()?.message ?: "Failed to load wallet"
                    )
                }
            }
        }
    }
    
    fun updateWithdrawalAmount(amount: String) {
        val amountDouble = amount.toDoubleOrNull() ?: 0.0
        val fee = if (_uiState.value.isFirstWithdrawal) 0.0 else amountDouble * PROCESSING_FEE_PERCENTAGE
        
        _uiState.update {
            it.copy(
                withdrawalAmount = amount,
                processingFee = fee
            )
        }
    }
    
    fun requestWithdrawal(userId: String) {
        viewModelScope.launch {
            val amount = _uiState.value.withdrawalAmount.toDoubleOrNull() ?: 0.0
            val wallet = _uiState.value.wallet
            
            if (wallet == null) {
                _uiState.update { it.copy(error = "Wallet data not available") }
                return@launch
            }
            
            if (amount < MIN_WITHDRAWAL) {
                _uiState.update { it.copy(error = "Minimum withdrawal is ₹$MIN_WITHDRAWAL") }
                return@launch
            }
            
            if (amount > wallet.balance) {
                _uiState.update { it.copy(error = "Insufficient balance") }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.requestWithdrawal(userId, amount)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = true,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to request withdrawal"
                    )
                }
            }
        }
    }
    
    fun resetState() {
        _uiState.value = WithdrawalUiState()
    }
}
