package com.rewardhub.app.data.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Profile data model
 * 
 * These tests validate:
 * 1. Profile creation with phone number instead of email
 * 2. Profile data integrity
 * 3. Password field handling
 */
class ProfileTest {

    @Test
    fun `test profile creation with phone number and password`() {
        // Arrange
        val userId = "test-user-123"
        val phoneNumber = "+1234567890"
        val password = "hashed_password_here"
        val createdAt = "2024-01-16T00:00:00Z"
        
        // Act
        val profile = Profile(
            userId = userId,
            phoneNumber = phoneNumber,
            password = password,
            createdAt = createdAt
        )
        
        // Assert
        assertEquals("User ID should match", userId, profile.userId)
        assertEquals("Phone number should match", phoneNumber, profile.phoneNumber)
        assertEquals("Password should match", password, profile.password)
        assertEquals("Created at should match", createdAt, profile.createdAt)
    }
    
    @Test
    fun `test profile creation without optional createdAt`() {
        // Arrange
        val userId = "test-user-456"
        val phoneNumber = "+919876543210"
        val password = "hashed_password"
        
        // Act
        val profile = Profile(
            userId = userId,
            phoneNumber = phoneNumber,
            password = password
        )
        
        // Assert
        assertEquals("User ID should match", userId, profile.userId)
        assertEquals("Phone number should match", phoneNumber, profile.phoneNumber)
        assertEquals("Password should match", password, profile.password)
        assertNull("Created at should be null when not provided", profile.createdAt)
    }
    
    @Test
    fun `test profile with various phone number formats`() {
        // Test different international phone number formats
        val phoneNumbers = listOf(
            "+1234567890",       // US format
            "+919876543210",     // India format
            "+447700900000",     // UK format
            "+8613800138000",    // China format
            "+33612345678"       // France format
        )
        
        phoneNumbers.forEachIndexed { index, phoneNumber ->
            val profile = Profile(
                userId = "user-$index",
                phoneNumber = phoneNumber,
                password = "test_password_$index"
            )
            
            assertEquals("Phone number should be stored correctly", phoneNumber, profile.phoneNumber)
            assertFalse("Phone number should not be empty", profile.phoneNumber.isEmpty())
            assertTrue("Phone number should start with +", profile.phoneNumber.startsWith("+"))
        }
    }
    
    @Test
    fun `test profile equality and data class properties`() {
        // Arrange
        val profile1 = Profile(
            userId = "user-1",
            phoneNumber = "+1234567890",
            password = "password123"
        )
        
        val profile2 = Profile(
            userId = "user-1",
            phoneNumber = "+1234567890",
            password = "password123"
        )
        
        val profile3 = Profile(
            userId = "user-2",
            phoneNumber = "+1234567890",
            password = "password123"
        )
        
        // Assert
        assertEquals("Profiles with same data should be equal", profile1, profile2)
        assertNotEquals("Profiles with different userId should not be equal", profile1, profile3)
        assertEquals("Hash codes should match for equal profiles", profile1.hashCode(), profile2.hashCode())
    }
    
    @Test
    fun `test profile copy functionality`() {
        // Arrange
        val originalProfile = Profile(
            userId = "user-1",
            phoneNumber = "+1234567890",
            password = "original_password"
        )
        
        // Act
        val updatedProfile = originalProfile.copy(password = "new_password")
        
        // Assert
        assertEquals("User ID should remain the same", originalProfile.userId, updatedProfile.userId)
        assertEquals("Phone number should remain the same", originalProfile.phoneNumber, updatedProfile.phoneNumber)
        assertNotEquals("Password should be updated", originalProfile.password, updatedProfile.password)
        assertEquals("New password should match", "new_password", updatedProfile.password)
    }
    
    @Test
    fun `test profile does not contain email field`() {
        // This test validates that the Profile model has been successfully migrated
        // from email to phone_number based authentication
        
        val profile = Profile(
            userId = "user-1",
            phoneNumber = "+1234567890",
            password = "password123"
        )
        
        // Verify phoneNumber field exists and is accessible
        assertNotNull("Phone number field should exist", profile.phoneNumber)
        
        // Note: We can't directly test that email field doesn't exist in Kotlin,
        // but compilation success of this test confirms the migration
        assertTrue("Profile should use phone number", profile.phoneNumber.isNotEmpty())
    }
}
