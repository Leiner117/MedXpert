package com.tec.medxpert.ui.profile.patient;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tec.medxpert.R;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.tec.medxpert.data.model.profile.Patient;
import com.tec.medxpert.util.Resource;
import com.tec.medxpert.ui.profile.patient.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment for managing patient's family medical history
 */
@AndroidEntryPoint
public class FamilyMedicalHistoryFragment extends Fragment {

    private ProfileViewModel viewModel;
    private LinearLayout conditionsContainer;
    private EditText newConditionInput;
    private Button addButton;
    private Button saveButton;
    private View loadingView;

    private List<String> conditionsList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medical_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText("Family Medical History");

        conditionsContainer = view.findViewById(R.id.conditionsContainer);
        newConditionInput = view.findViewById(R.id.newConditionInput);
        addButton = view.findViewById(R.id.addButton);
        saveButton = view.findViewById(R.id.saveButton);
        loadingView = view.findViewById(R.id.loadingView);

        // Set up back button
        view.findViewById(R.id.backButton).setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Set up add button
        addButton.setOnClickListener(v -> {
            String condition = newConditionInput.getText().toString().trim();
            if (!condition.isEmpty()) {
                conditionsList.add(condition);
                updateConditionsList();
                newConditionInput.setText("");
            }
        });

        // Set up save button
        saveButton.setOnClickListener(v -> {
            viewModel.updateFamilyMedicalHistory(conditionsList);
        });

        // Observe patient data
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getPatientData().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading(true);
            } else {
                showLoading(false);

                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    Patient patient = resource.data;
                    if (patient.getPersonalData() != null && patient.getPersonalData().getFamilyMedicalHistory() != null) {
                        conditionsList = new ArrayList<>(patient.getPersonalData().getFamilyMedicalHistory());
                        updateConditionsList();
                    }
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getSaveStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading(true);
            } else {
                showLoading(false);

                if (resource.status == Resource.Status.SUCCESS && Boolean.TRUE.equals(resource.data)) {
                    Toast.makeText(getContext(), "Family medical history saved successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateConditionsList() {
        conditionsContainer.removeAllViews();

        if (conditionsList.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("No family medical conditions added");
            emptyText.setPadding(0, 16, 0, 16);
            conditionsContainer.addView(emptyText);
        } else {
            for (int i = 0; i < conditionsList.size(); i++) {
                View conditionItem = getLayoutInflater().inflate(R.layout.item_medical_condition, conditionsContainer, false);
                TextView conditionText = conditionItem.findViewById(R.id.conditionText);
                ImageButton deleteButton = conditionItem.findViewById(R.id.deleteButton);
                ImageButton editButton = conditionItem.findViewById(R.id.editButton);

                conditionText.setText(conditionsList.get(i));

                final int position = i;
                deleteButton.setOnClickListener(v -> {
                    conditionsList.remove(position);
                    updateConditionsList();
                });

                editButton.setOnClickListener(v -> {
                    showEditDialog(position);
                });

                conditionsContainer.addView(conditionItem);
            }
        }
    }

    private void showEditDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Family Medical Condition");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setText(conditionsList.get(position));
        input.setSelection(input.getText().length());
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String editedText = input.getText().toString().trim();
            if (!editedText.isEmpty()) {
                conditionsList.set(position, editedText);
                updateConditionsList();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLoading(boolean isLoading) {
        if (loadingView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (saveButton != null) {
            saveButton.setEnabled(!isLoading);
        }
        if (addButton != null) {
            addButton.setEnabled(!isLoading);
        }
    }
}
