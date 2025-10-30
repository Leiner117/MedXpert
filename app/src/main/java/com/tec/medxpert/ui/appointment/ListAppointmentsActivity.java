package com.tec.medxpert.ui.appointment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.profile.Doctor;
import com.tec.medxpert.data.repository.profile.DoctorRepository;
import com.tec.medxpert.navigation.appointment.AppAppointmentCoordinator;
import com.tec.medxpert.navigation.appointment.AppointmentCoordinator;
import com.tec.medxpert.ui.viewMedication.ViewMedicationAdapter;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ListAppointmentsActivity extends AppCompatActivity implements AppointmentAdapter.OnAppointmentClickListener {

    private ProgressBar progressBar;
    private RecyclerView appointmentsRecyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList = new ArrayList<>();

    private EditText searchEditText;
    private ImageView filterIcon;
    private CardView filterDialog;

    private ImageView backButton;

    private TextView dateFilter, specialtyFilter;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddAppointment;

    private Disposable updateMissedDisposable;
    private ListAppointmentsViewModel viewModel;
    private Disposable appointmentsDisposable;
    private AppointmentCoordinator appointmentCoordinator;

    private TextView emptyView;
    private DoctorRepository doctorRepository = new DoctorRepository(FirebaseFirestore.getInstance());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        // Coordinator
        appointmentCoordinator = new AppAppointmentCoordinator(this);

        // Initialization of UI components
        progressBar = findViewById(R.id.progress_bar);
        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        filterIcon = findViewById(R.id.filterIcon);
        filterDialog = findViewById(R.id.filterDialog);
        dateFilter = findViewById(R.id.dateFilter);
        specialtyFilter = findViewById(R.id.specialtyFilter);
        tabLayout = findViewById(R.id.tabLayout);
        fabAddAppointment = findViewById(R.id.fabAddAppointment);
        backButton = findViewById(R.id.btn_back);
        emptyView = findViewById(R.id.emptyView);

        // ViewModel initialization
        viewModel = new ViewModelProvider(this).get(ListAppointmentsViewModel.class);

        // Event to navigate to back
        backButton.setOnClickListener(v -> finish());

        // RecyclerView
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter
        appointmentAdapter = new AppointmentAdapter(this, appointmentList, this);
        appointmentsRecyclerView.setAdapter(appointmentAdapter);

        searchEditText           = findViewById(R.id.searchEditText);
        filterIcon               = findViewById(R.id.filterIcon);
        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView);

        // Load history appointments

        // Setup search functionality
        setupSearch();

        // Observe ViewModel for changes
        observeViewModel();

        // Setup tabs
        setupTabs();

        // Event to navigate to book appointment
        fabAddAppointment.setOnClickListener(v ->
                appointmentCoordinator.navigateToBookAppointment()
        );

        viewModel.getFilteredAppointments().observe(this, filteredList -> {
            appointmentAdapter.updateList(filteredList);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Reload appointments when the activity resumes
        updateMissedDisposable = viewModel.getRepository().updateMissedAppointments()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (tabLayout.getSelectedTabPosition() == 0) {
                        loadUpcomingAppointments();
                    } else {
                        loadHistoryAppointments();
                    }
                }, error -> {
                    if (tabLayout.getSelectedTabPosition() == 0) {
                        loadUpcomingAppointments();
                    } else {
                        loadHistoryAppointments();
                    }
                });
    }
    private void loadUpcomingAppointments() {
        appointmentAdapter.setShowStatusLabel(false);
        appointmentList.clear();
        appointmentAdapter.notifyDataSetChanged();
        appointmentsRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (appointmentsDisposable != null && !appointmentsDisposable.isDisposed()) {
            appointmentsDisposable.dispose();
        }
        appointmentsDisposable = viewModel.getUpcomingAppointments()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(appointments -> {
                progressBar.setVisibility(View.GONE);
                appointmentList.clear();
                appointmentList.addAll(appointments);
                appointmentAdapter.notifyDataSetChanged();
                appointmentsRecyclerView.setVisibility(appointments.isEmpty() ? View.GONE : View.VISIBLE);
                emptyView.setVisibility(appointments.isEmpty() ? View.VISIBLE : View.GONE);
                viewModel.getAppointmentsLiveData().setValue(appointments);
            }, error -> {
                progressBar.setVisibility(View.GONE);
                appointmentsRecyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                Toast.makeText(this, "Error loading appointments", Toast.LENGTH_LONG).show();
            });
    }

    // Method to load history appointments
    private void loadHistoryAppointments() {
        appointmentAdapter.setShowStatusLabel(true);
        appointmentList.clear();
        appointmentAdapter.notifyDataSetChanged();
        appointmentsRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (appointmentsDisposable != null && !appointmentsDisposable.isDisposed()) {
            appointmentsDisposable.dispose();
        }
        appointmentsDisposable = viewModel.getHistoryAppointments()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appointments -> {
                    progressBar.setVisibility(View.GONE);
                    appointmentList.clear();
                    appointmentList.addAll(appointments);
                    appointmentAdapter.notifyDataSetChanged();
                    appointmentsRecyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(appointments.isEmpty() ? View.VISIBLE : View.GONE);
                    viewModel.getAppointmentsLiveData().setValue(appointments);
                }, error -> {
                    progressBar.setVisibility(View.GONE);
                    appointmentsRecyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading appointments", Toast.LENGTH_LONG).show();
                });
    }

    // Method to search appointments by date or specialty
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        filterIcon.setOnClickListener(v -> {
            ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
            PopupMenu popupMenu = new PopupMenu(ctw, filterIcon);

            popupMenu.getMenuInflater().inflate(R.menu.filter_menu_appointment, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.filter_by_specialty) {
                    viewModel.setSelectedFilter("specialty");
                } else if (id == R.id.filter_by_date) {
                    viewModel.setSelectedFilter("date");
                }
                return true;
            });
            popupMenu.show();
        });
    }

    private void observeViewModel() {
        appointmentAdapter = new AppointmentAdapter(this, appointmentList, this);
        appointmentsRecyclerView.setAdapter(appointmentAdapter);

        viewModel.getFilteredAppointments().observe(this, appointments -> {
            if (appointments != null) {
                appointmentAdapter.updateList(appointments);
            }
        });

        viewModel.getSelectedFilter().observe(this, filter -> {
            switch (filter) {
                case "date":
                    searchEditText.setHint(R.string.ap_lbl_search_by_date);
                    break;
                case "specialty":
                    searchEditText.setHint(R.string.ap_lbl_search_by_specialty);
                    break;
                default:
                    searchEditText.setHint(R.string.ap_lbl_search_by_name);
            }
        });
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    appointmentAdapter.setShowStatusLabel(false);
                    loadUpcomingAppointments();
                } else {
                    appointmentAdapter.setShowStatusLabel(true);
                    loadHistoryAppointments();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    @Override
    public void onAppointmentClick(Appointment appointment, int position) {
        showAppointmentDetailDialog(appointment);
    }

    private void showAppointmentDetailDialog(Appointment appointment) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_appointment_detail);
        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView titleView    = dialog.findViewById(R.id.appointmentTitleTextView);
        TextView statusView   = dialog.findViewById(R.id.detailStatusTextView);
        TextView dateView     = dialog.findViewById(R.id.detailDateTextView);
        TextView timeView     = dialog.findViewById(R.id.detailTimeTextView);
        TextView doctorView   = dialog.findViewById(R.id.detailDoctorTextView);
        TextView commentsView = dialog.findViewById(R.id.detailCommentsTextView);
        ImageView closeBtn    = dialog.findViewById(R.id.closeDetailDialog);
        Button cancelBtn      = dialog.findViewById(R.id.cancelAppointmentButton);
        Button viewDiagnosisBtn = dialog.findViewById(R.id.viewDiagnosisButton);

        // Don't show cancel button if the appointment is not pending
        if (!"Pending".equals(appointment.getStatus())) {
            cancelBtn.setVisibility(View.GONE);
        } else {
            cancelBtn.setVisibility(View.VISIBLE);
        }

        // Show view diagnosis button only if the appointment is completed
        if ("Completed".equals(appointment.getStatus())) {
            viewDiagnosisBtn.setVisibility(View.VISIBLE);
            viewDiagnosisBtn.setOnClickListener(v -> {
                // TODO: Here implement the logic to view the diagnosis
                Toast.makeText(this, "View Diagnosis clicked", Toast.LENGTH_SHORT).show();
            });
        } else {
            viewDiagnosisBtn.setVisibility(View.GONE);
        }

        doctorRepository.getDoctor(appointment.getDoctorId())
                .addOnSuccessListener(documentSnapshot -> {
                    Doctor doctor = documentSnapshot.toObject(Doctor.class);
                    String doctorName = (doctor != null) ? doctor.getName() : appointment.getDoctorId();
                    doctorView.setText(doctorName);
                    titleView.setText(appointment.getSpecialty() + " with " + doctorName);
                })
                .addOnFailureListener(e -> {
                    doctorView.setText(appointment.getDoctorId());
                    titleView.setText(appointment.getSpecialty() + " with " + appointment.getDoctorId());
                });

        try {
            SimpleDateFormat sdfDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date d = sdfDate.parse(appointment.getDate());
            Date t = sdfTime.parse(appointment.getTime());

            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Calendar calTime = Calendar.getInstance();
            calTime.setTime(t);
            cal.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE,     calTime.get(Calendar.MINUTE));
            Date appointmentDate = cal.getTime();

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            dateView.setText(dateFormat.format(appointmentDate));
            timeView.setText(timeFormat.format(appointmentDate));
        } catch (ParseException e) {

            dateView.setText(appointment.getDate());
            timeView.setText(appointment.getTime());
            e.printStackTrace();
        }

        statusView.setText(appointment.getStatus());
        commentsView.setText(appointment.getComments());

        // Set click listeners
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        cancelBtn.setOnClickListener(v -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy h:mm a", Locale.getDefault());
                Date citaDate = sdf.parse(appointment.getDate() + " " + appointment.getTime());
                Date ahora = new Date();

                assert citaDate != null;
                long diffMillis = citaDate.getTime() - ahora.getTime();
                long diffHoras = diffMillis / (1000 * 60 * 60);

                if (diffHoras >= 12) { // Allow cancellation if more than 12 hours before the appointment
                    dialog.dismiss();
                    showCancelConfirmationDialog(appointment);
                } else {
                    Toast.makeText(this, "Cannot cancel appointment within 12 hours of the scheduled time.", Toast.LENGTH_SHORT).show();
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Error parsing appointment date/time", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
    }

    private void showCancelConfirmationDialog(Appointment appointment) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cancel_confirmation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize dialog views
        ImageView closeCancelDialog = dialog.findViewById(R.id.closeCancelDialog);
        Button yesButton = dialog.findViewById(R.id.yesButton);
        Button noButton = dialog.findViewById(R.id.noButton);

        // Set click listeners
        closeCancelDialog.setOnClickListener(v -> dialog.dismiss());

        yesButton.setOnClickListener(v -> {
            viewModel.cancelAppointment(appointment.getId());
            Toast.makeText(this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadUpcomingAppointments(); // Refresh the list after cancellation
        });

        noButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (appointmentsDisposable != null && !appointmentsDisposable.isDisposed()) {
            appointmentsDisposable.dispose();
        }

        if (updateMissedDisposable != null && !updateMissedDisposable.isDisposed()) {
            updateMissedDisposable.dispose();
        }
        super.onDestroy();
    }
}
