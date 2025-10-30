package com.tec.medxpert.ui.codeSecurity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.tec.medxpert.R;
import com.tec.medxpert.navigation.codeSecurity.VerificationCodeNavigator;
import com.tec.medxpert.ui.register.RegisterViewModel;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VerificationCodeActivity extends AppCompatActivity {

    @Inject
    VerificationCodeNavigator verificationCodeNavigator;

    private VerificationCodeViewModel viewModel;
    private RegisterViewModel registerViewModel;

    private EditText etCode1, etCode2, etCode3, etCode4, etCode5, etCode6;
    private Button btnVerify;
    private TextView tvError, tvResendCode;
    private ImageButton btnBack;

    private static final String PREFS_NAME = "MedXpertPrefs";
    private static final String KEY_SECURITY_CODE = "SecurityCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        viewModel = new ViewModelProvider(this).get(VerificationCodeViewModel.class);

        etCode1 = findViewById(R.id.etCode1);
        etCode2 = findViewById(R.id.etCode2);
        etCode3 = findViewById(R.id.etCode3);
        etCode4 = findViewById(R.id.etCode4);
        etCode5 = findViewById(R.id.etCode5);
        etCode6 = findViewById(R.id.etCode6);
        btnVerify = findViewById(R.id.btnVerify);
        tvError = findViewById(R.id.tvError);
        tvResendCode = findViewById(R.id.tvResendCode);
        btnBack = findViewById(R.id.btn_back);

        setupEditTexts();

        btnVerify.setOnClickListener(v -> verifyCode());

        btnBack.setOnClickListener(v -> finish());

        tvResendCode.setOnClickListener(v -> {
            registerViewModel.resendSecurityCode();
            Toast.makeText(this, getString(R.string.code_resent_successfully), Toast.LENGTH_SHORT).show();
        });

    }

    private void setupEditTexts() {
        // Move focus to the next EditText when one character is entered
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    View nextView = Objects.requireNonNull(getCurrentFocus()).focusSearch(View.FOCUS_RIGHT);
                    if (nextView != null) nextView.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        etCode1.addTextChangedListener(textWatcher);
        etCode2.addTextChangedListener(textWatcher);
        etCode3.addTextChangedListener(textWatcher);
        etCode4.addTextChangedListener(textWatcher);
        etCode5.addTextChangedListener(textWatcher);
        etCode6.addTextChangedListener(textWatcher);
    }

    private void verifyCode() {
        String enteredCode = etCode1.getText().toString() +
                etCode2.getText().toString() +
                etCode3.getText().toString() +
                etCode4.getText().toString() +
                etCode5.getText().toString() +
                etCode6.getText().toString();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String correctCode = prefs.getString(KEY_SECURITY_CODE, "");

        if (enteredCode.equals(correctCode)) {
            tvError.setVisibility(View.INVISIBLE);
            Toast.makeText(this, getString(R.string.code_verified_successfully), Toast.LENGTH_SHORT).show();

            registerViewModel.setCodeVerified(true);

            String email = prefs.getString("TempEmail", "");
            String password = prefs.getString("TempPassword", "");

            registerViewModel.signUpWithEmail(email, password, verificationCodeNavigator);

        } else {
            tvError.setVisibility(View.VISIBLE);
        }
    }
}

