package com.tec.medxpert.ui.appointment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.tec.medxpert.R;
import com.tec.medxpert.data.repository.profile.PatientRepository;
import com.tec.medxpert.navigation.appointment.AppAppointmentCoordinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.Disposable;

@AndroidEntryPoint
public class DoctorAppointmentsActivity extends AppCompatActivity {

    @Inject
    PatientRepository patientRepository;

    private DoctorAppointmentsViewModel viewModel;
    private DoctorAppointmentAdapter appointmentAdapter;
    private RecyclerView recyclerView;
    private Disposable appointmentsDisposable;
    private ProgressBar progressBar;
    private String currentFilter = "name";
    private String searchQuery;
    private EditText searchEditText;
    private ImageView filterIcon;
    private TextView emptyView;

    private List<Appointment> currentList = new ArrayList<>();
    private String currentTab = "upcoming";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointments);

        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.appointmentsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setHint(getString(R.string.doc_lbl_search_by_name));
        filterIcon = findViewById(R.id.filterIcon);
        emptyView = findViewById(R.id.emptyView);

        filterIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, filterIcon);
            popup.getMenuInflater().inflate(R.menu.filter_menu_doctor_appointments, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                    if (id == R.id.doc_filter_by_name) {
                        currentFilter = "name";
                        searchEditText.setHint(getString(R.string.doc_lbl_search_by_name));
                    } else if (id == R.id.doc_filter_by_id) {
                        currentFilter = "id";
                        searchEditText.setHint(getString(R.string.doc_lbl_search_by_id));
                    } else if (id == R.id.doc_filter_by_date) {
                        currentFilter = "date";
                        searchEditText.setHint(getString(R.string.doc_lbl_search_by_date));
                    } else if (id == R.id.doc_filter_by_specialty) {
                        currentFilter = "specialty";
                        searchEditText.setHint(getString(R.string.doc_lbl_search_by_specialty));
                }
                // Seac
                searchEditText.setText("");
                appointmentAdapter.updateList(currentList);
                return true;
            });
            popup.show();
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                filterAppointments();
            }
        });

        appointmentAdapter =
                new DoctorAppointmentAdapter(this, patientRepository);
        recyclerView.setAdapter(appointmentAdapter);

        appointmentAdapter.setOnAppointmentClickListener(appointment -> {
            showAppointmentDetailDialog(appointment);
        });

        viewModel = new ViewModelProvider(this)
                .get(DoctorAppointmentsViewModel.class);

        // Load appointments for the doctor
        loadUpcomingAppointments();

        // Handle tab selection
        setupTabs();

        ImageView btnBack = findViewById(R.id.btn_back);

        // Back button listener
        btnBack.setOnClickListener(v -> finish());
    }

    // Method to setup tabs and their listeners
    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadUpcomingAppointments();
                } else {
                    loadHistoryAppointments();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // Method to setup search and filter functionality
    private void loadUpcomingAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        if (appointmentsDisposable != null && !appointmentsDisposable.isDisposed()) {
            appointmentsDisposable.dispose();
        }
        appointmentsDisposable = viewModel.getUpcomingAppointments()
            .subscribe(
                appointmentList -> {
                    progressBar.setVisibility(View.GONE);
                    List<Appointment> sortedList = viewModel.sortAppointments(appointmentList, false);
                    appointmentAdapter.updateList(sortedList);
                    appointmentAdapter.setShowStatusLabel(false);
                    currentList = sortedList;
                    currentTab = "upcoming";
                    recyclerView.setVisibility(sortedList.isEmpty() ? View.GONE : View.VISIBLE);
                    emptyView.setVisibility(sortedList.isEmpty() ? View.VISIBLE : View.GONE);
                },
                throwable -> {
                    progressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading appointments: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            );
    }

    // Method to load history appointments
    private void loadHistoryAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        if (appointmentsDisposable != null && !appointmentsDisposable.isDisposed()) {
            appointmentsDisposable.dispose();
        }

        appointmentsDisposable = viewModel.getHistoryAppointments()
            .subscribe(
                appointmentList -> {
                    progressBar.setVisibility(View.GONE);
                    List<Appointment> sortedList = viewModel.sortAppointments(appointmentList, true);
                    appointmentAdapter.updateList(sortedList);
                    appointmentAdapter.setShowStatusLabel(true);
                    currentList = sortedList;
                    currentTab = "history";
                    recyclerView.setVisibility(sortedList.isEmpty() ? View.GONE : View.VISIBLE);
                    emptyView.setVisibility(sortedList.isEmpty() ? View.VISIBLE : View.GONE);
                },
                throwable -> {
                    progressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading appointments: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appointmentsDisposable != null && !appointmentsDisposable.isDisposed()) {
            appointmentsDisposable.dispose();
        }
    }

    // Method to show appointment detail dialog
    private void showAppointmentDetailDialog(Appointment appointment) {
        // Check if the appointment is completed or cancelled to determine the layout
        boolean isHistory = "Completed".equalsIgnoreCase(appointment.getStatus()) || "Cancelled".equalsIgnoreCase(appointment.getStatus()) || "Missed".equalsIgnoreCase(appointment.getStatus());
        int layoutId = isHistory ? R.layout.dialog_doctor_history_appointment_detail : R.layout.dialog_doctor_appointment_detail;

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutId);

        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        dialog.getWindow().setLayout(width, android.view.WindowManager.LayoutParams.WRAP_CONTENT);


        TextView patientName = dialog.findViewById(R.id.detailPatientTextView);
        TextView patientId = dialog.findViewById(R.id.detailIdNumberTextView);
        TextView specialty = dialog.findViewById(R.id.detailSpecialtyTextView);
        TextView date = dialog.findViewById(R.id.detailDateTextView);
        TextView time = dialog.findViewById(R.id.detailTimeTextView);
        TextView comments = dialog.findViewById(R.id.detailCommentsTextView);
        TextView statusView = dialog.findViewById(R.id.detailStatusTextView);
        View closeBtn = dialog.findViewById(R.id.closeDetailDialog);
        Button registerDiagnosisBtn = dialog.findViewById(R.id.registerDiagnosisButton);
        if (statusView != null) statusView.setText(appointment.getStatus());
        if (closeBtn != null) closeBtn.setOnClickListener(v -> dialog.dismiss());
        Button viewDiagnosisBtn = dialog.findViewById(R.id.viewDiagnosisButton);

        // Check if the appointment is completed to show the view diagnosis button
        if (viewDiagnosisBtn != null) {
            if ("Completed".equalsIgnoreCase(appointment.getStatus())) {
                viewDiagnosisBtn.setVisibility(View.VISIBLE);
                viewDiagnosisBtn.setEnabled(true);

                viewDiagnosisBtn.setOnClickListener(v -> {
                    AppAppointmentCoordinator coordinator = new AppAppointmentCoordinator(this);
                    coordinator.navigateToDiagnosticInformation(this, appointment.getId());
                });

            } else {
                viewDiagnosisBtn.setVisibility(View.GONE);
            }
        }

        // Register diagnosis button is only available for pending appointments
        if (registerDiagnosisBtn != null) {
            registerDiagnosisBtn.setOnClickListener(v -> {
                AppAppointmentCoordinator coordinator = new AppAppointmentCoordinator(this);
                coordinator.navigateToDiagnostic(this, 101,
                    appointment.getId(),
                    appointment.getPatientId(),
                    patientName != null ? patientName.getText().toString() : "Unknown");
                dialog.dismiss();
            });
        }

        // Fetch patient details and set them in the dialog
        patientRepository.getPatientById(appointment.getPatientId())
            .addOnSuccessListener(documentSnapshot -> {
                com.tec.medxpert.data.model.profile.Patient patient = documentSnapshot.toObject(com.tec.medxpert.data.model.profile.Patient.class);
                if (patient != null && patient.getPersonalData() != null) {
                    if (patientName != null) patientName.setText(patient.getPersonalData().getName());
                    if (patientId != null) patientId.setText(patient.getPersonalData().getIdNumber());
                } else {
                    if (patientName != null) patientName.setText("–");
                    if (patientId != null) patientId.setText("–");
                }
            });

        if (specialty != null) specialty.setText(appointment.getSpecialty());
        if (date != null) date.setText(appointment.getDate());
        if (time != null) time.setText(appointment.getTime());
        if (comments != null) comments.setText(appointment.getComments());

        dialog.show();
    }

    // Handle the result from the diagnostic activity
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            loadUpcomingAppointments();
        }
    }
    private void filterAppointments() {
        List<Appointment> filtered = new ArrayList<>();

        if (currentFilter.equals("name") || currentFilter.equals("id")) {
            // Filter by name or ID requires fetching patient data
            patientRepository.getAllPatients().addOnSuccessListener(querySnapshot -> {
                Map<String, String> patientNames = new HashMap<>();
                Map<String, String> patientIdNumbers = new HashMap<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String id = doc.getId();
                    Map<String, Object> data = doc.getData();
                    if (data != null && data.containsKey("personalData")) {
                        Map<String, Object> personalData = (Map<String, Object>) data.get("personalData");
                        if (personalData != null) {
                            if (personalData.containsKey("name")) {
                                String name = (String) personalData.get("name");
                                if (name != null) patientNames.put(id, name.toLowerCase());
                            }
                            if (personalData.containsKey("idNumber")) {
                                String idNumber = (String) personalData.get("idNumber");
                                if (idNumber != null) patientIdNumbers.put(id, idNumber.toLowerCase());
                            }
                        }
                    }
                }
                for (Appointment ap : currentList) {
                    if (currentFilter.equals("name")) {
                        String name = patientNames.get(ap.getPatientId());
                        // Check if the name contains the search query
                        if (name != null && name.contains(searchQuery)) {
                            filtered.add(ap);
                        }
                    } else if (currentFilter.equals("id")) {
                        String idNumber = patientIdNumbers.get(ap.getPatientId());
                        // Check if the ID number contains the search query
                        if (idNumber != null && idNumber.contains(searchQuery)) {
                            filtered.add(ap);
                        }
                    }
                }
                appointmentAdapter.updateList(filtered);
            });
        } else {
            for (Appointment ap : currentList) {
                String field = "";
                switch (currentFilter) {
                    case "date":
                        field = ap.getDate();
                        break;
                    case "specialty":
                        field = ap.getSpecialty();
                        break;
                }
                if (field != null && field.toLowerCase().contains(searchQuery)) {
                    filtered.add(ap);
                }
            }
            appointmentAdapter.updateList(filtered);
        }
    }
}