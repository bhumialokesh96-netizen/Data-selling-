# RewardHub Architecture

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         Android Application                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Presentation Layer                      │  │
│  │                   (Jetpack Compose UI)                     │  │
│  │                                                             │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │  │
│  │  │  Login   │  │   Home   │  │Withdrawal│  │Transaction│  │  │
│  │  │  Screen  │  │  Screen  │  │  Screen  │  │  Screen   │  │  │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬──────┘  │  │
│  │       │             │             │             │          │  │
│  └───────┼─────────────┼─────────────┼─────────────┼──────────┘  │
│          │             │             │             │             │
│          ▼             ▼             ▼             ▼             │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    ViewModel Layer                         │  │
│  │                (State Management & Logic)                  │  │
│  │                                                             │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │  │
│  │  │   Auth   │  │   Home   │  │Withdrawal│  │Transaction│  │  │
│  │  │ViewModel │  │ ViewModel│  │ ViewModel│  │ ViewModel │  │  │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬──────┘  │  │
│  │       │             │             │             │          │  │
│  └───────┼─────────────┼─────────────┼─────────────┼──────────┘  │
│          │             │             │             │             │
│          └─────────────┴─────────────┴─────────────┘             │
│                           │                                       │
│                           ▼                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                   Repository Layer                         │  │
│  │            (Single Source of Truth Pattern)                │  │
│  │                                                             │  │
│  │              ┌──────────────────────┐                      │  │
│  │              │  RewardHub Repository│                      │  │
│  │              └──────────┬───────────┘                      │  │
│  │                         │                                   │  │
│  └─────────────────────────┼───────────────────────────────────┘  │
│                            │                                      │
│          ┌─────────────────┴─────────────────┐                   │
│          ▼                                    ▼                   │
│  ┌──────────────────┐              ┌──────────────────┐          │
│  │   Data Sources   │              │   Data Sources   │          │
│  │     (Remote)     │              │     (Local)      │          │
│  │                  │              │                  │          │
│  │  Supabase Client │              │    DataStore     │          │
│  │   - Auth API     │              │   - User Prefs   │          │
│  │   - Postgrest    │              │   - Session      │          │
│  │   - Realtime     │              │                  │          │
│  └────────┬─────────┘              └──────────────────┘          │
│           │                                                       │
└───────────┼───────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Supabase Backend                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Auth.Users  │  │   Profiles   │  │    Wallets   │          │
│  │              │  │              │  │              │          │
│  │  - id        │  │  - user_id   │  │  - user_id   │          │
│  │  - email     │  │  - email     │  │  - balance   │          │
│  │  - password  │  │  - created_at│  │  - earnings  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                   │
│  ┌──────────────┐  ┌──────────────────────────────────────┐    │
│  │ Transactions │  │         Database Functions           │    │
│  │              │  │                                       │    │
│  │  - id        │  │  - process_first_withdrawal()        │    │
│  │  - user_id   │  │  - update_wallet_on_transaction()    │    │
│  │  - type      │  │                                       │    │
│  │  - amount    │  │  Triggers:                            │    │
│  │  - status    │  │  - auto_process_first_withdrawal     │    │
│  │  - created_at│  │  - auto_update_wallet_on_earning     │    │
│  └──────────────┘  └──────────────────────────────────────┘    │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Row Level Security (RLS)                     │   │
│  │                                                            │   │
│  │  - Users can only access their own data                  │   │
│  │  - Enforced at database level                            │   │
│  │  - Policies per table                                    │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagrams

### Authentication Flow

```
User Input (Email/Password)
        │
        ▼
   UI Screen
        │
        ▼
  AuthViewModel
        │
        ▼
   Repository
        │
        ▼
SupabaseDataSource
        │
        ▼
   Supabase Auth API
        │
        ├─── Success ───┐
        │               │
        ▼               ▼
Create Profile    Get User ID
        │               │
        ▼               ▼
Create Wallet    Save to DataStore
        │               │
        └───────┬───────┘
                ▼
        Navigate to Home
```

### Withdrawal Flow

```
User enters amount
        │
        ▼
WithdrawalScreen
        │
        ▼
WithdrawalViewModel
        │
        ├─── Validate ───┤
        │                │
        ▼                ▼
  Check balance    Calculate fee
        │                │
        └────────┬───────┘
                 ▼
            Repository
                 │
                 ▼
       SupabaseDataSource
                 │
                 ▼
    Insert into transactions
                 │
                 ▼
       Database Trigger
                 │
      ┌──────────┴──────────┐
      ▼                      ▼
First withdrawal?        Not first?
      │                      │
      ▼                      ▼
Auto-approve            Stay pending
      │                      │
Update wallet          Wait for approval
      │                      │
      └──────────┬───────────┘
                 ▼
         Success Response
                 │
                 ▼
         Update UI State
```

### Transaction History Flow

