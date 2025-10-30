package com.tec.medxpert.ui.appointment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.Medication;
import com.tec.medxpert.data.model.profile.Doctor;
import com.tec.medxpert.data.repository.profile.DoctorRepository;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private Context context;
    private OnAppointmentClickListener listener;

    private boolean showStatusLabel = false;

    private DoctorRepository doctorRepository = new DoctorRepository(FirebaseFirestore.getInstance());

    public void updateList(List<Appointment> filteredList) {
        appointments = filteredList;
        notifyDataSetChanged();
    }

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment, int position);
    }

    public AppointmentAdapter(Context context, List<Appointment> appointments, OnAppointmentClickListener listener) {
        this.context = context;
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.statusValue.setVisibility(View.VISIBLE);
        holder.statusValue.setText(appointment.getStatus());
        TextView statusLabel = holder.itemView.findViewById(R.id.statusValue);
        if (showStatusLabel) {
            statusLabel.setVisibility(View.VISIBLE);
            statusLabel.setText(appointment.getStatus());
        } else {
            statusLabel.setVisibility(View.GONE);
        }

        holder.specialtyTextView.setText(appointment.getSpecialty());

        doctorRepository.getDoctor(appointment.getDoctorId())
                .addOnSuccessListener(documentSnapshot -> {
                    Doctor doctor = documentSnapshot.toObject(Doctor.class);
                    if (doctor != null) {
                        holder.doctorTextView.setText(doctor.getName());
                    } else {
                        holder.doctorTextView.setText(appointment.getDoctorId());
                    }
                })
                .addOnFailureListener(e -> holder.doctorTextView.setText("Error loading doctor"));

        String dateString = appointment.getDate();
        String timeString = appointment.getTime();

        try {
            SimpleDateFormat sdfDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date d = sdfDate.parse(dateString);
            Date t = sdfTime.parse(timeString);


            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Calendar calTime = Calendar.getInstance();
            calTime.setTime(t);
            cal.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
            Date appointmentDate = cal.getTime();


            SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d  h:mm a", Locale.getDefault());
            holder.dateTimeTextView.setText(displayFormat.format(appointmentDate));
        } catch (ParseException e) {

            holder.dateTimeTextView.setText(dateString + " " + timeString);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppointmentClick(appointment, position);
            }
        });
    }

    public void setShowStatusLabel(boolean show) {
        this.showStatusLabel = show;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    private static class AppointmentDiffCallback extends DiffUtil.Callback {
        private final List<Appointment> oldList;
        private final List<Appointment> newList;

        private AppointmentDiffCallback(List<Appointment> oldList, List<Appointment> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView specialtyTextView;
        TextView doctorTextView;
        TextView dateTimeTextView;
        ImageView detailsArrow;

        TextView statusValue;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            specialtyTextView = itemView.findViewById(R.id.specialtyTextView);
            doctorTextView = itemView.findViewById(R.id.doctorTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            detailsArrow = itemView.findViewById(R.id.detailsArrow);
            statusValue = itemView.findViewById(R.id.statusValue);
        }
    }
}
