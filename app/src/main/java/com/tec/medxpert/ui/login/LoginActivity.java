package com.tec.medxpert.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;


import com.tec.medxpert.navigation.authentication.AuthenticationNavigator;

import java.util.Objects;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    @Inject
    AuthenticationNavigator navigator;
    private TextInputEditText editTextEmail, editTextPassword;

    private CheckBox checkBoxRememberMe;
    private MaterialButton buttonLogin;
    private ImageButton buttonGoogle;
    private TextView textViewForgotPassword;
    private LoginViewModel viewModel;

    private TextView textViewDontHaveAccount;


    private static final int RC_SIGN_IN = 9001;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private SharedPreferences preferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initUI();

        disposables.add(
            viewModel.getLoginStatusSubject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(status -> {
                if ("account_registered_with_email".equals(status)) {
                    Toast.makeText(this, getString(R.string.account_registered_with_email), Toast.LENGTH_SHORT).show();
                } else if (status != null && !status.isEmpty()) {
                    int resId = getResources().getIdentifier(status, "string", getPackageName());
                    if (resId == 0) {
                        resId = R.string.error_account_deleted;
                    }
                    Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
                }
            })
        );

        // Load "Remember Me" state
        preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        boolean rememberMe = preferences.getBoolean("rememberMe", false);
        checkBoxRememberMe.setChecked(rememberMe);

        // Load logo from Firebase Storage
        ImageView imageDoctor = findViewById(R.id.imageDoctor);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("logo.png");
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (!isDestroyed() && !isFinishing()) {
                Glide.with(LoginActivity.this)
                        .load(uri)
                        .into(imageDoctor);
            }
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", getString(R.string.firebase_storage_error), e);
        });

        buttonLogin.setOnClickListener(v -> loginUser());

        buttonGoogle.setOnClickListener(v -> {
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

        textViewForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, R.string.functionality_in_development, Toast.LENGTH_SHORT).show()
        );

        textViewDontHaveAccount.setOnClickListener(v -> {
            navigator.navigateToRegister();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    viewModel.loginWithGoogle(account, navigator, checkBoxRememberMe.isChecked());

                    // Save "Remember Me" state
                    saveLoginPreferences(checkBoxRememberMe.isChecked());
                }
            } catch (ApiException e) {
                Log.e("GoogleSignIn", getString(R.string.google_signin_error), e);
                Toast.makeText(this, R.string.google_sign_in_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void initUI() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGoogle = findViewById(R.id.buttonGoogle);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);
        textViewDontHaveAccount = findViewById(R.id.textViewDontHaveAccount);

    }


    private void loginUser() {
        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();

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

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String storedHash = queryDocumentSnapshots.getDocuments().get(0).getString("password");


                        if (storedHash == null || storedHash.isEmpty()) {
                            Toast.makeText(this, getString(R.string.account_registered_with_google), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (BCrypt.checkpw(password, storedHash)) {
                            // Password is correct and user is found
                            viewModel.loginWithEmail(email, password, navigator, checkBoxRememberMe.isChecked());
                            saveLoginPreferences(checkBoxRememberMe.isChecked());
                        } else {
                            Toast.makeText(this, getString(R.string.different_password), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.account_not_registered), Toast.LENGTH_SHORT).show();
                        viewModel.loginWithEmail(email, password, navigator, checkBoxRememberMe.isChecked());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginActivity", getString(R.string.error_fetching_user), e);
                    Toast.makeText(this, getString(R.string.error_fetching_user), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveLoginPreferences(boolean rememberMe) {
        preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("rememberMe", rememberMe);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

}
