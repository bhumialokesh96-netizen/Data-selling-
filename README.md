# RewardHub - Real Earning & Rewards Application

RewardHub is a modern Android application designed for real-time earnings and rewards management. Built with Kotlin, Jetpack Compose, and powered by Supabase backend, it provides a seamless user experience with production-grade features.

## Features

### 🎯 Core Functionality
- **User Authentication**: Simple registration and login system using phone numbers and passwords
- **Secure Password Storage**: Passwords are hashed using BCrypt before storage
- **Real-time Wallet**: Track your balance, earnings, and withdrawals in real-time
- **Withdrawal System**: 
  - First withdrawal of ₹10 is free and auto-approved
  - Subsequent withdrawals include a 2% processing fee
  - Secure transaction processing through backend
- **Transaction History**: Complete view of all earnings and withdrawals with status tracking
- **Modern UI**: Built with Material Design 3 and Jetpack Compose for a sleek user interface

### 🔒 Security
- Row Level Security (RLS) on Supabase database
- Secure authentication with phone numbers and passwords
- BCrypt password hashing with cost factor 12
- Backend-driven business logic for wallet operations
- Rate limiting and abuse detection capabilities

### 🏗️ Architecture
- **MVVM Pattern**: Clean separation of concerns
- **Kotlin Coroutines & Flows**: Asynchronous operations and reactive data streams
- **DataStore**: Secure local storage for user preferences
- **Jetpack Compose**: Modern declarative UI framework

## Prerequisites

