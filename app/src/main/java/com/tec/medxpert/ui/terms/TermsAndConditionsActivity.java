package com.tec.medxpert.ui.terms;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;

public class TermsAndConditionsActivity extends AppCompatActivity {

    private TextView tvTermsContent;
    private Button btnAccept, btnDecline;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);

        tvTermsContent = findViewById(R.id.tvTermsContent);
        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);

        firestore = FirebaseFirestore.getInstance();

        fetchTermsAndConditions();

        btnAccept.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        btnDecline.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void fetchTermsAndConditions() {
        firestore.collection("terms_conditions")
                .document("terms_and_conditions")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String content = documentSnapshot.getString("content");
                        tvTermsContent.setText(content);
                    }
                })
                .addOnFailureListener(e -> {
                    tvTermsContent.setText(getString(R.string.error_loading_terms));
                });
    }
}
