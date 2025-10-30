package com.tec.medxpert.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mock authentication provider for development purposes.
 * This class provides a simulated authenticated user ID while the real authentication
 * system is being developed.
 */
@Singleton
public class MockAuthProvider {

    private final FirebaseAuth firebaseAuth;
    private final String mockUserId = "mock_user_id";

    @Inject
    public MockAuthProvider(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * Get the current user ID. During development, this will return a mock user ID if no real user is authenticated.
     * @return The user ID (real or mock)
     */
    public String getUserId() {
        // Check if there's a real authenticated user first
        FirebaseUser realUser = firebaseAuth.getCurrentUser();
        if (realUser != null) {
            // If there's a real user, return their ID
            return realUser.getUid();
        }

        // Otherwise, return the mock user ID
        return mockUserId;
    }

    /**
     * Check if a user is authenticated. During development, this will always return true.
     * @return true indicating a user is authenticated
     */
    public boolean isAuthenticated() {
        return true; // Always return true during development
    }
}
