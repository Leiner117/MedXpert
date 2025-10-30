package com.tec.medxpert.ui.diagnosticInformation;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Diagnostic;
import com.tec.medxpert.data.model.diagnostic.Medicine;
import com.tec.medxpert.data.model.diagnostic.VitalSigns;
import com.tec.medxpert.navigation.diagnosticInformation.DiagnosticInformationCoordinator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiagnosticInformationActivity extends AppCompatActivity {

    @Inject
    DiagnosticInformationCoordinator navigator;

    private DiagnosticInformationViewModel viewModel;
    private String diagnosticId;
    private DiagnosticMedicineAdapter medicineAdapter;
    private Button btnMedicines;
    private boolean isDoctor = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_information);

        viewModel = new ViewModelProvider(this).get(DiagnosticInformationViewModel.class);

        diagnosticId = getIntent().getStringExtra("diagnosticId");
        String documentId = getIntent().getStringExtra("documentId");

        viewModel.getCurrentUserRole();

        if (documentId != null) {
            viewModel.loadDiagnosticByAppointmentId(documentId);
        }
        if (diagnosticId != null) {
            viewModel.loadDiagnosticDetails(diagnosticId);
        }

        setupUI();
        setupObservers();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnMedicines = findViewById(R.id.btnViewMedicines);

        btnBack.setOnClickListener(v -> navigator.navigateToPreviousScreen());
        btnMedicines.setOnClickListener(v -> showMedicinePopup());
    }

    private void setupObservers() {
        viewModel.getIsDoctor().observe(this, isDoctorUser -> {
            if (isDoctorUser != null) {
                isDoctor = isDoctorUser;
                updateUIBasedOnRole();
            }
        });

        viewModel.getSelectedDiagnostic().observe(this, diagnostic -> {
            if (diagnostic != null) {
                updateUIWithDiagnostic(diagnostic);
            } else {
                Log.e("DiagnosticInformationActivity", getString(R.string.no_diagnostic_data_available));
            }
        });

        viewModel.getUpdateSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, getString(R.string.toast_frequency_updated_successfully), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUIBasedOnRole() {
        if (isDoctor) {
            btnMedicines.setText(getString(R.string.btn_medicines_text));
        }
    }

    // DiagnosticInformationActivity.java
    private void setupUI() {
        if (diagnosticId != null) {
            viewModel.loadDiagnosticDetails(diagnosticId);

            viewModel.getSelectedDiagnostic().observe(this, diagnostic -> {
                if (diagnostic != null) {

                    RecyclerView rvImages = findViewById(R.id.rvImages);
                    rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

                    List<String> imageUrls = diagnostic.getImageUrls();
                    if (imageUrls != null && !imageUrls.isEmpty()) {
                        DiagnosticImageAdapter imageAdapter = new DiagnosticImageAdapter(this, imageUrls);
                        rvImages.setAdapter(imageAdapter);
                    } else {
                        Log.e("DiagnosticInformationActivity", "No image URLs available for diagnostic ID: " + diagnosticId);
                    }

                    TextView etWeight = findViewById(R.id.etWeight);
                    TextView etPhysicalExamination = findViewById(R.id.etPhysicalExamination);
                    TextView etReasonForConsultation = findViewById(R.id.etReasonForConsultation);
                    TextView etSubjectiveCondition = findViewById(R.id.etSubjectiveCondition);
                    TextView etObjectiveCondition = findViewById(R.id.etObjectiveCondition);
                    TextView etAnalysisPlan = findViewById(R.id.etAnalysisPlan);

                    etWeight.setText(diagnostic.getWeight());
                    etPhysicalExamination.setText(diagnostic.getPhysical_examination());
                    etReasonForConsultation.setText(diagnostic.getConsultation_reason());
                    etSubjectiveCondition.setText(diagnostic.getSubjective_condition());
                    etObjectiveCondition.setText(diagnostic.getObjective_condition());
                    etAnalysisPlan.setText(diagnostic.getAnalysis_and_plan());

                    TextView tvHeartbeat = findViewById(R.id.tvHeartbeat);
                    TextView tvTemperature = findViewById(R.id.tvTemperature);
                    TextView tvBloodPressure = findViewById(R.id.tvBloodPressure);
                    TextView tvOxygenSaturation = findViewById(R.id.tvOxygenSaturation);

                    VitalSigns vitalSigns = diagnostic.getVitalSigns();
                    if (vitalSigns != null) {
                        tvHeartbeat.setText(vitalSigns.getHeartbeat());
                        tvTemperature.setText(vitalSigns.getTemperature());
                        tvBloodPressure.setText(vitalSigns.getBloodPressure());
                        tvOxygenSaturation.setText(vitalSigns.getOxygenSaturation());
                    }
                }
            });
        }
    }

    private void showMedicinePopup() {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_diagnostic_view_medicine);

        ImageButton btnBack = dialog.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> dialog.dismiss());

        RecyclerView rvMedicineList = dialog.findViewById(R.id.rvMedicineList);
        rvMedicineList.setLayoutManager(new LinearLayoutManager(this));

        TextInputEditText etSearch = dialog.findViewById(R.id.etSearch);

        List<Medicine> originalMedicines = viewModel.getMedicines().getValue();
        if (originalMedicines == null || originalMedicines.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_no_medications_available), Toast.LENGTH_SHORT).show();
            return;
        }
        List<Medicine> filteredMedicines = new ArrayList<>(originalMedicines);


        medicineAdapter = new DiagnosticMedicineAdapter(filteredMedicines);
        medicineAdapter.setDoctorView(isDoctor);

        if (isDoctor) {
            medicineAdapter.setOnEditMedicineClickListener((medicine, position) -> {
                dialog.dismiss();
                showEditMedicineFrequencyDialog(medicine, position);
            });
        }

        rvMedicineList.setAdapter(medicineAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filteredMedicines.clear();
                for (Medicine medicine : originalMedicines) {
                    if (medicine.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                        filteredMedicines.add(medicine);
                    }
                }
                medicineAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        dialog.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );

        dialog.show();
    }

    private void showEditMedicineFrequencyDialog(Medicine medicine, int position) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_frequency_medicine_diagnostic);

        TextView tvSelectedMedicine = dialog.findViewById(R.id.tvSelectedMedicine);
        TextInputEditText etHours = dialog.findViewById(R.id.etHours);
        TextInputEditText etDays = dialog.findViewById(R.id.etDays);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnAdd);

        tvSelectedMedicine.setText(getString(R.string.lb_form_edit_medication_doctor_placeholder, medicine.getName()));
        etHours.setText(String.valueOf(medicine.getHours()));
        etDays.setText(String.valueOf(medicine.getDays()));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String hoursText = etHours.getText().toString().trim();
            String daysText = etDays.getText().toString().trim();

            if (hoursText.isEmpty() || daysText.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int hours = Integer.parseInt(hoursText);
                int days = Integer.parseInt(daysText);
                if (hours <= 0 || days <= 0) {
                    Toast.makeText(this, getString(R.string.error_values_greater_than_zero), Toast.LENGTH_SHORT).show();
                    return;
                }

                Medicine updatedMedicine = new Medicine(
                        medicine.getId(),
                        medicine.getName(),
                        medicine.getDosage(),
                        hours,
                        days
                );

                viewModel.updateMedicineFrequency(diagnosticId, updatedMedicine, position);
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.error_invalid_numeric_values), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );

        dialog.show();
    }

    private void updateUIWithDiagnostic(Diagnostic diagnostic) {
        TextView etWeight = findViewById(R.id.etWeight);
        TextView etPhysicalExamination = findViewById(R.id.etPhysicalExamination);
        TextView etReasonForConsultation = findViewById(R.id.etReasonForConsultation);
        TextView etSubjectiveCondition = findViewById(R.id.etSubjectiveCondition);
        TextView etObjectiveCondition = findViewById(R.id.etObjectiveCondition);
        TextView etAnalysisPlan = findViewById(R.id.etAnalysisPlan);

        etWeight.setText(diagnostic.getWeight());
        etPhysicalExamination.setText(diagnostic.getPhysical_examination());
        etReasonForConsultation.setText(diagnostic.getConsultation_reason());
        etSubjectiveCondition.setText(diagnostic.getSubjective_condition());
        etObjectiveCondition.setText(diagnostic.getObjective_condition());
        etAnalysisPlan.setText(diagnostic.getAnalysis_and_plan());

        TextView tvHeartbeat = findViewById(R.id.tvHeartbeat);
        TextView tvTemperature = findViewById(R.id.tvTemperature);
        TextView tvBloodPressure = findViewById(R.id.tvBloodPressure);
        TextView tvOxygenSaturation = findViewById(R.id.tvOxygenSaturation);

        VitalSigns vitalSigns = diagnostic.getVitalSigns();
        if (vitalSigns != null) {
            tvHeartbeat.setText(vitalSigns.getHeartbeat());
            tvTemperature.setText(vitalSigns.getTemperature());
            tvBloodPressure.setText(vitalSigns.getBloodPressure());
            tvOxygenSaturation.setText(vitalSigns.getOxygenSaturation());
        }
    }

}