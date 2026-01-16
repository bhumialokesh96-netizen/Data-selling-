-- =====================================================
-- Complete Database Setup Script for RewardHub
-- =====================================================
-- This script sets up the entire database schema with
-- phone-based authentication and password storage
-- =====================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- 1. CREATE TABLES
-- =====================================================

-- Create profiles table with phone_number and password
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

-- =====================================================
-- 2. ENABLE ROW LEVEL SECURITY
-- =====================================================

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;

-- =====================================================
-- 3. CREATE RLS POLICIES
-- =====================================================

-- Profiles policies
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

-- Wallets policies
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

-- Transactions policies
CREATE POLICY "Users can view their own transactions"
    ON transactions FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own transactions"
    ON transactions FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- App config policies
CREATE POLICY "Anyone can read app config"
    ON app_config FOR SELECT
    USING (true);

-- =====================================================
-- 4. CREATE INDEXES FOR PERFORMANCE
-- =====================================================

CREATE INDEX idx_profiles_phone_number ON profiles(phone_number);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_wallets_user_id ON wallets(user_id);

-- =====================================================
-- 5. CREATE FUNCTIONS AND TRIGGERS
-- =====================================================

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

-- Function to update wallet on approved earnings
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

-- =====================================================
-- 6. INSERT DEFAULT CONFIGURATION
-- =====================================================

INSERT INTO app_config (key, value) VALUES
    ('min_withdrawal', '10.00'),
    ('processing_fee_percentage', '0.02'),
    ('max_withdrawal_per_day', '10000.00'),
    ('app_version', '1.0.0')
ON CONFLICT (key) DO NOTHING;

-- =====================================================
-- Setup Complete!
-- =====================================================

SELECT 'Database setup completed successfully!' as status;
