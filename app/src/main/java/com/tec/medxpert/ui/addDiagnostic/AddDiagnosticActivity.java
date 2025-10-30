package com.tec.medxpert.ui.addDiagnostic;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.VitalSigns;
import com.tec.medxpert.navigation.addDiagnostic.AddDiagnosticCoodinator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddDiagnosticActivity extends AppCompatActivity implements SelectedMedicineAdapterDiagnostic.OnMedicineRemovedListener {
    @Inject
    AddDiagnosticCoodinator coordinator;

    private String appointmentId, patientId, patientName;
    private EditText etWeight, etPhysicalExamination, etReasonForConsultation,
            etSubjectiveCondition, etObjectiveCondition, etAnalysisPlan;
    private Button btnAddMedicines, saveDiagnostic;
    private CardView cvHeartbeat, cvTemperature, cvBloodPressure, cvOxygenSaturation;
    private TextView tvHeartbeatValue, tvTemperatureValue, tvBloodPressureValue, tvOxygenSaturationValue;
    private AddDiagnosticViewModel viewModel;
    private RecyclerView rvMedicines;
    private SelectedMedicineAdapterDiagnostic selectedMedicineAdapter;
    private ImageButton btnChooseImage, btnBack;
    private LinearLayout llSelectedImages;
    private ProgressBar progressBarUpload;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_form);

        viewModel = new ViewModelProvider(this).get(AddDiagnosticViewModel.class);

        appointmentId = getIntent().getStringExtra("appointmentId");
        patientId = getIntent().getStringExtra("patientId");
        patientName = getIntent().getStringExtra("patientName");

        initUI();
        setupListeners();
        observeViewModel();
    }

    private void initUI() {
        etWeight = findViewById(R.id.etWeight);
        etPhysicalExamination = findViewById(R.id.etPhysicalExamination);
        etReasonForConsultation = findViewById(R.id.etReasonForConsultation);
        etSubjectiveCondition = findViewById(R.id.etSubjectiveCondition);
        etObjectiveCondition = findViewById(R.id.etObjectiveCondition);
        etAnalysisPlan = findViewById(R.id.etAnalysisPlan);

        btnAddMedicines = findViewById(R.id.btnAddMedicines);
        rvMedicines = findViewById(R.id.rvMedicines);

        cvHeartbeat = findViewById(R.id.cvHeartbeat);
        cvTemperature = findViewById(R.id.cvTemperature);
        cvBloodPressure = findViewById(R.id.cvBloodPressure);
        cvOxygenSaturation = findViewById(R.id.cvOxygenSaturation);

        tvHeartbeatValue = findViewById(R.id.tvHeartbeatValue);
        tvTemperatureValue = findViewById(R.id.tvTemperatureValue);
        tvBloodPressureValue = findViewById(R.id.tvBloodPressureValue);
        tvOxygenSaturationValue = findViewById(R.id.tvOxygenSaturationValue);

        saveDiagnostic = findViewById(R.id.btnSaveDiagnostic);

        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnBack = findViewById(R.id.btnBack);
        llSelectedImages = createSelectedImagesContainer();
        progressBarUpload = createProgressBar();

        LinearLayout mainLayout = findViewById(R.id.mainLinearLayout);
        int insertIndex = findInsertIndexForImages(mainLayout);
        mainLayout.addView(llSelectedImages, insertIndex);
        mainLayout.addView(progressBarUpload, insertIndex + 1);
    }

    private void setupListeners() {

        saveDiagnostic.setOnClickListener(v -> {
            if (validateForm()) {
                viewModel.onClickedSaveDiagnostic(
                        appointmentId,
                        patientId,
                        patientName,
                        etWeight.getText().toString(),
                        etPhysicalExamination.getText().toString(),
                        etReasonForConsultation.getText().toString(),
                        etSubjectiveCondition.getText().toString(),
                        etObjectiveCondition.getText().toString(),
                        etAnalysisPlan.getText().toString()
                );

                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show();
            }

        });

        rvMedicines.setLayoutManager(new LinearLayoutManager(this));
        selectedMedicineAdapter = new SelectedMedicineAdapterDiagnostic(viewModel.getSelectedMedicines().getValue(), this);
        rvMedicines.setAdapter(selectedMedicineAdapter);

        cvHeartbeat.setOnClickListener(v -> showVitalSignDialog(VitalSignInputDialog.VitalSignType.HEARTBEAT));
        cvTemperature.setOnClickListener(v -> showVitalSignDialog(VitalSignInputDialog.VitalSignType.TEMPERATURE));
        cvBloodPressure.setOnClickListener(v -> showVitalSignDialog(VitalSignInputDialog.VitalSignType.BLOOD_PRESSURE));
        cvOxygenSaturation.setOnClickListener(v -> showVitalSignDialog(VitalSignInputDialog.VitalSignType.OXYGEN_SATURATION));

        btnAddMedicines.setOnClickListener(v -> showMedicineSelectorDialog());

        btnChooseImage.setOnClickListener(v -> {
            if (checkPermissions()) {
                openImagePicker();
            } else {
                requestPermissions();
            }
        });

        btnBack.setOnClickListener(v -> {
            viewModel.onClickedBack();
        });
    }

    private void observeViewModel() {
        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(AddDiagnosticActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigateBack().observe(this, shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                coordinator.navigateToBack(this);
                finish();
            }
        });

        viewModel.getSelectedMedicines().observe(this, medicines -> {
            selectedMedicineAdapter.updateSelectedMedicines(medicines);

            if (medicines != null && !medicines.isEmpty()) {
                rvMedicines.setVisibility(View.VISIBLE);
            } else {
                rvMedicines.setVisibility(View.GONE);
            }
        });

        viewModel.getVitalSigns().observe(this, vitalSigns -> {
            if (vitalSigns != null) {
                updateVitalSignsDisplay(vitalSigns);
            }
        });

        viewModel.getSelectedImages().observe(this, this::updateSelectedImagesDisplay);

        viewModel.getIsUploading().observe(this, isUploading -> {
            if (isUploading != null) {
                progressBarUpload.setVisibility(isUploading ? View.VISIBLE : View.GONE);
                saveDiagnostic.setEnabled(!isUploading);
                btnChooseImage.setEnabled(!isUploading);
            }
        });
    }

    private void showVitalSignDialog(VitalSignInputDialog.VitalSignType type) {
        VitalSignInputDialog dialog = new VitalSignInputDialog(this, type, (vitalType, value) -> {
            viewModel.updateVitalSign(vitalType, value);
        });
        dialog.show();
    }

    private void updateVitalSignsDisplay(VitalSigns vitalSigns) {
        tvHeartbeatValue.setText(vitalSigns.getHeartbeat() != null ?
                vitalSigns.getHeartbeat() + " bpm" : "-- bpm");

        tvTemperatureValue.setText(vitalSigns.getTemperature() != null ?
                vitalSigns.getTemperature() + " °C" : "-- °C");

        tvBloodPressureValue.setText(vitalSigns.getBloodPressure() != null ?
                vitalSigns.getBloodPressure() + " mmHg" : "--/-- mmHg");

        tvOxygenSaturationValue.setText(vitalSigns.getOxygenSaturation() != null ?
                vitalSigns.getOxygenSaturation() + " %" : "-- %");
    }

    private boolean validateForm() {
        String weight = etWeight.getText().toString();
        String physicalExamination = etPhysicalExamination.getText().toString();
        String reasonForConsultation = etReasonForConsultation.getText().toString();
        String subjectiveCondition = etSubjectiveCondition.getText().toString();
        String objectiveCondition = etObjectiveCondition.getText().toString();
        String analysisPlan = etAnalysisPlan.getText().toString();

        boolean isValid = true;

        if (weight.isEmpty()) {
            etWeight.setError(getString(R.string.error_weight_required));
            isValid = false;
        } else {
            etWeight.setError(null);
        }

        if (physicalExamination.isEmpty()) {
            etPhysicalExamination.setError(getString(R.string.error_physical_examination_required));
            isValid = false;
        } else {
            etPhysicalExamination.setError(null);
        }

        if (reasonForConsultation.isEmpty()) {
            etReasonForConsultation.setError(getString(R.string.error_reason_for_consultation_required));
            isValid = false;
        } else {
            etReasonForConsultation.setError(null);
        }

        if (subjectiveCondition.isEmpty()) {
            etSubjectiveCondition.setError(getString(R.string.error_subjective_condition_required));
            isValid = false;
        } else {
            etSubjectiveCondition.setError(null);
        }

        if (objectiveCondition.isEmpty()) {
            etObjectiveCondition.setError(getString(R.string.error_objective_condition_required));
            isValid = false;
        } else {
            etObjectiveCondition.setError(null);
        }

        if (analysisPlan.isEmpty()) {
            etAnalysisPlan.setError(getString(R.string.error_analysis_plan_required));
            isValid = false;
        } else {
            etAnalysisPlan.setError(null);
        }

        if (!validateVitalSigns()) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validateVitalSigns() {
        VitalSigns currentVitalSigns = viewModel.getVitalSigns().getValue();

        if (currentVitalSigns == null) {
            showVitalSignsErrorDialog(getString(R.string.error_vital_signs_required));
            return false;
        }

        List<String> missingVitalSigns = new ArrayList<>();

        if (currentVitalSigns.getHeartbeat() == null || currentVitalSigns.getHeartbeat().trim().isEmpty()) {
            missingVitalSigns.add(getString(R.string.vital_sign_heart_rate));
            highlightMissingVitalSign(cvHeartbeat);
        } else {
            removeVitalSignHighlight(cvHeartbeat);
        }

        if (currentVitalSigns.getTemperature() == null || currentVitalSigns.getTemperature().trim().isEmpty()) {
            missingVitalSigns.add(getString(R.string.vital_sign_temperature));
            highlightMissingVitalSign(cvTemperature);
        } else {
            removeVitalSignHighlight(cvTemperature);
        }

        if (currentVitalSigns.getBloodPressure() == null || currentVitalSigns.getBloodPressure().trim().isEmpty()) {
            missingVitalSigns.add(getString(R.string.vital_sign_blood_pressure));
            highlightMissingVitalSign(cvBloodPressure);
        } else {
            removeVitalSignHighlight(cvBloodPressure);
        }

        if (currentVitalSigns.getOxygenSaturation() == null || currentVitalSigns.getOxygenSaturation().trim().isEmpty()) {
            missingVitalSigns.add(getString(R.string.vital_sign_oxygen_saturation));
            highlightMissingVitalSign(cvOxygenSaturation);
        } else {
            removeVitalSignHighlight(cvOxygenSaturation);
        }

        if (!missingVitalSigns.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder(getString(R.string.error_missing_vital_signs));
            for (String vitalSign : missingVitalSigns) {
                errorMessage.append("• ").append(vitalSign).append("\n");
            }
            showVitalSignsErrorDialog(errorMessage.toString());
            return false;
        }

        return true;
    }

    private void showVitalSignsErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_missing_vital_signs_title))
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void highlightMissingVitalSign(CardView cardView) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));

        ObjectAnimator animator = ObjectAnimator.ofFloat(cardView, "alpha", 1f, 0.5f, 1f);
        animator.setDuration(1000);
        animator.setRepeatCount(5);
        animator.start();
    }

    private void removeVitalSignHighlight(CardView cardView) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blue)); // Color original
    }

    private void showMedicineSelectorDialog() {
        viewModel.getMedicineList().observe(this, medicines -> {
            if (medicines != null && !medicines.isEmpty()) {
                MedicineSelectorDialogDiagnostic dialog = new MedicineSelectorDialogDiagnostic(
                        this,
                        medicines,
                        (medicine, hours, days) -> viewModel.addSelectedMedicine(medicine, hours, days)
                );
                dialog.show();
            }
        });
    }

    @Override
    public void onMedicineRemoved(int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_confirm_deletion_title))
                .setMessage(getString(R.string.dialog_confirm_delete_medication_message))
                .setPositiveButton(getString(R.string.dialog_positive_button_delete), (dialog, which) -> {
                    viewModel.removeSelectedMedicine(position);
                    Toast.makeText(this, getString(R.string.toast_medication_eliminated), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.dialog_negative_button_cancel), null)
                .show();
    }
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        Intent data = result.getData();

                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                viewModel.addSelectedImage(imageUri);
                            }
                        } else if (data.getData() != null) {
                            Uri imageUri = data.getData();
                            viewModel.addSelectedImage(imageUri);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.toast_error_getting_selected_images), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private LinearLayout createSelectedImagesContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setVisibility(View.GONE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(16));
        container.setLayoutParams(params);

        return container;
    }

    private ProgressBar createProgressBar() {
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(16));
        progressBar.setLayoutParams(params);

        return progressBar;
    }

    private int findInsertIndexForImages(LinearLayout mainLayout) {
        for (int i = 0; i < mainLayout.getChildCount(); i++) {
            View child = mainLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout childLayout = (LinearLayout) child;
                if (childLayout.getChildCount() > 0 &&
                        childLayout.getChildAt(0) instanceof ImageButton) {
                    return i;
                }
            }
        }
        return mainLayout.getChildCount() - 2;
    }

    private void updateSelectedImagesDisplay(List<Uri> images) {
        llSelectedImages.removeAllViews();

        if (images != null && !images.isEmpty()) {
            llSelectedImages.setVisibility(View.VISIBLE);

            for (int i = 0; i < images.size(); i++) {
                Uri imageUri = images.get(i);
                View imageView = createImagePreview(imageUri, i);
                llSelectedImages.addView(imageView);
            }
        } else {
            llSelectedImages.setVisibility(View.GONE);
        }
    }

    private View createImagePreview(Uri imageUri, int position) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(0, dpToPx(8), 0, dpToPx(8));

        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                dpToPx(80), dpToPx(80)
        );
        imageParams.setMargins(0, 0, dpToPx(16), 0);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(imageView);

        ImageButton btnRemove = new ImageButton(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                dpToPx(32), dpToPx(32)
        );
        btnRemove.setLayoutParams(btnParams);
        btnRemove.setImageResource(android.R.drawable.ic_menu_delete);
        btnRemove.setBackground(ContextCompat.getDrawable(this, android.R.drawable.btn_default));
        btnRemove.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_confirm_deletion_title))
                    .setMessage(getString(R.string.dialog_confirm_delete_image_message))
                    .setPositiveButton(getString(R.string.dialog_positive_button_delete_image), (dialog, which) -> {
                        viewModel.removeSelectedImage(position);
                        Toast.makeText(this, getString(R.string.toast_image_removed), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.dialog_negative_button_cancel), null)
                    .show();
        });

        container.addView(imageView);
        container.addView(btnRemove);

        return container;
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, getString(R.string.image_picker_title)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, getString(R.string.toast_permission_required), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
