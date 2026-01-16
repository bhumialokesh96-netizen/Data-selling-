package com.rewardhub.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Withdrawal : Screen("withdrawal")
    object Transactions : Screen("transactions")
}
