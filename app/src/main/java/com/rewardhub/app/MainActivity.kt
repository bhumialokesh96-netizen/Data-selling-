package com.rewardhub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.rewardhub.app.data.local.UserPreferencesDataStore
import com.rewardhub.app.data.remote.SupabaseDataSource
import com.rewardhub.app.data.repository.RewardHubRepository
import com.rewardhub.app.ui.navigation.AppNavigation
import com.rewardhub.app.ui.navigation.Screen
import com.rewardhub.app.ui.screens.auth.AuthViewModel
import com.rewardhub.app.ui.screens.home.HomeViewModel
import com.rewardhub.app.ui.screens.transactions.TransactionViewModel
import com.rewardhub.app.ui.screens.withdrawal.WithdrawalViewModel
import com.rewardhub.app.ui.theme.RewardHubTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var repository: RewardHubRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repository
        val supabaseDataSource = SupabaseDataSource()
        val userPreferencesDataStore = UserPreferencesDataStore(applicationContext)
        repository = RewardHubRepository(supabaseDataSource, userPreferencesDataStore)
        
        setContent {
            RewardHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Determine start destination based on user login status
                    val startDestination by produceState<String>(
                        initialValue = Screen.Login.route,
                        producer = {
                            val userId = repository.getUserId().first()
                            value = if (userId != null) Screen.Home.route else Screen.Login.route
                        }
                    )
                    
                    // Create ViewModels
                    val authViewModel = remember {
                        AuthViewModel(repository)
                    }
                    
                    val homeViewModel = remember {
                        HomeViewModel(repository)
                    }
                    
                    val withdrawalViewModel = remember {
                        WithdrawalViewModel(repository)
                    }
                    
                    val transactionViewModel = remember {
                        TransactionViewModel(repository)
                    }
                    
                    AppNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        homeViewModel = homeViewModel,
                        withdrawalViewModel = withdrawalViewModel,
                        transactionViewModel = transactionViewModel,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
