package com.rewardhub.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rewardhub.app.ui.screens.auth.AuthViewModel
import com.rewardhub.app.ui.screens.auth.LoginScreen
import com.rewardhub.app.ui.screens.auth.RegisterScreen
import com.rewardhub.app.ui.screens.home.HomeScreen
import com.rewardhub.app.ui.screens.home.HomeViewModel
import com.rewardhub.app.ui.screens.transactions.TransactionHistoryScreen
import com.rewardhub.app.ui.screens.transactions.TransactionViewModel
import com.rewardhub.app.ui.screens.withdrawal.WithdrawalScreen
import com.rewardhub.app.ui.screens.withdrawal.WithdrawalViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    withdrawalViewModel: WithdrawalViewModel,
    transactionViewModel: TransactionViewModel,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    authViewModel.resetState()
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    authViewModel.resetState()
                    navController.navigate(Screen.Login.route)
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            val userId by homeViewModel.userId.collectAsState(initial = null)
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToWithdrawal = {
                    navController.navigate(Screen.Withdrawal.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Withdrawal.route) {
            val userId by homeViewModel.userId.collectAsState(initial = null)
            WithdrawalScreen(
                viewModel = withdrawalViewModel,
                userId = userId,
                onNavigateBack = {
                    withdrawalViewModel.resetState()
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Transactions.route) {
            val userId by homeViewModel.userId.collectAsState(initial = null)
            TransactionHistoryScreen(
                viewModel = transactionViewModel,
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
