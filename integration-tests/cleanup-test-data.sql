-- ============================================================================
-- OpenMarket Integration Test - Database Cleanup Script
-- ============================================================================
-- This script removes test data created by integration tests
-- Run this between test executions for a clean database state
-- ============================================================================

-- WARNING: This will delete test data! Use with caution.

-- Start transaction (rollback if something goes wrong)
BEGIN;

-- 1. Delete reviews for test products
DELETE FROM reviews
WHERE product_id IN (
    SELECT id FROM products WHERE name LIKE 'Test Product%'
);

-- 2. Delete order items for test orders
DELETE FROM order_items
WHERE order_id IN (
    SELECT o.id FROM orders o
    JOIN buyer_profiles bp ON o.buyer_profile_id = bp.id
    JOIN users u ON bp.user_id = u.external_auth_id
    WHERE u.email LIKE '%@test.com'
);

-- 3. Delete test orders
DELETE FROM orders
WHERE buyer_profile_id IN (
    SELECT bp.id FROM buyer_profiles bp
    JOIN users u ON bp.user_id = u.external_auth_id
    WHERE u.email LIKE '%@test.com'
)
OR seller_profile_id IN (
    SELECT sp.id FROM seller_profiles sp
    JOIN users u ON sp.user_id = u.external_auth_id
    WHERE u.email LIKE '%@test.com'
);

-- 4. Delete product images for test products
DELETE FROM product_images
WHERE product_id IN (
    SELECT id FROM products WHERE name LIKE 'Test Product%'
);

-- 5. Delete test products
DELETE FROM products
WHERE name LIKE 'Test Product%'
OR seller_profile_id IN (
    SELECT sp.id FROM seller_profiles sp
    JOIN users u ON sp.user_id = u.external_auth_id
    WHERE u.email LIKE '%@test.com'
);

-- 6. Delete test buyer profiles
DELETE FROM buyer_profiles
WHERE user_id IN (
    SELECT external_auth_id FROM users WHERE email LIKE 'buyer%@test.com'
);

-- 7. Delete test seller profiles
DELETE FROM seller_profiles
WHERE user_id IN (
    SELECT external_auth_id FROM users WHERE email LIKE 'seller%@test.com'
);

-- 8. Delete test users
DELETE FROM users
WHERE email LIKE '%@test.com';

-- Commit transaction if everything is OK
COMMIT;

-- Display summary
SELECT 'Cleanup completed successfully!' AS status;

-- Optional: Display remaining test data (should be empty)
SELECT 'Test users remaining:' AS check_type, COUNT(*) AS count
FROM users WHERE email LIKE '%@test.com'
UNION ALL
SELECT 'Test products remaining:', COUNT(*)
FROM products WHERE name LIKE 'Test Product%'
UNION ALL
SELECT 'Test seller profiles remaining:', COUNT(*)
FROM seller_profiles WHERE user_id IN (
    SELECT external_auth_id FROM users WHERE email LIKE '%@test.com'
)
UNION ALL
SELECT 'Test buyer profiles remaining:', COUNT(*)
FROM buyer_profiles WHERE user_id IN (
    SELECT external_auth_id FROM users WHERE email LIKE '%@test.com'
);
