# Supabase Database Schema for RewardHub

This document describes the complete database schema required for the RewardHub application.

## Tables

### 1. profiles
Stores user profile information linked to Supabase Auth.

```sql
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

**Columns:**
- `user_id`: UUID - Primary key, references auth.users
- `email`: TEXT - User's email address
- `created_at`: TIMESTAMP - Account creation timestamp

### 2. wallets
Manages user wallet balances and transaction summaries.

```sql
CREATE TABLE wallets (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    total_earnings DECIMAL(10, 2) DEFAULT 0.00,
    total_withdrawals DECIMAL(10, 2) DEFAULT 0.00,
    withdrawal_count INTEGER DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

**Columns:**
- `user_id`: UUID - Primary key, references auth.users
- `balance`: DECIMAL(10, 2) - Current wallet balance
- `total_earnings`: DECIMAL(10, 2) - Cumulative earnings
- `total_withdrawals`: DECIMAL(10, 2) - Cumulative withdrawals
- `withdrawal_count`: INTEGER - Number of withdrawals made
- `updated_at`: TIMESTAMP - Last update timestamp

### 3. transactions
Records all financial transactions (earnings and withdrawals).

```sql
CREATE TABLE transactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    type TEXT NOT NULL CHECK (type IN ('earning', 'withdrawal')),
    amount DECIMAL(10, 2) NOT NULL,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

**Columns:**
- `id`: UUID - Primary key, auto-generated
- `user_id`: UUID - References auth.users
- `type`: TEXT - Transaction type ('earning' or 'withdrawal')
- `amount`: DECIMAL(10, 2) - Transaction amount
- `status`: TEXT - Transaction status ('pending', 'approved', or 'rejected')
- `description`: TEXT - Optional description
- `created_at`: TIMESTAMP - Transaction creation timestamp

### 4. app_config
Stores application-wide configuration values.

```sql
CREATE TABLE app_config (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

**Columns:**
- `key`: TEXT - Configuration key (primary key)
- `value`: TEXT - Configuration value
- `updated_at`: TIMESTAMP - Last update timestamp

## Row Level Security (RLS) Policies

### profiles table

```sql
-- Users can view their own profile
CREATE POLICY "Users can view their own profile"
    ON profiles FOR SELECT
    USING (auth.uid() = user_id);

-- Users can insert their own profile
CREATE POLICY "Users can insert their own profile"
    ON profiles FOR INSERT
    WITH CHECK (auth.uid() = user_id);
```

### wallets table

```sql
-- Users can view their own wallet
CREATE POLICY "Users can view their own wallet"
    ON wallets FOR SELECT
    USING (auth.uid() = user_id);

-- Users can insert their own wallet
CREATE POLICY "Users can insert their own wallet"
    ON wallets FOR INSERT
    WITH CHECK (auth.uid() = user_id);
```

### transactions table

```sql
-- Users can view their own transactions
CREATE POLICY "Users can view their own transactions"
    ON transactions FOR SELECT
    USING (auth.uid() = user_id);

-- Users can insert their own transactions
CREATE POLICY "Users can insert their own transactions"
    ON transactions FOR INSERT
    WITH CHECK (auth.uid() = user_id);
```

### app_config table

```sql
-- Anyone can read app config
CREATE POLICY "Anyone can read app config"
    ON app_config FOR SELECT
    USING (true);
```

## Indexes

For optimal query performance:

```sql
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
```

## Database Functions & Triggers

### Auto-process First Withdrawal

```sql
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

CREATE TRIGGER auto_process_first_withdrawal
    BEFORE INSERT ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION process_first_withdrawal();
```

### Auto-update Wallet on Earnings

```sql
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

CREATE TRIGGER auto_update_wallet_on_earning
    AFTER INSERT ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_wallet_on_transaction();
```

## Initial Configuration Data

```sql
-- Insert default app configuration
INSERT INTO app_config (key, value) VALUES
    ('min_withdrawal', '10.00'),
    ('processing_fee_percentage', '0.02'),
    ('max_withdrawal_per_day', '10000.00'),
    ('app_version', '1.0.0')
ON CONFLICT (key) DO NOTHING;
```

## Testing Queries

### Add Test User Data

```sql
-- Add a test earning for a user
INSERT INTO transactions (user_id, type, amount, status, description)
VALUES ('your-user-uuid', 'earning', 100.00, 'approved', 'Test earning');

-- Check wallet balance
SELECT * FROM wallets WHERE user_id = 'your-user-uuid';

-- View all transactions
SELECT * FROM transactions WHERE user_id = 'your-user-uuid' ORDER BY created_at DESC;
```

### Manually Process Withdrawal

```sql
-- Approve a pending withdrawal
UPDATE transactions 
SET status = 'approved' 
WHERE id = 'transaction-uuid' AND type = 'withdrawal';

-- Update wallet after approval
UPDATE wallets 
SET 
    balance = balance - (SELECT amount FROM transactions WHERE id = 'transaction-uuid'),
    total_withdrawals = total_withdrawals + (SELECT amount FROM transactions WHERE id = 'transaction-uuid'),
    withdrawal_count = withdrawal_count + 1,
    updated_at = NOW()
WHERE user_id = (SELECT user_id FROM transactions WHERE id = 'transaction-uuid');
```

## Security Notes

1. All tables have RLS enabled to ensure users can only access their own data
2. The `app_config` table is read-only for regular users
3. Sensitive operations (withdrawal approvals) should be handled by backend functions
4. Consider adding rate limiting for transaction creation
5. Monitor for suspicious patterns (multiple rapid withdrawals, etc.)

## Maintenance

### Regular Tasks

1. **Backup Database**: Set up automated backups in Supabase dashboard
2. **Monitor Storage**: Check database size and transaction volume
3. **Review Transactions**: Periodically audit transaction patterns
4. **Update Indexes**: Add indexes based on query patterns
5. **Clean Old Data**: Archive or remove old transactions if needed

### Performance Optimization

```sql
-- Analyze table statistics
ANALYZE profiles;
ANALYZE wallets;
ANALYZE transactions;
ANALYZE app_config;

-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

## Migration Guide

If you need to update the schema in production:

1. Always test migrations in a development environment first
2. Use Supabase migrations feature for version control
3. Backup database before applying migrations
4. Apply migrations during low-traffic periods
5. Monitor application logs after migration

## Support

For issues with the database schema:
- Check Supabase logs in the dashboard
- Review RLS policies if access is denied
- Ensure triggers are enabled and functioning
- Verify foreign key constraints are not causing issues
