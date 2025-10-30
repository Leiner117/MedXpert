package com.tec.medxpert.ui.chat_profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tec.medxpert.R;

public class DoctorChatProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private ImageView doctorPhoto;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_chat_profile);

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");

        doctorPhoto = findViewById(R.id.doctorPhoto);
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        loadUserProfilePicture("7oqLYPG2ZxTXYxzdgqHVEqtGaSm1");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            doctorPhoto.setImageURI(imageUri);

        }
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
}