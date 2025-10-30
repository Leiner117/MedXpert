package com.tec.medxpert.navigation.profile;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;
import com.tec.medxpert.ui.home.DoctorFragment;
import com.tec.medxpert.ui.home.PatientHomeFragment;

/**
 * Utility class to handle navigation based on user role
 */
public class UserRoleNavigator {
    private static final String TAG = "UserRoleNavigator";

    /**
     * Navigate to the appropriate home screen based on user role
     * @param context The context to use for navigation
     */
    public static void navigateBasedOnRole(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user is currently logged in");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null) {
                            if ("doctor".equals(role)) {
                                navigateToDoctorHome(context);
                            } else if ("patient".equals(role)) {
                                navigateToPatientHome(context);
                            } else {
                                Log.w(TAG, "Unknown role: " + role);
                                Toast.makeText(context, "Unknown user role", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w(TAG, "User role not set");
                            Toast.makeText(context, "User role not set", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "User document does not exist");
                        Toast.makeText(context, "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user role", e);
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigate to doctor home screen
     * @param context The context to use for navigation
     */
    private static void navigateToDoctorHome(Context context) {
        Intent intent = new Intent(context, DoctorFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Navigate to patient home screen
     * @param context The context to use for navigation
     */
    private static void navigateToPatientHome(Context context) {
        PatientHomeFragment patientHomeFragment = new PatientHomeFragment();
        ((AppCompatActivity) context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, patientHomeFragment)
                .addToBackStack(null)
                .commit();
        //TODO: limpiar la pila de actividades
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    /**
     * Load the appropriate home fragment based on user role
     * @param activity The activity containing the fragment container
     * @param containerId The ID of the fragment container
     */
    public static void loadHomeFragment(FragmentActivity activity, int containerId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user is currently logged in");
            return;
        }

        // Show loading indicator if needed
        // activity.showLoadingIndicator();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        if ("doctor".equals(role)) {
                            transaction.replace(containerId, new DoctorFragment());
                            Log.d(TAG, "Loading doctor home fragment");
                        } else if ("patient".equals(role)) {
                            transaction.replace(containerId, new PatientHomeFragment());
                            Log.d(TAG, "Loading patient home fragment");
                        } else {
                            Log.w(TAG, "Unknown role: " + role);
                            // Default to patient home for unknown roles
                            transaction.replace(containerId, new PatientHomeFragment());
                            Toast.makeText(activity, "Unknown user role, defaulting to patient view", Toast.LENGTH_SHORT).show();
                        }

                        transaction.commit();
                    } else {
                        Log.w(TAG, "User document does not exist");
                        // Default to patient home if user document doesn't exist
                        FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(containerId, new PatientHomeFragment())
                                .commit();
                        Toast.makeText(activity, "User profile not found, defaulting to patient view", Toast.LENGTH_SHORT).show();
                    }

                    // Hide loading indicator if needed
                    // activity.hideLoadingIndicator();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user role", e);
                    // Hide loading indicator if needed
                    // activity.hideLoadingIndicator();

                    // Default to patient home on error
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(containerId, new PatientHomeFragment())
                            .commit();
                    Toast.makeText(activity, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
