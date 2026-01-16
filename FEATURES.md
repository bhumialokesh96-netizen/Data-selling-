# RewardHub - Feature Documentation

This document provides detailed information about all features implemented in the RewardHub Android application.

## Table of Contents
1. [Authentication System](#authentication-system)
2. [Wallet Management](#wallet-management)
3. [Withdrawal System](#withdrawal-system)
4. [Transaction History](#transaction-history)
5. [User Interface](#user-interface)
6. [Architecture & Technical Features](#architecture--technical-features)

---

## Authentication System

### User Registration
- **Simple Email/Password Registration**: Users can create an account using email and password
- **Backend Integration**: Automatically creates user profile and wallet upon registration
- **No Email Verification**: Simplified for ease of use (can be added later)
- **Secure Storage**: User credentials are managed by Supabase Auth
- **Automatic Login**: Users are automatically logged in after successful registration

**Implementation Details:**
- Located in: `ui/screens/auth/RegisterScreen.kt`
- ViewModel: `AuthViewModel.kt`
- Validates password matching before submission
- Displays error messages for failed registration attempts

### User Login
- **Email/Password Authentication**: Secure login using Supabase Auth
- **Session Management**: Persistent login using DataStore
- **Automatic Navigation**: Redirects to home screen on successful login
- **Error Handling**: Clear error messages for invalid credentials

**Implementation Details:**
- Located in: `ui/screens/auth/LoginScreen.kt`
- Stores user session locally for automatic login on app restart
- Clean, modern UI with Material Design 3

---

## Wallet Management

### Real-time Balance Display
- **Current Balance**: Shows available balance in user's wallet
- **Total Earnings**: Displays cumulative earnings over time
- **Auto-refresh**: Balance updates when returning to home screen
- **Currency Format**: Display amounts in Indian Rupees (₹)

### Wallet Features
- **Initial Balance**: New users start with ₹0.00
- **Earnings Addition**: Automatically updates when earnings are added
- **Withdrawal Deduction**: Balance decreases upon successful withdrawal
- **Transaction Count**: Tracks number of withdrawals for first-withdrawal-free feature

**Implementation Details:**
- Data Model: `data/model/Wallet.kt`
- Backend Table: `wallets` in Supabase
- Fields: `balance`, `total_earnings`, `total_withdrawals`, `withdrawal_count`

---

## Withdrawal System

### First Withdrawal (Free)
- **No Processing Fee**: First withdrawal is completely free
- **Auto-Approval**: Automatically approved without manual review
- **Minimum Amount**: ₹10 minimum withdrawal requirement
- **Instant Processing**: Balance is immediately updated

### Subsequent Withdrawals
- **2% Processing Fee**: Standard fee applied to all withdrawals after the first
- **Manual Review**: Requires backend approval (can be automated)
- **Fee Calculation**: Transparent display of fees before confirmation
- **Net Amount Display**: Shows exact amount user will receive

### Withdrawal Flow
1. User navigates to withdrawal screen
2. Enters desired withdrawal amount
3. System validates:
   - Minimum amount (₹10)
   - Sufficient balance
   - Calculates processing fee if not first withdrawal
4. Shows confirmation with net amount
5. User confirms withdrawal request
6. Transaction is created with appropriate status
7. Success message displayed

**Implementation Details:**
- Screen: `ui/screens/withdrawal/WithdrawalScreen.kt`
- ViewModel: `WithdrawalViewModel.kt`
- Automatic fee calculation based on withdrawal count
- Beautiful UI with Material Design 3 cards

**Business Rules:**
- Minimum withdrawal: ₹10.00
- Processing fee: 2% (except first withdrawal)
- First withdrawal: Auto-approved, fee-free
- Subsequent withdrawals: Pending status, requires approval

---

## Transaction History

### Transaction Types

#### 1. Earnings
- **Type**: Incoming transactions
- **Icon**: Upward trend arrow (green)
- **Display**: +₹ amount
- **Status**: Usually "approved"
- **Description**: Optional description of earning source

#### 2. Withdrawals
- **Type**: Outgoing transactions
- **Icon**: Downward trend arrow (red)
- **Display**: -₹ amount
- **Status**: "pending", "approved", or "rejected"
- **Description**: Withdrawal request

### Transaction Status

1. **Pending**: 
   - Transaction is awaiting approval
   - Shown with clock icon
   - Yellow/orange color scheme

2. **Approved**: 
   - Transaction has been processed
   - Shown with check circle icon
   - Green color scheme

3. **Rejected**: 
   - Transaction was denied
   - Shown with cancel icon
   - Red color scheme

### Features
- **Chronological Order**: Most recent transactions first
- **Complete History**: All transactions since account creation
- **Detailed View**: Amount, type, status, date, and description
- **Color Coding**: Visual distinction between earnings and withdrawals
- **Pull to Refresh**: Manually refresh transaction list
- **Empty State**: Helpful message when no transactions exist

**Implementation Details:**
- Screen: `ui/screens/transactions/TransactionHistoryScreen.kt`
- ViewModel: `TransactionViewModel.kt`
- Data loaded from Supabase `transactions` table
- Styled with Material 3 cards

---

## User Interface

### Design System
- **Material Design 3**: Modern, consistent design language
- **Jetpack Compose**: Declarative UI framework
- **Custom Theme**: Purple primary color (#6200EE)
- **Responsive Layout**: Adapts to different screen sizes

### Screens

#### 1. Login Screen
- Email and password input fields
- Sign In button
- Navigation to registration
- Error message display
- Loading state indicator

#### 2. Registration Screen
- Email input field
- Password input field
- Confirm password field
- Password match validation
- Sign Up button
- Navigation to login
- Error handling

#### 3. Home Screen
- Top app bar with refresh and sign out
- Large wallet balance card
- Total earnings display
- Action buttons (Withdraw, History)
- Recent transactions list (5 most recent)
- Pull-to-refresh capability
- Loading and error states

#### 4. Withdrawal Screen
- Current balance display
- Special badge for first withdrawal
- Amount input field
- Fee calculation display (if applicable)
- Net amount preview
- Request button
- Success confirmation screen
- Back navigation

#### 5. Transaction History Screen
- Scrollable list of all transactions
- Transaction cards with details
- Status badges with icons
- Date formatting
- Empty state
- Refresh button
- Back navigation

### UI Components
- **Cards**: Elevated surfaces for content grouping
- **Buttons**: Primary and text buttons with loading states
- **Text Fields**: Outlined style with icons
- **Icons**: Material Icons Extended library
- **Status Chips**: Color-coded status indicators
- **Progress Indicators**: Loading spinners

---

## Architecture & Technical Features

### MVVM Architecture
```
UI Layer (Compose Screens)
    ↓
ViewModel Layer (Business Logic)
    ↓
Repository Layer (Data Coordination)
    ↓
Data Sources (Supabase Remote + DataStore Local)
```

### Key Components

#### 1. Data Layer
- **Models**: Kotlin data classes with serialization
- **Remote Source**: Supabase client for API calls
- **Local Source**: DataStore for preferences
- **Repository**: Single source of truth pattern

#### 2. Domain Layer
- **ViewModels**: State management and business logic
- **UI State**: Immutable state classes
- **Use Cases**: Implicitly handled in ViewModels

#### 3. UI Layer
- **Compose Screens**: Declarative UI components
- **Navigation**: Type-safe navigation with Compose
- **Theme**: Centralized theming with Material 3

### Asynchronous Operations
- **Kotlin Coroutines**: For background operations
- **Flows**: Reactive data streams
- **StateFlow**: UI state management
- **Suspend Functions**: Async/await pattern

### Local Persistence
- **DataStore Preferences**: For user session
- **Stored Data**:
  - User ID
  - Email address
- **Automatic Cleanup**: On sign out

### Network Operations
- **Supabase Client**: REST API integration
- **Ktor**: HTTP client for Supabase
- **Authentication**: JWT token management
- **Error Handling**: Try-catch with Result types

### Security Features

#### 1. Row Level Security (RLS)
- Users can only access their own data
- Enforced at database level
- Policies for each table

#### 2. Authentication
- Secure password storage (Supabase Auth)
- Session token management
- Automatic token refresh

#### 3. Input Validation
- Client-side validation for amounts
- Password confirmation
- Email format checking

#### 4. Backend-Driven Logic
- Wallet updates handled by database
- Transaction approval logic on backend
- Prevents client-side manipulation

### Performance Optimizations
- **Lazy Loading**: Efficient list rendering
- **State Hoisting**: Minimize recomposition
- **Remember**: Cached computations
- **Flow Collection**: Only active when needed
- **Efficient Queries**: Indexed database fields

### Error Handling
- **Result Types**: Success/Failure pattern
- **User-Friendly Messages**: Clear error descriptions
- **Retry Mechanisms**: Manual retry buttons
- **Loading States**: Visual feedback during operations
- **Network Error Detection**: Graceful degradation

---

## Future Enhancement Possibilities

### Short Term
- [ ] Email verification
- [ ] Password reset functionality
- [ ] Profile editing
- [ ] Transaction filtering and search
- [ ] Export transaction history

### Medium Term
- [ ] Push notifications for transaction updates
- [ ] Multiple withdrawal methods (UPI, Bank transfer)
- [ ] Referral system
- [ ] Achievement badges
- [ ] Dark mode support

### Long Term
- [ ] KYC integration
- [ ] Multi-language support
- [ ] Biometric authentication
- [ ] Advanced analytics dashboard
- [ ] Social features
- [ ] In-app customer support

---

## Testing Recommendations

### Manual Testing Checklist

1. **Authentication**
   - [ ] Register new account
   - [ ] Login with valid credentials
   - [ ] Login with invalid credentials
   - [ ] Sign out
   - [ ] Automatic login on app restart

2. **Wallet**
   - [ ] View initial balance (₹0)
   - [ ] Add test earning via backend
   - [ ] Verify balance updates
   - [ ] Refresh balance

3. **Withdrawal**
   - [ ] Attempt withdrawal below minimum
   - [ ] Attempt withdrawal above balance
   - [ ] Complete first withdrawal (free)
   - [ ] Complete second withdrawal (with fee)
   - [ ] Verify fee calculation

4. **Transactions**
   - [ ] View empty transaction list
   - [ ] View transactions after earning
   - [ ] View transactions after withdrawal
   - [ ] Check status badges
   - [ ] Verify transaction ordering

5. **UI/UX**
   - [ ] Check all screens render correctly
   - [ ] Test navigation flows
   - [ ] Verify loading states
   - [ ] Test error states
   - [ ] Check responsive design

### Automated Testing
- Unit tests for ViewModels
- Repository tests with mocked data sources
- UI tests for critical flows
- Integration tests for API calls

---

## API Reference

### Supabase Tables

#### profiles
```sql
user_id: UUID (PK)
email: TEXT
created_at: TIMESTAMP
```

#### wallets
```sql
user_id: UUID (PK)
balance: DECIMAL(10,2)
total_earnings: DECIMAL(10,2)
total_withdrawals: DECIMAL(10,2)
withdrawal_count: INTEGER
updated_at: TIMESTAMP
```

#### transactions
```sql
id: UUID (PK)
user_id: UUID (FK)
type: TEXT ('earning'|'withdrawal')
amount: DECIMAL(10,2)
status: TEXT ('pending'|'approved'|'rejected')
description: TEXT
created_at: TIMESTAMP
```

#### app_config
```sql
key: TEXT (PK)
value: TEXT
updated_at: TIMESTAMP
```

---

## Conclusion

RewardHub is a production-ready Android application that demonstrates modern Android development practices with:
- Clean architecture
- Material Design 3
- Jetpack Compose
- Supabase backend integration
- Secure authentication
- Real-time data synchronization
- User-friendly interface
- Scalable structure

The application is ready for deployment and can be extended with additional features as needed.
