# RewardHub Quick Start Guide

Get up and running with RewardHub in just a few minutes!

## Prerequisites

- Android Studio Arctic Fox or newer
- JDK 17+
- Android device or emulator (API 24+)
- Supabase account (free)

## 5-Minute Setup

### 1. Clone & Open (1 minute)

```bash
git clone https://github.com/bhumialokesh96-netizen/Data-selling-.git
cd Data-selling-
```

Open the project in Android Studio.

### 2. Setup Supabase (2 minutes)

1. Go to [supabase.com](https://supabase.com) and create a project
2. In Supabase SQL Editor, run:

```sql
-- Create tables
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE wallets (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    total_earnings DECIMAL(10, 2) DEFAULT 0.00,
    total_withdrawals DECIMAL(10, 2) DEFAULT 0.00,
    withdrawal_count INTEGER DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE transactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    type TEXT NOT NULL CHECK (type IN ('earning', 'withdrawal')),
    amount DECIMAL(10, 2) NOT NULL,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable RLS
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- Basic policies
CREATE POLICY "Users can view own profile" ON profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own profile" ON profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can view own wallet" ON wallets FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own wallet" ON wallets FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can view own transactions" ON transactions FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own transactions" ON transactions FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Auto-approve first withdrawal
CREATE OR REPLACE FUNCTION process_first_withdrawal()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.type = 'withdrawal' AND NEW.status = 'pending' THEN
        IF (SELECT withdrawal_count FROM wallets WHERE user_id = NEW.user_id) = 0 THEN
            NEW.status := 'approved';
            UPDATE wallets SET 
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

CREATE TRIGGER auto_process_first_withdrawal
    BEFORE INSERT ON transactions FOR EACH ROW
    EXECUTE FUNCTION process_first_withdrawal();

-- Auto-update wallet on earnings
CREATE OR REPLACE FUNCTION update_wallet_on_transaction()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.type = 'earning' AND NEW.status = 'approved' THEN
        UPDATE wallets SET 
            balance = balance + NEW.amount,
            total_earnings = total_earnings + NEW.amount,
            updated_at = NOW()
        WHERE user_id = NEW.user_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER auto_update_wallet_on_earning
    AFTER INSERT ON transactions FOR EACH ROW
    EXECUTE FUNCTION update_wallet_on_transaction();
```

3. Get your credentials from Settings → API

### 3. Configure App (1 minute)

Edit `app/src/main/java/com/rewardhub/app/data/remote/SupabaseConfig.kt`:

```kotlin
private const val SUPABASE_URL = "https://your-project.supabase.co"
private const val SUPABASE_KEY = "your-anon-key"
```

### 4. Build & Run (1 minute)

Click the green Run button in Android Studio or:

```bash
./gradlew assembleDebug
```

## Testing the App

### 1. Create Account
- Open app
- Click "Sign Up"
- Enter email and password
- Click "Sign Up"

### 2. Add Test Earnings

In Supabase SQL Editor:

```sql
INSERT INTO transactions (user_id, type, amount, status, description)
VALUES ('your-user-id', 'earning', 100.00, 'approved', 'Welcome bonus');
```

Get your `user_id` from the Supabase Authentication tab.

### 3. Test Withdrawal
- Click "Withdraw" on home screen
- Enter amount (min ₹10)
- First withdrawal is free!
- Click "Request Withdrawal"

## Common Issues

### Build Fails
```bash
./gradlew clean
./gradlew build
```

### Supabase Connection Error
- Check URL and API key
- Verify internet connection
- Check Supabase project is not paused

### Login Fails
- Verify email/password
- Check Supabase Auth settings
- Ensure RLS policies are created

## Next Steps

- Read [FEATURES.md](FEATURES.md) for detailed feature list
- Check [README.md](README.md) for complete documentation
- See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines

## Need Help?

- Open an issue on GitHub
- Check Supabase documentation
- Review Android Studio logs

---

**Happy Coding! 🚀**
