# Database SQL Scripts

This directory contains SQL scripts for managing the RewardHub database with phone-based authentication.

## Scripts Overview

### 1. `factory_reset.sql`
**Purpose**: Completely reset the database by dropping all objects.

**What it does**:
- Drops all RLS policies
- Drops all triggers
- Drops all functions
- Drops all tables (profiles, wallets, transactions, app_config)
- Drops all sequences
- Drops all views
- Drops all custom indexes

**Warning**: This is a destructive operation that will delete ALL data. Only use in development/testing environments.

**Usage**:
1. Open Supabase SQL Editor
2. Copy and paste the contents of `factory_reset.sql`
3. Execute the script
4. Verify with: `SELECT 'Factory reset completed successfully!' as status;`

### 2. `complete_setup.sql`
**Purpose**: Set up the entire database schema from scratch.

**What it does**:
- Creates all tables with phone-based authentication schema:
  - `profiles` (with phone_number and password fields)
  - `wallets`
  - `transactions`
  - `app_config`
- Enables Row Level Security (RLS) on all tables
- Creates RLS policies based on `auth.uid()`
- Creates performance indexes
- Creates database functions:
  - `process_first_withdrawal()` - Auto-approves first withdrawal
  - `update_wallet_on_transaction()` - Updates wallet on earnings
- Creates triggers:
  - `auto_process_first_withdrawal`
  - `auto_update_wallet_on_earning`
- Inserts default app configuration

**Usage**:
1. Run `factory_reset.sql` first (if resetting)
2. Open Supabase SQL Editor
3. Copy and paste the contents of `complete_setup.sql`
4. Execute the script
5. Verify tables created: `SELECT tablename FROM pg_tables WHERE schemaname = 'public';`

### 3. `test_queries.sql`
**Purpose**: Collection of useful queries for testing and verification.

**Categories**:
1. **Manual Test User Insertion** - Insert test profiles with phone numbers
2. **Test Earnings** - Add test earning transactions
3. **Test Withdrawal Flow** - Test withdrawal requests and approvals
4. **Manual Withdrawal Approval** - Approve pending withdrawals
5. **Query User Data** - View profiles, wallets, and transactions
6. **Summary Queries** - Get user balance summaries and transaction stats
7. **Verification Queries** - Check policies, triggers, and indexes
8. **Search by Phone Number** - Find users by phone number
9. **Cleanup Test Data** - Remove test users and transactions

**Usage**:
- Copy individual queries as needed
- Replace placeholder values (e.g., `'your-user-uuid'`, `'+1234567890'`)
- Execute in Supabase SQL Editor

## Workflow Examples

### Initial Database Setup
```sql
-- Step 1: Run complete_setup.sql
-- This creates all tables, policies, functions, and triggers
```

### Reset and Reinitialize
```sql
-- Step 1: Run factory_reset.sql
-- Step 2: Run complete_setup.sql
-- Step 3: Use test_queries.sql to add test data
```

### Add Test User
```sql
-- From test_queries.sql, customize and run:
INSERT INTO profiles (user_id, phone_number, password)
VALUES (
    'your-user-uuid',
    '+1234567890',
    '$2a$12$example.hashed.password.here'
);
```

## Key Schema Changes

### Phone-Based Authentication
The database now uses phone numbers instead of emails:

**Old Schema (Email-based)**:
```sql
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    email TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

**New Schema (Phone-based)**:
```sql
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    phone_number TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### Password Storage
- Passwords are hashed using BCrypt with cost factor 12
- Format: `$2a$12$...` (BCrypt identifier + salt + hash)
- Never store plain text passwords

### RLS Policies
All policies now use `auth.uid()` for access control:
```sql
CREATE POLICY "Users can view their own profile"
    ON profiles FOR SELECT
    USING (auth.uid() = user_id);
```

## Security Notes

1. **Factory Reset**: Only use in development. Never run in production.
2. **Passwords**: Always use BCrypt hashed passwords, never plain text.
3. **Phone Numbers**: Store with country code prefix (e.g., +1234567890).
4. **RLS**: All tables have Row Level Security enabled.
5. **Backups**: Always backup before running factory reset.

## Troubleshooting

### Error: "relation does not exist"
**Solution**: Run `complete_setup.sql` to create tables.

### Error: "policy already exists"
**Solution**: Run `factory_reset.sql` first, then `complete_setup.sql`.

### Can't insert/query data
**Solution**: Check RLS policies are created correctly and auth.uid() returns valid user ID.

### Triggers not firing
**Solution**: Verify triggers exist with verification query from `test_queries.sql`.

## Related Documentation

- See `SUPABASE_SCHEMA.md` for detailed schema documentation
- See `README.md` for application setup instructions
- See Supabase documentation for RLS and auth details
