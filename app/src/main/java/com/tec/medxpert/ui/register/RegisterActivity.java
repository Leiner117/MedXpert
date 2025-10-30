package com.tec.medxpert.ui.register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.tec.medxpert.navigation.authentication.AuthenticationNavigator;
import com.tec.medxpert.navigation.terms.TermsNavigator;
import com.tec.medxpert.navigation.codeSecurity.VerificationCodeNavigator;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    @Inject
    AuthenticationNavigator navigator;

    @Inject
    TermsNavigator termsNavigator;

    @Inject
    VerificationCodeNavigator verificationCodeNavigator;


    private RegisterViewModel viewModel;
    private CompositeDisposable disposables = new CompositeDisposable();
    private TextInputEditText editTextEmail, editTextPassword;
    private MaterialButton buttonSignUp;
    private CheckBox checkBoxAcceptTerms;
    private ImageButton buttonGoogle;

    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        disposables.add(viewModel.getRegistrationStatus()
            .subscribe(status -> {
                if (status != null && !status.isEmpty()) {
                    Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
                }
            })
        );

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignUp = findViewById(R.id.buttonSign_up);
        checkBoxAcceptTerms = findViewById(R.id.checkBoxAcceptTerms);
        buttonGoogle = findViewById(R.id.buttonGoogle);

        buttonGoogle.setOnClickListener(v -> {
            if (!checkBoxAcceptTerms.isChecked()) {
                Toast.makeText(this, R.string.terms_conditions, Toast.LENGTH_SHORT).show();
                return;
            }

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
            );

            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });

        });

        buttonSignUp.setOnClickListener(v -> {
            String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
            boolean termsAccepted = checkBoxAcceptTerms.isChecked();

            handleSignUp(email, password, termsAccepted);
        });

        checkBoxAcceptTerms.setOnClickListener(v -> {
            termsNavigator.navigateToTerms(1001);
        });
    }

    private void checkEmailRegistration(String email, Runnable onNotRegistered) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                String storedPassword = querySnapshot.getDocuments().get(0).getString("password");

                if (storedPassword == null || storedPassword.isEmpty()) {
                    // Account registered with Google
                    Toast.makeText(this, getString(R.string.account_registered_with_google), Toast.LENGTH_SHORT).show();
                } else {
                    // Account registered with email
                    Toast.makeText(this, getString(R.string.account_registered_with_email), Toast.LENGTH_SHORT).show();
                }
            } else {
                // Account not registered
                onNotRegistered.run();
            }
        })
        .addOnFailureListener(e -> {
            Log.e("CheckEmail", getString(R.string.error_fetching_user), e);
            Toast.makeText(this, getString(R.string.error_fetching_user), Toast.LENGTH_SHORT).show();
        });
    }

    private void handleSignUp(String email, String password, boolean termsAccepted) {
        if (!termsAccepted) {
            Toast.makeText(this, getString(R.string.terms_conditions), Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            editTextEmail.setError(getString(R.string.incorrect_email_format));
            editTextEmail.requestFocus();
            return;
        }

        if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            editTextPassword.setError(getString(R.string.invalid_password));
            editTextPassword.requestFocus();
            return;
        }

        checkEmailRegistration(email, () -> {
            // Save temporary email and password
            SharedPreferences prefs = getSharedPreferences("MedXpertPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("TempEmail", email)
                    .putString("TempPassword", password)
                    .apply();

            viewModel.signUpWithEmail(email, password, verificationCodeNavigator);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    checkEmailRegistration(account.getEmail(), () -> {
                        viewModel.handleSignInWithGoogle(account, navigator);
                    });

                }
            } catch (ApiException e) {
                Toast.makeText(this, R.string.google_signup_error, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                checkBoxAcceptTerms.setChecked(true);
            } else if (resultCode == RESULT_CANCELED) {
                checkBoxAcceptTerms.setChecked(false);
                Toast.makeText(this, getString(R.string.must_accept_terms), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
