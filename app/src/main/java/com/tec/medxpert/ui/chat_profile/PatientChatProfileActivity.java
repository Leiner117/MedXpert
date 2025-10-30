package com.tec.medxpert.ui.chat_profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.patientChats.ProfileChat;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PatientChatProfileActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private PatientChatProfileViewModel viewModel;
    private TextView doctorName, telephone, email, bloodType, weight, height;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageReference;
    private ImageView doctorPhoto;

    private LinearLayout allergiesSection, allergiesList, personalMedicalHistorySection, personalMedicalHistoryList, familyMedicalHistorySection, familyMedicalHistoryList;
    private ImageView allergiesArrow, personalMedicalHistoryArrow, familyMedicalHistoryArrow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_chat_profile);

        firestore = FirebaseFirestore.getInstance();

        ImageButton backButton = findViewById(R.id.backButton);

        doctorName = findViewById(R.id.doctorName);
        telephone = findViewById(R.id.telephone);
        email = findViewById(R.id.email);
        bloodType = findViewById(R.id.bloodType);
        weight = findViewById(R.id.weight);
        height = findViewById(R.id.height);
        doctorPhoto = findViewById(R.id.doctorPhoto);

        allergiesSection = findViewById(R.id.allergiesSection);
        allergiesList = findViewById(R.id.allergiesList);
        allergiesArrow = findViewById(R.id.allergiesArrow);

        personalMedicalHistorySection = findViewById(R.id.personalMedicalHistorySection);
        personalMedicalHistoryList = findViewById(R.id.personalMedicalHistoryList);
        personalMedicalHistoryArrow = findViewById(R.id.personalMedicalHistoryArrow);

        familyMedicalHistorySection = findViewById(R.id.familyMedicalHistorySection);
        familyMedicalHistoryList = findViewById(R.id.familyMedicalHistoryList);
        familyMedicalHistoryArrow = findViewById(R.id.familyMedicalHistoryArrow);

        // Listeners configuration
        setupExpandableSection(allergiesSection, allergiesList, allergiesArrow);
        setupExpandableSection(personalMedicalHistorySection, personalMedicalHistoryList, personalMedicalHistoryArrow);
        setupExpandableSection(familyMedicalHistorySection, familyMedicalHistoryList, familyMedicalHistoryArrow);

        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");

        backButton.setOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(PatientChatProfileViewModel.class);

        String userId = getIntent().getStringExtra("userId");

        if (userId != null) {
            loadUserProfilePicture(userId);
            viewModel.getProfileChatByUserId(userId).observe(this, profileChat -> {
                if (profileChat != null) {
                    updateUI(profileChat);
                } else {
                    Toast.makeText(this, R.string.no_patient_data_found, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, R.string.userid_does_not_exist, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupExpandableSection(LinearLayout section, LinearLayout list, ImageView arrow) {
        section.setOnClickListener(v -> {
            if (list.getVisibility() == View.GONE) {
                list.setVisibility(View.VISIBLE);
                arrow.setImageResource(R.drawable.arrowdown);
            } else {
                list.setVisibility(View.GONE);
                arrow.setImageResource(R.drawable.arrowdown);
            }
        });
    }

    private void updateUI(ProfileChat profileChat) {
        doctorName.setText(profileChat.getName());
        telephone.setText(profileChat.getPhone());
        email.setText(profileChat.getEmail());
        bloodType.setText(getString(R.string.blood_type, profileChat.getBloodType()));
        weight.setText(getString(R.string.weight, profileChat.getWeight()));
        height.setText(getString(R.string.height, profileChat.getHeight()));

        // Load profile picture using Glide
        String profilePictureUrl = profileChat.getProfilePicture();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Glide.with(this)
            .load(profilePictureUrl)
            .into(doctorPhoto);
        }

        // Load lists
        populateList(allergiesList, profileChat.getAllergies());
        populateList(personalMedicalHistoryList, profileChat.getPersonalMedicalHistory());
        populateList(familyMedicalHistoryList, profileChat.getFamilyMedicalHistory());
    }

    private void loadUserProfilePicture(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e("UserChatProfile", getString(R.string.the_userid_is_null_or_empty));
            return;
        }

        String[] extensions = {".jpg", ".png", ".webp"};
        tryLoadImageWithExtensions(userId, extensions, 0);
    }

    private void tryLoadImageWithExtensions(String userId, String[] extensions, int index) {
        if (index >= extensions.length) {
            Toast.makeText(this,  R.string.no_image_found_for_userid, Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = userId + extensions[index];
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference("ProfileImages")
                .child(filename);

        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(this)
                            .load(uri)
                            .into(doctorPhoto);
                })
                .addOnFailureListener(e -> {
                    tryLoadImageWithExtensions(userId, extensions, index + 1);
                });
    }


    private void populateList(LinearLayout listContainer, List<String> items) {
        listContainer.removeAllViews();
        for (String item : items) {
            TextView textView = new TextView(this);
            textView.setText(item);
            textView.setTextSize(14);
            textView.setTextColor(getResources().getColor(R.color.black));
            listContainer.addView(textView);
        }
    }
}