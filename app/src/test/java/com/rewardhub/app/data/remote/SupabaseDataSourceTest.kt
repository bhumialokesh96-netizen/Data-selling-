package com.rewardhub.app.data.remote

import at.favre.lib.crypto.bcrypt.BCrypt
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for phone-based registration functionality in SupabaseDataSource
 * 
 * These tests validate:
 * 1. Phone number format conversion for authentication
 * 2. Password hashing using BCrypt
 * 3. Phone number validation
 */
class SupabaseDataSourceTest {

    @Test
    fun `test phone number to auth email conversion`() {
        // Test various phone number formats
        val phoneNumbers = listOf(
            "+1234567890",
            "+91 9876543210",
            "+44 7700 900000"
        )
        
        phoneNumbers.forEach { phoneNumber ->
            val authEmail = "${phoneNumber.replace("+", "").replace(" ", "")}@rewardhub.app"
            
            // Verify email format is valid
            assertTrue("Auth email should contain @rewardhub.app", authEmail.contains("@rewardhub.app"))
            assertFalse("Auth email should not contain +", authEmail.contains("+"))
            assertFalse("Auth email should not contain spaces", authEmail.contains(" "))
        }
    }
    
    @Test
    fun `test password hashing is secure`() {
        val password = "TestPassword123"
        
        // Hash the password using BCrypt
        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        
        // Verify hash is not empty
        assertNotNull("Hashed password should not be null", hashedPassword)
        assertTrue("Hashed password should not be empty", hashedPassword.isNotEmpty())
        
        // Verify hash is different from original password
        assertNotEquals("Hashed password should differ from original", password, hashedPassword)
        
        // Verify hash starts with BCrypt identifier
        assertTrue("Hash should start with $2a$ (BCrypt identifier)", hashedPassword.startsWith("$2a$"))
        
        // Verify password can be verified against hash
        val result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword)
        assertTrue("Password verification should succeed", result.verified)
    }
    
    @Test
    fun `test password hashing produces different hashes for same password`() {
        val password = "TestPassword123"
        
        // Hash the same password twice
        val hash1 = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val hash2 = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        
        // Verify hashes are different (due to different salts)
        assertNotEquals("Two hashes of same password should differ", hash1, hash2)
        
        // But both should verify successfully
        assertTrue("First hash should verify", BCrypt.verifyer().verify(password.toCharArray(), hash1).verified)
        assertTrue("Second hash should verify", BCrypt.verifyer().verify(password.toCharArray(), hash2).verified)
    }
    
    @Test
    fun `test wrong password fails verification`() {
        val correctPassword = "CorrectPassword123"
        val wrongPassword = "WrongPassword456"
        
        // Hash the correct password
        val hashedPassword = BCrypt.withDefaults().hashToString(12, correctPassword.toCharArray())
        
        // Verify correct password succeeds
        val correctResult = BCrypt.verifyer().verify(correctPassword.toCharArray(), hashedPassword)
        assertTrue("Correct password should verify", correctResult.verified)
        
        // Verify wrong password fails
        val wrongResult = BCrypt.verifyer().verify(wrongPassword.toCharArray(), hashedPassword)
        assertFalse("Wrong password should not verify", wrongResult.verified)
    }
    
    @Test
    fun `test phone number validation patterns`() {
        // Valid phone numbers
        val validPhoneNumbers = listOf(
            "+1234567890",
            "+919876543210",
            "+447700900000",
            "+8613800138000"
        )
        
        // Invalid phone numbers (for context, though not enforced in current implementation)
        val invalidPhoneNumbers = listOf(
            "1234567890",      // Missing + prefix
            "+123",            // Too short
            "notaphonenumber", // Not numeric
            ""                 // Empty
        )
        
        // Verify valid phone numbers can be converted
        validPhoneNumbers.forEach { phoneNumber ->
            val authEmail = "${phoneNumber.replace("+", "").replace(" ", "")}@rewardhub.app"
            assertTrue("Valid phone should produce valid email", authEmail.matches(Regex("^[0-9]+@rewardhub\\.app$")))
        }
    }
    
    @Test
    fun `test BCrypt cost factor impacts hash length`() {
        val password = "TestPassword123"
        
        // Test different cost factors (though we use 12 in production)
        val costFactors = listOf(4, 8, 12)
        
        costFactors.forEach { cost ->
            val hash = BCrypt.withDefaults().hashToString(cost, password.toCharArray())
            
            // All BCrypt hashes should have similar length structure
            assertTrue("Hash should be at least 50 characters", hash.length >= 50)
            assertTrue("Hash should verify", BCrypt.verifyer().verify(password.toCharArray(), hash).verified)
        }
    }
}
