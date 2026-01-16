-- =====================================================
-- Test Queries for RewardHub Database
-- =====================================================
-- Use these queries to test and verify database functionality
-- =====================================================

-- =====================================================
-- 1. MANUALLY INSERT TEST USER
-- =====================================================
-- Note: Replace 'your-user-uuid' with actual UUID from auth.users table
-- Note: Password should be hashed in production using bcrypt

-- Insert test profile
INSERT INTO profiles (user_id, phone_number, password)
VALUES (
    'your-user-uuid',
    '+1234567890',
    '$2a$12$example.hashed.password.here' -- Replace with actual bcrypt hash (cost factor 12)
);

-- Insert test wallet
INSERT INTO wallets (user_id, balance, total_earnings, total_withdrawals, withdrawal_count)
VALUES (
    'your-user-uuid',
    100.00,
    100.00,
    0.00,
    0
);

-- =====================================================
-- 2. ADD TEST EARNINGS
-- =====================================================

-- Add a test earning (auto-approved)
INSERT INTO transactions (user_id, type, amount, status, description)
VALUES ('your-user-uuid', 'earning', 50.00, 'approved', 'Test earning - Task completed');

-- Add another earning
INSERT INTO transactions (user_id, type, amount, status, description)
VALUES ('your-user-uuid', 'earning', 25.00, 'approved', 'Test earning - Survey completed');

-- =====================================================
-- 3. TEST WITHDRAWAL FLOW
-- =====================================================

-- Request first withdrawal (should auto-approve due to trigger)
INSERT INTO transactions (user_id, type, amount, status, description)
VALUES ('your-user-uuid', 'withdrawal', 10.00, 'pending', 'First withdrawal request');

-- Request second withdrawal (will remain pending)
INSERT INTO transactions (user_id, type, amount, status, description)
VALUES ('your-user-uuid', 'withdrawal', 20.00, 'pending', 'Second withdrawal request');

-- =====================================================
-- 4. MANUALLY APPROVE WITHDRAWAL
-- =====================================================

-- Approve a pending withdrawal
UPDATE transactions 
SET status = 'approved' 
WHERE id = 'transaction-uuid-here' AND type = 'withdrawal';

-- Update wallet after manual approval
UPDATE wallets 
SET 
    balance = balance - (SELECT amount FROM transactions WHERE id = 'transaction-uuid-here'),
    total_withdrawals = total_withdrawals + (SELECT amount FROM transactions WHERE id = 'transaction-uuid-here'),
    withdrawal_count = withdrawal_count + 1,
    updated_at = NOW()
WHERE user_id = (SELECT user_id FROM transactions WHERE id = 'transaction-uuid-here');

-- =====================================================
-- 5. QUERY USER DATA
-- =====================================================

-- View user profile
SELECT * FROM profiles WHERE user_id = 'your-user-uuid';

-- View user wallet
SELECT * FROM wallets WHERE user_id = 'your-user-uuid';

-- View all user transactions
SELECT * FROM transactions 
WHERE user_id = 'your-user-uuid' 
ORDER BY created_at DESC;

-- View pending withdrawals
SELECT * FROM transactions 
WHERE user_id = 'your-user-uuid' 
  AND type = 'withdrawal' 
  AND status = 'pending'
ORDER BY created_at DESC;

-- =====================================================
-- 6. SUMMARY QUERIES
-- =====================================================

-- User balance summary
SELECT 
    p.phone_number,
    w.balance,
    w.total_earnings,
    w.total_withdrawals,
    w.withdrawal_count
FROM profiles p
JOIN wallets w ON p.user_id = w.user_id
WHERE p.user_id = 'your-user-uuid';

-- Transaction summary by type
SELECT 
    type,
    status,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM transactions
WHERE user_id = 'your-user-uuid'
GROUP BY type, status
ORDER BY type, status;

-- =====================================================
-- 7. VERIFICATION QUERIES
-- =====================================================

-- Verify RLS policies exist
SELECT schemaname, tablename, policyname 
FROM pg_policies 
WHERE schemaname = 'public'
ORDER BY tablename, policyname;

-- Verify triggers exist
SELECT trigger_name, event_object_table, action_statement
FROM information_schema.triggers
WHERE trigger_schema = 'public'
ORDER BY event_object_table, trigger_name;

-- Verify indexes exist
SELECT indexname, tablename 
FROM pg_indexes 
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- =====================================================
-- 8. SEARCH BY PHONE NUMBER
-- =====================================================

-- Find user by phone number
SELECT 
    p.user_id,
    p.phone_number,
    p.created_at,
    w.balance,
    w.total_earnings
FROM profiles p
LEFT JOIN wallets w ON p.user_id = w.user_id
WHERE p.phone_number = '+1234567890';

-- =====================================================
-- 9. CLEANUP TEST DATA
-- =====================================================

-- Delete all transactions for a test user
DELETE FROM transactions WHERE user_id = 'your-user-uuid';

-- Delete wallet for a test user
DELETE FROM wallets WHERE user_id = 'your-user-uuid';

-- Delete profile for a test user
DELETE FROM profiles WHERE user_id = 'your-user-uuid';

-- Note: Deleting from auth.users will cascade to all related tables
-- DELETE FROM auth.users WHERE id = 'your-user-uuid';
