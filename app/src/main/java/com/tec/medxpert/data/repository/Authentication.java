package com.tec.medxpert.data.repository;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import at.favre.lib.crypto.bcrypt.BCrypt;

import io.reactivex.rxjava3.core.Single;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import android.util.Log;

public class Authentication {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;


    @Inject
    public Authentication() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // Register with email and password
    public Single<AuthResult> signUpWithEmailAndPasswordRx(String email, String password) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        return Single.create(emitter -> {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                if (user != null) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", user.getEmail());
                    userData.put("password", hashedPassword);
                    userData.put("isActive", "false");
                    userData.put("role", "patient");
                    userData.put("createdAt", com.google.firebase.Timestamp.now());

                    firestore.collection("users").document(user.getUid()).set(userData)
                    .addOnSuccessListener(unused -> {
                        emitter.onSuccess(authResult);
                    })
                    .addOnFailureListener(emitter::onError);
                } else {
                    emitter.onError(new Exception("error_user_null"));
                }
            })
            .addOnFailureListener(emitter::onError);
        });
    }

    // Register with Google
    public Single<AuthResult> signUpWithGoogleRx(GoogleSignInAccount account) {
        return Single.create(emitter -> {
            String email = account.getEmail();
            if (email == null || email.isEmpty()) {
                emitter.onError(new Exception("error_email_null"));
                return;
            }

            // Verify if the account is already registered in Firestore
            firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener(snapshot -> {
                if (!snapshot.isEmpty()) {
                    String storedPassword = snapshot.getDocuments().get(0).getString("password");

                    if (storedPassword != null && !storedPassword.isEmpty()) {
                        // Account is registered with email and password
                        emitter.onError(new Exception("error_registered_with_email"));
                    } else {
                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                        firebaseAuth.signInWithCredential(credential)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email);
                                userData.put("password", "");
                                userData.put("isActive", "false");
                                userData.put("role", "patient");
                                userData.put("createdAt", com.google.firebase.Timestamp.now());

                                firestore.collection("users").document(user.getUid()).set(userData)
                                .addOnSuccessListener(unused -> emitter.onSuccess(authResult))
                                .addOnFailureListener(emitter::onError);
                            } else {
                                emitter.onError(new Exception("error_user_null"));
                            }
                        })
                        .addOnFailureListener(emitter::onError);
                    }
                } else {
                    // Account is not registered, proceed with Google authentication
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("password", "");
                            userData.put("isActive", "false");
                            userData.put("role", "patient");
                            userData.put("createdAt", com.google.firebase.Timestamp.now());

                            firestore.collection("users").document(user.getUid()).set(userData)
                            .addOnSuccessListener(unused -> emitter.onSuccess(authResult))
                            .addOnFailureListener(emitter::onError);
                        } else {
                            emitter.onError(new Exception("error_user_null"));
                        }
                    })
                    .addOnFailureListener(emitter::onError);
                }
            })
            .addOnFailureListener(emitter::onError);
        });
    }

    // Login with email and password
    public Single<Boolean> loginWithEmailRx(String email, String password, boolean rememberMe) {
        return Single.create(emitter -> {
            firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                if (user != null) {
                    String uid = user.getUid();
                    firestore.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Update isActive field
                            Boolean isDeleted = documentSnapshot.getBoolean("isDeleted");
                            if (isDeleted != null && isDeleted) {
                                emitter.onError(new Exception("error_registered_with_email"));
                            }
                            firestore.collection("users").document(uid)
                            .update("isActive", rememberMe ? "true" : "false")
                            .addOnSuccessListener(aVoid -> emitter.onSuccess(true))
                            .addOnFailureListener(emitter::onError);
                        } else {
                            emitter.onSuccess(false);
                        }
                    })
                    .addOnFailureListener(emitter::onError);
                } else {
                    emitter.onError(new Exception("error_user_null"));
                }
            })
            .addOnFailureListener(error -> {
                if (error instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                    emitter.onSuccess(false);
                } else if (error instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                    emitter.onSuccess(false);
                } else {
                    emitter.onError(error);
                }
            });
        });
    }

    public Single<Boolean> loginWithGoogleRx(GoogleSignInAccount account, boolean rememberMe) {
        return Single.create(emitter -> {
            String email = account.getEmail();
            if (email == null || email.isEmpty()) {
                emitter.onError(new Exception("error_email_null"));
                return;
            }

            // Verify if the account is already registered in Firestore
            firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener(snapshot -> {
                if (!snapshot.isEmpty()) {
                    String storedPassword = snapshot.getDocuments().get(0).getString("password");
                    Boolean isDeleted = snapshot.getDocuments().get(0).getBoolean("isDeleted");
                    if (isDeleted != null && isDeleted) {
                        emitter.onError(new Exception("error_registered_with_email"));
                    }
                    if (storedPassword != null && !storedPassword.isEmpty()) {
                        // Account is registered with email and password
                        emitter.onError(new Exception("error_registered_with_email"));
                    } else {
                        // Authenticate with Google
                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                        firebaseAuth.signInWithCredential(credential)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                String uid = user.getUid();
                                firestore.collection("users").document(uid)
                                .update("isActive", rememberMe ? "true" : "false")
                                .addOnSuccessListener(aVoid -> emitter.onSuccess(true))
                                .addOnFailureListener(emitter::onError);
                            } else {
                                emitter.onError(new Exception("error_user_null"));
                            }
                        })
                        .addOnFailureListener(emitter::onError);
                    }
                } else {
                    // Account does not exist, proceed with Google authentication
                    emitter.onSuccess(false);
                }
            })
            .addOnFailureListener(emitter::onError);
        });
    }

    // Logout here
    public void logout(Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // update isActive field to false
            firestore.collection("users").document(uid)
                    .update("isActive", "false")
                    .addOnSuccessListener(aVoid -> {
                        auth.signOut();

                        SharedPreferences preferences = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                    })
                    .addOnFailureListener(e -> {
                        auth.signOut();
                    });
        }
    }

    public void deactivateAccount(Context context) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("isDeleted", true);
            updates.put("isActive", false);

            firestore.collection("users").document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        firebaseAuth.signOut();
                        SharedPreferences preferences = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                    })
                    .addOnFailureListener(e -> {
                        firebaseAuth.signOut();
                    });
        }
    }




}