```
Screen opened
      │
      ▼
TransactionViewModel
      │
      ▼
Repository
      │
      ▼
SupabaseDataSource
      │
      ▼
Query transactions table
      │
      ▼
Filter by user_id
      │
      ▼
Order by created_at DESC
      │
      ▼
Apply RLS policies
      │
      ▼
Return list
      │
      ▼
Update UI State
      │
      ▼
Display in LazyColumn
```

## Component Dependencies

```
MainActivity
    │
    ├─── RewardHubTheme
    │
    ├─── Navigation
    │       │
    │       ├─── LoginScreen ──────────┐
    │       │                          │
    │       ├─── RegisterScreen ───────┤
    │       │                          │
    │       ├─── HomeScreen ───────────┤
    │       │                          │
    │       ├─── WithdrawalScreen ─────┤
    │       │                          │
    │       └─── TransactionScreen ────┤
    │                                  │
    └─── ViewModels ──────────────────┘
            │
            ├─── AuthViewModel
            ├─── HomeViewModel
            ├─── WithdrawalViewModel
            └─── TransactionViewModel
                     │
                     └─── Repository
                              │
                              ├─── SupabaseDataSource
                              └─── UserPreferencesDataStore
```

## State Management

```
┌──────────────────────────────────────────────────┐
│              ViewModel (State Holder)            │
│                                                   │
│  private val _uiState = MutableStateFlow(...)    │
│  val uiState: StateFlow<UiState> = _uiState     │
│                                                   │
│  fun performAction() {                            │
│      viewModelScope.launch {                     │
│          _uiState.update { currentState ->       │
│              // Update state                     │
│          }                                        │
│      }                                            │
│  }                                                │
└──────────────────┬───────────────────────────────┘
                   │
                   │ StateFlow emission
                   │
                   ▼
┌──────────────────────────────────────────────────┐
│           Compose UI (State Consumer)            │
│                                                   │
│  val uiState by viewModel.uiState.collectAsState()│
│                                                   │
│  when (uiState) {                                │
│      is Loading -> ShowLoading()                 │
│      is Success -> ShowContent()                 │
│      is Error -> ShowError()                     │
│  }                                                │
└──────────────────────────────────────────────────┘
```

## Threading Model

```
Main Thread (UI)
    │
    ├─── Compose Rendering
    │
    └─── User Interactions
            │
            ▼
ViewModel (Main Dispatcher)
            │
            ▼
viewModelScope.launch
            │
            ▼
Background Thread (IO Dispatcher)
            │
            ├─── Network Calls
            ├─── Database Operations
            └─── File I/O
                    │
                    ▼
            Results/Errors
                    │
                    ▼
Update StateFlow (Main Thread)
                    │
                    ▼
            Recompose UI
```

## Security Architecture

```
┌─────────────────────────────────────────────────┐
│                  Client Side                     │
├─────────────────────────────────────────────────┤
│  - Input Validation                              │
│  - UI State Management                           │
│  - Local Session Storage (encrypted)             │
│  - HTTPS for all requests                        │
└──────────────────┬──────────────────────────────┘
                   │
                   │ JWT Token Authentication
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│                  Server Side                     │
├─────────────────────────────────────────────────┤
│  Supabase Backend                                │
│  ┌───────────────────────────────────────────┐  │
│  │  Row Level Security (RLS)                 │  │
│  │  - Validate auth.uid() = user_id          │  │
│  │  - Policies per operation (SELECT/INSERT) │  │
│  └───────────────────────────────────────────┘  │
│                                                  │
│  ┌───────────────────────────────────────────┐  │
│  │  Database Triggers                        │  │
│  │  - Business logic enforcement             │  │
│  │  - Data integrity checks                  │  │
│  └───────────────────────────────────────────┘  │
│                                                  │
│  ┌───────────────────────────────────────────┐  │
│  │  Authentication                           │  │
│  │  - JWT token management                   │  │
│  │  - Session handling                       │  │
│  │  - Password hashing                       │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

## Technology Stack

```
┌──────────────────────────────────────┐
│         Presentation Layer           │
├──────────────────────────────────────┤
│  - Jetpack Compose                   │
│  - Material Design 3                 │
│  - Navigation Compose                │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│          Business Layer              │
├──────────────────────────────────────┤
│  - ViewModels                        │
│  - Kotlin Coroutines                 │
│  - StateFlow/Flow                    │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│            Data Layer                │
├──────────────────────────────────────┤
│  - Repository Pattern                │
│  - Supabase Kotlin SDK               │
│  - DataStore (Local)                 │
│  - Ktor Client                       │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│           Backend Layer              │
├──────────────────────────────────────┤
│  - Supabase                          │
│  - PostgreSQL                        │
│  - RESTful API                       │
│  - Real-time subscriptions           │
└──────────────────────────────────────┘
```

---

**Note**: This architecture follows Android best practices and is designed for:
- Scalability
- Maintainability
- Testability
- Security
- Performance
