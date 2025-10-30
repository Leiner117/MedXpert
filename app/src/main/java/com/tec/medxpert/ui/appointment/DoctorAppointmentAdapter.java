package com.tec.medxpert.ui.appointment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.profile.Patient;
import com.tec.medxpert.data.model.profile.PersonalData;
import com.tec.medxpert.data.repository.profile.PatientRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorAppointmentAdapter
        extends RecyclerView.Adapter<DoctorAppointmentAdapter.AppointmentViewHolder> {

    private final Context context;
    private final PatientRepository patientRepository;
    private final List<Appointment> appointmentList = new ArrayList<>();

    private final List<Appointment> allAppointments = new ArrayList<>();

    private boolean showStatusLabel = false;

    // Formats to parse and display dates and times
    private final SimpleDateFormat inputDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat outputDateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat inputTimeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat outputTimeFormat =
            new SimpleDateFormat("h:mm a", Locale.getDefault());

    public DoctorAppointmentAdapter(Context context, PatientRepository patientRepository) {
        this.context = context;
        this.patientRepository = patientRepository;
    }

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    private OnAppointmentClickListener listener;

    public void setOnAppointmentClickListener(OnAppointmentClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_doctor_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    public void setShowStatusLabel(boolean show) {
        this.showStatusLabel = show;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {

        Appointment appointment = appointmentList.get(position);

        TextView statusLabel = holder.itemView.findViewById(R.id.statusValue);
        if (showStatusLabel) {
            statusLabel.setVisibility(View.VISIBLE);
            statusLabel.setText(appointment.getStatus());
        } else {
            statusLabel.setVisibility(View.GONE);
        }

        holder.specialtyTextView.setText(appointment.getSpecialty());

        String rawDate = appointment.getDate();
        String rawTime = appointment.getTime();
        String dateTime;
        try {
            Date dateParsed = inputDateFormat.parse(rawDate);
            Date timeParsed = inputTimeFormat.parse(rawTime);
            String dateFormatted = outputDateFormat.format(dateParsed);
            String timeFormatted = outputTimeFormat.format(timeParsed);
            dateTime = dateFormatted + "  " + timeFormatted;
        } catch (ParseException e) {
            dateTime = rawDate + "  " + rawTime;
        }
        holder.dateTimeTextView.setText(dateTime);

        String patientId = appointment.getPatientId();
        patientRepository.getPatientById(patientId)
                .addOnSuccessListener(documentSnapshot -> {
                    Patient patient = documentSnapshot.toObject(Patient.class);
                    if (patient != null) {
                        PersonalData pd = patient.getPersonalData();
                        if (pd != null) {
                            holder.patientNameTextView.setText(pd.getName());
                            holder.patientIdTextView.setText(pd.getIdNumber());
                        } else {
                            holder.patientNameTextView.setText("–");
                            holder.patientIdTextView.setText("–");
                        }
                    } else {
                        holder.patientNameTextView.setText("–");
                        holder.patientIdTextView.setText("–");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.patientNameTextView.setText("Error: Can't load patient");
                    holder.patientIdTextView.setText("Could not load ID");
                });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppointmentClick(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    // Update the list of appointments and notify the adapter
    public void updateList(List<Appointment> newAppointments) {
        allAppointments.clear();
        allAppointments.addAll(newAppointments);
        appointmentList.clear();
        appointmentList.addAll(newAppointments);
        notifyDataSetChanged();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        final TextView patientNameTextView;
        final TextView patientIdTextView;
        final TextView specialtyTextView;
        final TextView dateTimeTextView;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            patientNameTextView =
                    itemView.findViewById(R.id.patientNameTextView);
            patientIdTextView =
                    itemView.findViewById(R.id.patientIdTextView);
            specialtyTextView =
                    itemView.findViewById(R.id.specialtyTextView);
            dateTimeTextView =
                    itemView.findViewById(R.id.dateTimeTextView);
        }
    }

}
