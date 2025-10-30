package com.tec.medxpert.ui.addMedication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.Medication;
import com.tec.medxpert.navigation.addMedicationCoordinator.AddMedicationCoordinator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddMedicationActivity extends AppCompatActivity {

    @Inject
    AddMedicationCoordinator coordinator;
    private EditText medicationName, medicationDescription, medicationDosage, medicationFrequencyHours, medicationFrequencyDays;
    private Button btnCancel, btnAdd;
    private ImageButton btnBack;
    private AddMedicationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        viewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(AddMedicationViewModel.class);

        Medication medicationToEdit = (Medication) getIntent().getSerializableExtra("medication");

        initUI(medicationToEdit);
        setupListeners(medicationToEdit);
        observeViewModel();
    }

    private void initUI(Medication medicationToEdit) {
        medicationName = findViewById(R.id.editTextName);
        medicationDescription = findViewById(R.id.editTextDescription);
        medicationDosage = findViewById(R.id.editTextDosage);
        medicationFrequencyHours = findViewById(R.id.editTextHours);
        medicationFrequencyDays = findViewById(R.id.editTextDays);
        btnAdd = findViewById(R.id.btnAdd);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);

        if (medicationToEdit != null) {
            medicationName.setText(medicationToEdit.getName());
            medicationDescription.setText(medicationToEdit.getDescription());
            medicationDosage.setText(medicationToEdit.getDosage());

            if (medicationToEdit.getDefaultFrequency() != null) {
                if (medicationToEdit.getDefaultFrequency().get("hours") != null) {
                    medicationFrequencyHours.setText(String.valueOf(medicationToEdit.getDefaultFrequency().get("hours")));
                }
                if (medicationToEdit.getDefaultFrequency().get("days") != null) {
                    medicationFrequencyDays.setText(String.valueOf(medicationToEdit.getDefaultFrequency().get("days")));
                }
            }

            btnAdd.setText(getString(R.string.save_button_text));
        }
    }

    private void setupListeners(Medication medicationToEdit) {
        btnAdd.setOnClickListener(v -> {
            if (medicationToEdit == null) {
                viewModel.onAddClicked(
                        medicationName.getText().toString(),
                        medicationDescription.getText().toString(),
                        medicationDosage.getText().toString(),
                        medicationFrequencyHours.getText().toString(),
                        medicationFrequencyDays.getText().toString()
                );
            } else {
                viewModel.onSaveClicked(
                        medicationToEdit,
                        medicationName.getText().toString(),
                        medicationDescription.getText().toString(),
                        medicationDosage.getText().toString(),
                        medicationFrequencyHours.getText().toString(),
                        medicationFrequencyDays.getText().toString()
                );
            }
        });

        btnCancel.setOnClickListener(v -> viewModel.onCancelClicked());
        btnBack.setOnClickListener(v -> viewModel.onCancelClicked());
    }

    private void observeViewModel() {
        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(AddMedicationActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigateBack().observe(this, shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                coordinator.navigateToViewMedication(this);
                finish();
            }
        });
    }
}
