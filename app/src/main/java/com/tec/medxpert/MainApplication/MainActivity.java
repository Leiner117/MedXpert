package com.tec.medxpert.MainApplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;
import com.tec.medxpert.ui.chat.ChatFragment;
import com.tec.medxpert.ui.chat.ChatListFragment;
import com.tec.medxpert.ui.chat.ChatsListViewModel;
import com.tec.medxpert.ui.home.DoctorFragment;
import com.tec.medxpert.ui.home.PatientHomeFragment;
import com.tec.medxpert.ui.profile.doctor.RegisterDoctorFragment;
import com.tec.medxpert.ui.profile.patient.RegisterPatientFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String userRole = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                loadHomeBasedOnRole();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Navigate to profile
                if ("doctor".equals(userRole)) {
                    loadFragment(new RegisterDoctorFragment());
                } else {
                    //loadFragment(new PatientProfileFragment());
                    loadFragment(new RegisterPatientFragment());
                }
                return true;
            } else if (itemId == R.id.navigation_chat) {
                if ("doctor".equals(userRole)) {
                    // Si es doctor, muestra la lista de chats
                    ChatsListViewModel viewModel = new ViewModelProvider(this).get(ChatsListViewModel.class);
                    viewModel.loadPatients();
                    loadFragment(new ChatListFragment());
                } else {
                    String doctorUid = "7oqLYPG2ZxTXYxzdgqHVEqtGaSm1";
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String patientUid = user != null ? user.getUid() : "";
                    String patientName = user != null ? user.getDisplayName() : "Paciente";
                    Bundle args = new Bundle();
                    args.putString("doctorUid", doctorUid);
                    args.putString("doctorUid", doctorUid);
                    args.putString("doctorName", "Dr. José Pablo Badilla Peralta");
                    args.putString("patientUid", patientUid);
                    args.putString("patientName", patientName);
                    ChatFragment chatFragment = new ChatFragment();
                    chatFragment.setArguments(args);
                    loadFragment(chatFragment);
                }
                return true;
            }

            return false;
        });

        // Check user role and load appropriate home fragment
        if (savedInstanceState == null) {
            checkUserRoleAndLoadHome();
        }
    }

    private void checkUserRoleAndLoadHome() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user is currently logged in");
            loadFragment(new PatientHomeFragment()); // Default to patient home
            return;
        }

        // Show loading indicator if needed
        // showLoadingIndicator();

        Log.d(TAG, "Checking role for user: " + currentUser.getUid());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Hide loading indicator
                    // hideLoadingIndicator();

                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        Log.d(TAG, "User role from Firestore: " + role);

                        if (role != null) {
                            userRole = role.toLowerCase().trim(); // Normalize the role string
                            Log.d(TAG, "Normalized user role: " + userRole);

                            // Debug toast to verify role
                            // Toast.makeText(this, "User role: " + userRole, Toast.LENGTH_SHORT).show();

                            loadHomeBasedOnRole();
                        } else {
                            Log.w(TAG, "Role field is null in Firestore");
                            userRole = "patient"; // Default to patient if role is null
                            loadFragment(new PatientHomeFragment());
                        }
                    } else {
                        Log.w(TAG, "User document does not exist in Firestore");
                        userRole = "patient"; // Default to patient if document doesn't exist
                        loadFragment(new PatientHomeFragment());
                    }
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    // hideLoadingIndicator();

                    Log.e(TAG, "Error getting user role", e);
                    userRole = "patient"; // Default to patient on error
                    loadFragment(new PatientHomeFragment());
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadHomeBasedOnRole() {
        Log.d(TAG, "Loading home based on role: " + userRole);

        if ("doctor".equals(userRole)) {
            Log.d(TAG, "Loading doctor home fragment");
            loadFragment(new DoctorFragment());
        } else {
            Log.d(TAG, "Loading patient home fragment");
            loadFragment(new PatientHomeFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        Log.d(TAG, "Loading fragment: " + fragment.getClass().getSimpleName());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
