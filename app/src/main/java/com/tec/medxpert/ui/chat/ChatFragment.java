package com.tec.medxpert.ui.chat;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tec.medxpert.R;
import com.tec.medxpert.ui.chat.model.Message;
import com.tec.medxpert.ui.chat.adapter.MessageAdapter;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatFragment extends Fragment {

    private ChatViewModel viewModel;
    private RecyclerView rvMessages;
    private MessageAdapter adapter;
    private EditText etMessage;
    private ImageView btnSend;

    private DatabaseReference chatRef;
    private List<Message> messages = new ArrayList<>();

    private Toolbar toolbar;
    private ImageButton btnBack;
    private ImageView ivDoctorPhoto;
    private TextView tvDoctorName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Referencias UI
        toolbar = view.findViewById(R.id.toolbar);
        btnBack = view.findViewById(R.id.btn_back);
        ivDoctorPhoto = view.findViewById(R.id.ivDoctorPhoto);
        tvDoctorName = view.findViewById(R.id.tvDoctorName);

        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        // Toolbar
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        adapter = new MessageAdapter(new ArrayList<>());
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(adapter);

        Bundle args = getArguments();
        String doctorUid = args != null && args.getString("doctorUid") != null
                ? args.getString("doctorUid")
                : "7oqLYPG2ZxTXYxzdgqHVEqtGaSm1";
        String patientUid = args != null && args.getString("patientUid") != null
                ? args.getString("patientUid")
                : "";
        String doctorName = args != null ? args.getString("doctorName", "Doctor") : "Doctor";
        String patientName = args != null ? args.getString("patientName", "Paciente") : "Paciente";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String currentUserId = user != null ? user.getUid() : "anon";
        if (tvDoctorName != null) {
            if (currentUserId.equals(doctorUid)) {
                tvDoctorName.setText(patientName);

                //ivDoctorPhoto.setColorFilter(ContextCompat.getColor(requireContext(), R.color.ic_logo_background), android.graphics.PorterDuff.Mode.SRC_IN);
                //ivDoctorPhoto.setImageResource(R.drawable.ic_profile);
            } else {
                btnBack.setVisibility(View.GONE);
                tvDoctorName.setText(doctorName);

            }
        }

        tvDoctorName.setOnClickListener(v -> {
            if (currentUserId.equals(doctorUid)) {
                viewModel.onPatientNameClicked(patientUid, patientName);
            } else {
                viewModel.onDoctorNameClicked(doctorUid, doctorName);
            }
        });

        // Load user profile picture
        String doctorid = args != null ? args.getString("doctorUid") : null;
        String patientid = args != null ? args.getString("patientUid") : null;

        FirebaseUser userprofile = FirebaseAuth.getInstance().getCurrentUser();
        final String  currentID= userprofile != null ? userprofile.getUid() : "anon";

        if (currentID.equals(doctorid)) {
            loadUserProfilePicture(patientid);
        } else {
            loadUserProfilePicture(doctorid);
        }

        // Ruta del chat paciente-doctor
        chatRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(doctorUid)
                .child(patientUid);

        // Leer mensajes en tiempo real
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    if (msg != null) {
                        Message.Type type = msg.getSenderId().equals(currentUserId)
                                ? Message.Type.SENT
                                : Message.Type.RECEIVED;
                        messages.add(new Message(msg.getText(), msg.getSenderId(), msg.getTimestamp(), type));
                    }
                }
                adapter = new MessageAdapter(messages);
                rvMessages.setAdapter(adapter);
                if (adapter.getItemCount() > 0) {
                    rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Enviar mensaje
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String senderId = currentUser != null ? currentUser.getUid() : "anon";
                Message msg = new Message(text, senderId, System.currentTimeMillis());
                chatRef.push().setValue(msg);
                etMessage.setText("");
            }
        });

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
            Log.e("ChatFragment", getString(R.string.no_image_found_for_userid) + userId);
            Toast.makeText(getContext(), R.string.could_not_load_profile_picture, Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = userId + extensions[index];
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference("ProfileImages")
                .child(filename);

        imageRef.getDownloadUrl()
        .addOnSuccessListener(uri -> {
            if (!isAdded()) return;
        Glide.with(this)
        .load(uri)
        .error(R.drawable.ic_profile)
        .placeholder(R.drawable.ic_profile)

        .into(ivDoctorPhoto);

        })
        .addOnFailureListener(e -> {
            tryLoadImageWithExtensions(userId, extensions, index + 1);
        });
    }
}
