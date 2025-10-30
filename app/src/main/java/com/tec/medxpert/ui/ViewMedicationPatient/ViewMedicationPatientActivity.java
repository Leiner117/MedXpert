package com.tec.medxpert.ui.ViewMedicationPatient;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.tec.medxpert.R;
import com.tec.medxpert.navigation.medicationPatientCoordinator.MedicationPatientCoordinator;
import com.tec.medxpert.ui.ViewMedicationPatient.ViewMedicationPatientViewModel.TabType;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ViewMedicationPatientActivity extends AppCompatActivity {

    @Inject
    MedicationPatientCoordinator coordinator;
    private ViewMedicationPatientViewModel viewModel;
    private RecyclerView recyclerView;
    private ImageButton btnBack;
    private EditText searchView;
    private TabLayout tabLayout;
    private ViewMedicationPatientAdapter adapter;
    private TextView statusMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_patient);

        viewModel = new ViewModelProvider(this).get(ViewMedicationPatientViewModel.class);

        initUI();
        setupListeners();
        observeViewModel();
    }

    private void initUI() {
        recyclerView = findViewById(R.id.medicationRecyclerViewPatient);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.searchEditText);
        tabLayout = findViewById(R.id.tabLayout);
        statusMessageTextView = findViewById(R.id.statusMessageTextView);

        findViewById(R.id.btnBack).setOnClickListener(v -> coordinator.navigateToMain());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> viewModel.onBackClicked());

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TabType selectedTabType = tab.getPosition() == 0 ? TabType.IN_USE : TabType.TERMINATED;
                viewModel.setSelectedTab(selectedTabType);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void observeViewModel() {
        adapter = new ViewMedicationPatientAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel.getFilteredMedications().observe(this, medications -> {
            if (medications != null) {
                adapter.updateData(medications);
            } else {
                adapter.updateData(new ArrayList<>());
            }
        });

        viewModel.getNavigateToMain().observe(this, navigate -> {
            if (navigate != null && navigate) {
                coordinator.navigateToMain();
                viewModel.resetNavigationStates();
            }
        });

        viewModel.getStatusMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                statusMessageTextView.setText(message);
                statusMessageTextView.setVisibility(View.VISIBLE);
            } else {
                statusMessageTextView.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                statusMessageTextView.setText(getString(R.string.loading_medications));
                statusMessageTextView.setVisibility(View.VISIBLE);
            }
        });
    }
}