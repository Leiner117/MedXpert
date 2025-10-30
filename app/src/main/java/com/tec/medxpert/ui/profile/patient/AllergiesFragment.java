package com.tec.medxpert.ui.profile.patient;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.profile.Patient;
import com.tec.medxpert.util.Resource;
import com.tec.medxpert.ui.profile.patient.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment for managing patient allergies
 */
@AndroidEntryPoint
public class AllergiesFragment extends Fragment {

    private ProfileViewModel viewModel;
    private LinearLayout allergiesContainer;
    private EditText newAllergyInput;
    private Button addButton;
    private Button saveButton;
    private View loadingView;

    private List<String> allergiesList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_allergies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        allergiesContainer = view.findViewById(R.id.allergiesContainer);
        newAllergyInput = view.findViewById(R.id.newAllergyInput);
        addButton = view.findViewById(R.id.addButton);
        saveButton = view.findViewById(R.id.saveButton);
        loadingView = view.findViewById(R.id.loadingView);

        // Set up back button
        view.findViewById(R.id.backButton).setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Set up add button
        addButton.setOnClickListener(v -> {
            String allergy = newAllergyInput.getText().toString().trim();
            if (!allergy.isEmpty()) {
                allergiesList.add(allergy);
                updateAllergiesList();
                newAllergyInput.setText("");
            }
        });

        // Set up save button
        saveButton.setOnClickListener(v -> {
            viewModel.updateAllergies(allergiesList);
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
                    if (patient.getPersonalData() != null && patient.getPersonalData().getAllergies() != null) {
                        allergiesList = new ArrayList<>(patient.getPersonalData().getAllergies());
                        updateAllergiesList();
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
                    Toast.makeText(getContext(), "Allergies saved successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateAllergiesList() {
        allergiesContainer.removeAllViews();

        if (allergiesList.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("No allergies added");
            emptyText.setPadding(0, 16, 0, 16);
            allergiesContainer.addView(emptyText);
        } else {
            for (int i = 0; i < allergiesList.size(); i++) {
                View allergyItem = getLayoutInflater().inflate(R.layout.item_allergy, allergiesContainer, false);
                TextView allergyText = allergyItem.findViewById(R.id.allergyText);
                ImageButton deleteButton = allergyItem.findViewById(R.id.deleteButton);
                ImageButton editButton = allergyItem.findViewById(R.id.editButton);

                allergyText.setText(allergiesList.get(i));

                final int position = i;
                deleteButton.setOnClickListener(v -> {
                    allergiesList.remove(position);
                    updateAllergiesList();
                });

                editButton.setOnClickListener(v -> {
                    showEditDialog(position);
                });

                allergiesContainer.addView(allergyItem);
            }
        }
    }

    private void showEditDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Allergy");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setText(allergiesList.get(position));
        input.setSelection(input.getText().length());
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String editedText = input.getText().toString().trim();
            if (!editedText.isEmpty()) {
                allergiesList.set(position, editedText);
                updateAllergiesList();
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
