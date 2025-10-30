package com.tec.medxpert.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.patientChats.PatientChat;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private List<PatientChat> patients;
    private OnChatClickListener listener;

    // Define la interfaz para el click
    public interface OnChatClickListener {
        void onChatClick(PatientChat patient);
    }

    public ChatListAdapter(List<PatientChat> patients) {
        this.patients = patients;
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void updatePatients(List<PatientChat> newPatients) {
        this.patients = newPatients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        PatientChat patient = patients.get(position);
        holder.nameTextView.setText(patient.getName());
        holder.userIDTextView.setText(patient.getIdNumber());

        // Asocia el click al itemView
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(patient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView userIDTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            userIDTextView = itemView.findViewById(R.id.userIDTextView);
        }
    }
}