Before you begin, ensure you have:
- Android Studio (latest version recommended)
- JDK 17 or higher
- Android SDK with minimum API level 24
- A Supabase account (free tier available at [supabase.com](https://supabase.com))

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/bhumialokesh96-netizen/Data-selling-.git
cd Data-selling-
```

### 2. Supabase Backend Setup

#### 2.1 Create a Supabase Project
1. Go to [supabase.com](https://supabase.com) and sign up/login
2. Click "New Project"
3. Fill in the project details and wait for the project to be created

#### 2.2 Create Database Tables

Run the complete setup SQL script in your Supabase SQL Editor. This script is available at `database/sql/complete_setup.sql` and includes:

- Tables with phone-based authentication schema
- Row Level Security policies
- Indexes for performance
- Functions and triggers for business logic
- Default app configuration

Alternatively, you can run the SQL manually:

```sql
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create profiles table with phone number and password
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    phone_number TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create wallets table
CREATE TABLE wallets (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    total_earnings DECIMAL(10, 2) DEFAULT 0.00,
    total_withdrawals DECIMAL(10, 2) DEFAULT 0.00,
    withdrawal_count INTEGER DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create transactions table
CREATE TABLE transactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    type TEXT NOT NULL CHECK (type IN ('earning', 'withdrawal')),
    amount DECIMAL(10, 2) NOT NULL,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create app_config table
CREATE TABLE app_config (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;

-- RLS Policies for profiles
CREATE POLICY "Users can view their own profile"
    ON profiles FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own profile"
    ON profiles FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own profile"
    ON profiles FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- RLS Policies for wallets
CREATE POLICY "Users can view their own wallet"
    ON wallets FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own wallet"
    ON wallets FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own wallet"
    ON wallets FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- RLS Policies for transactions
CREATE POLICY "Users can view their own transactions"
    ON transactions FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own transactions"
    ON transactions FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- RLS Policies for app_config
CREATE POLICY "Anyone can read app config"
    ON app_config FOR SELECT
    USING (true);

-- Create indexes for better performance
CREATE INDEX idx_profiles_phone_number ON profiles(phone_number);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
```

**For complete setup including triggers and functions, see** `database/sql/complete_setup.sql`

#### 2.3 Set Up Database Functions (Optional - for auto-processing)

The complete setup script (`database/sql/complete_setup.sql`) already includes these functions. If you need to add them separately:

```sql
-- Function to process first withdrawal automatically
CREATE OR REPLACE FUNCTION process_first_withdrawal()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.type = 'withdrawal' AND NEW.status = 'pending' THEN
        -- Check if this is the first withdrawal
        IF (SELECT withdrawal_count FROM wallets WHERE user_id = NEW.user_id) = 0 THEN
            -- Auto-approve first withdrawal
            NEW.status := 'approved';
            
            -- Update wallet
            UPDATE wallets
            SET 
                balance = balance - NEW.amount,
                total_withdrawals = total_withdrawals + NEW.amount,
                withdrawal_count = withdrawal_count + 1,
                updated_at = NOW()
            WHERE user_id = NEW.user_id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for first withdrawal
CREATE TRIGGER auto_process_first_withdrawal
    BEFORE INSERT ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION process_first_withdrawal();

-- Function to update wallet on approved transactions
CREATE OR REPLACE FUNCTION update_wallet_on_transaction()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.type = 'earning' AND NEW.status = 'approved' THEN
        UPDATE wallets
        SET 
            balance = balance + NEW.amount,
            total_earnings = total_earnings + NEW.amount,
            updated_at = NOW()
        WHERE user_id = NEW.user_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for earnings
CREATE TRIGGER auto_update_wallet_on_earning
    AFTER INSERT ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_wallet_on_transaction();
```

### 3. Configure the Android App

#### 3.1 Update Supabase Configuration

Edit `app/src/main/java/com/rewardhub/app/data/remote/SupabaseConfig.kt`:

```kotlin
object SupabaseConfig {
    // Replace with your actual Supabase credentials
    private const val SUPABASE_URL = "https://your-project-id.supabase.co"
    private const val SUPABASE_KEY = "your-anon-public-key"
    
    // ... rest of the code
}
```

**To find your credentials:**
1. Go to your Supabase project dashboard
2. Click on "Settings" → "API"
3. Copy the "Project URL" and "anon public" key

### 4. Build and Run

#### 4.1 Open in Android Studio
1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the cloned repository and select it

#### 4.2 Sync Gradle
- Android Studio should automatically sync Gradle
- If not, click "File" → "Sync Project with Gradle Files"

#### 4.3 Run the App
1. Connect an Android device or start an emulator
2. Click the "Run" button (green triangle) or press `Shift + F10`
3. Select your device/emulator

## Project Structure

```
RewardHub/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rewardhub/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/          # Data models
│   │   │   │   │   ├── local/          # DataStore for local storage
│   │   │   │   │   ├── remote/         # Supabase API integration
│   │   │   │   │   └── repository/     # Repository pattern implementation
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/        # Compose screens
│   │   │   │   │   │   ├── auth/       # Login & Registration
│   │   │   │   │   │   ├── home/       # Home screen with wallet
│   │   │   │   │   │   ├── withdrawal/ # Withdrawal flow
│   │   │   │   │   │   └── transactions/ # Transaction history
│   │   │   │   │   ├── navigation/     # Navigation setup
│   │   │   │   │   └── theme/          # Material Design 3 theme
│   │   │   │   ├── MainActivity.kt     # Main entry point
│   │   │   │   └── RewardHubApplication.kt
│   │   │   ├── res/                    # Resources (strings, colors, etc.)
│   │   │   └── AndroidManifest.xml
│   │   └── test/                       # Unit tests
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Key Dependencies

- **Jetpack Compose**: Modern UI toolkit
- **Supabase Kotlin SDK**: Backend integration
- **Kotlin Coroutines**: Asynchronous programming
- **Navigation Component**: Screen navigation
- **DataStore**: Local data persistence
- **Material Design 3**: UI components and theming

## Usage Guide

### For End Users

1. **Registration**:
   - Open the app
   - Click "Sign Up"
   - Enter your phone number (format: +1234567890)
   - Enter and confirm password
   - Click "Sign Up" button

2. **Login**:
   - Enter registered phone number
   - Enter password
   - Click "Sign In"

3. **View Wallet**:
   - Home screen displays current balance and total earnings
   - Recent transactions are shown below

4. **Withdraw Funds**:
   - Click "Withdraw" button on home screen
   - Enter amount (minimum ₹10)
   - First withdrawal is free (no processing fee)
   - Subsequent withdrawals have 2% processing fee
   - Click "Request Withdrawal"

5. **View Transaction History**:
   - Click "History" button on home screen
   - See all your earnings and withdrawals with status

### For Developers

#### Database Management

**Factory Reset (Development/Testing Only)**

To completely reset the database, run `database/sql/factory_reset.sql` in Supabase SQL Editor. This will drop all tables, policies, functions, and triggers.

**Complete Setup**

After factory reset or for initial setup, run `database/sql/complete_setup.sql` to recreate the entire database schema with phone-based authentication.

**Test Queries**

Use `database/sql/test_queries.sql` for manual testing and verification of database functionality.

#### Adding Test Earnings

You can add test earnings directly through Supabase:

```sql
-- Add a test earning
INSERT INTO transactions (user_id, type, amount, status, description)
VALUES ('user-uuid-here', 'earning', 100.00, 'approved', 'Test earning');
```

#### Creating Test Users

```sql
-- Add test user profile (password should be bcrypt hashed)
INSERT INTO profiles (user_id, phone_number, password)
VALUES (
    'user-uuid-here',
    '+1234567890',
    '$2a$12$hashed.password.here'
);
```

#### Manually Approving Withdrawals

```sql
-- Approve a withdrawal and update wallet
UPDATE transactions 
SET status = 'approved' 
WHERE id = 'transaction-uuid';

UPDATE wallets 
SET 
    balance = balance - (SELECT amount FROM transactions WHERE id = 'transaction-uuid'),
    total_withdrawals = total_withdrawals + (SELECT amount FROM transactions WHERE id = 'transaction-uuid'),
    withdrawal_count = withdrawal_count + 1
WHERE user_id = 'user-uuid';
```

## Security Considerations

- Never commit your Supabase credentials to version control
- Use environment variables or secure configuration for production
- Passwords are hashed using BCrypt with cost factor 12
- Phone numbers are stored with country code prefix (e.g., +1234567890)
- Implement rate limiting for API calls
- Regularly update dependencies to patch security vulnerabilities
- Monitor suspicious activity through Supabase dashboard

## Troubleshooting

### Common Issues

1. **Build Errors**:
   - Clean and rebuild: `Build` → `Clean Project` → `Rebuild Project`
   - Invalidate caches: `File` → `Invalidate Caches / Restart`

2. **Network Errors**:
   - Check internet connection
   - Verify Supabase credentials are correct
   - Ensure Supabase project is not paused

3. **Login/Registration Fails**:
   - Check Supabase Auth settings
   - Verify email confirmation is disabled (for testing)
   - Check database tables are created correctly
   - Ensure phone number is in correct format (+1234567890)
   - Verify password meets minimum requirements

## Future Enhancements

- [ ] Email verification
- [ ] Push notifications for transaction updates
- [ ] Multiple payment methods
- [ ] Referral system
- [ ] Dark mode support
- [ ] Multi-language support
- [ ] Analytics dashboard
- [ ] KYC integration

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit your changes: `git commit -am 'Add feature'`
4. Push to the branch: `git push origin feature-name`
5. Submit a pull request

## License

This project is available under the MIT License.

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Contact: [Project Maintainer]

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Backend powered by [Supabase](https://supabase.com)
- Material Design by [Google](https://m3.material.io/)

---

**Note**: This is a demonstration application. For production use, implement additional security measures, proper error handling, and compliance with financial regulations.