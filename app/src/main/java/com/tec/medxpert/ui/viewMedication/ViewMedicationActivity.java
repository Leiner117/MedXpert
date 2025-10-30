package com.tec.medxpert.ui.viewMedication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tec.medxpert.R;
import com.tec.medxpert.navigation.viewMedicationCoordinator.ViewMedicationCoordinator;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ViewMedicationActivity extends ComponentActivity {
    @Inject
    ViewMedicationCoordinator coordinator;
    private RecyclerView recyclerView;
    private FloatingActionButton btnAddMedicationFab;
    private ImageButton btnBack;
    private ViewMedicationViewModel viewModel;
    private ViewMedicationAdapter adapter;
    private EditText searchView;
    private ImageView filterButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_medication);

        viewModel = new ViewModelProvider(this).get(ViewMedicationViewModel.class);

        initUI();
        setupListeners();
        observeViewModel();
    }

    private void initUI() {
        recyclerView = findViewById(R.id.medicationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnAddMedicationFab = findViewById(R.id.addMedicationFab);
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.searchEditText);
        findViewById(R.id.btnBack).setOnClickListener(v -> coordinator.navigateToMain());
        filterButton = findViewById(R.id.filterButton);
    }

    private void setupListeners() {
        btnAddMedicationFab.setOnClickListener(v -> viewModel.onAddMedicationClicked());
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

        filterButton.setOnClickListener(v -> {
            ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
            PopupMenu popupMenu = new PopupMenu(ctw, filterButton);

            popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.filter_by_name) {
                    viewModel.setSelectedFilter("name");
                } else if (id == R.id.filter_by_date) {
                    viewModel.setSelectedFilter("date");
                } else if (id == R.id.filter_by_dosage) {
                    viewModel.setSelectedFilter("dosage");
                }

                return true;
            });
            popupMenu.show();
        });
    }

    private void observeViewModel() {
        adapter = new ViewMedicationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel.getFilteredMedications().observe(this, medications -> {
            if (medications != null) {
                adapter.updateData(medications);
            }
        });

        viewModel.getSelectedFilter().observe(this, filter -> {
            switch (filter) {
                case "date":
                    searchView.setHint(R.string.search_by_date);
                    break;
                case "dosage":
                    searchView.setHint(R.string.search_by_dosage);
                    break;
                default:
                    searchView.setHint(R.string.search_by_name);
            }
        });

        viewModel.getNavigateToEditMedication().observe(this, medication -> {
            if (medication != null) {
                coordinator.navigateToAddMedication(this, medication);
                viewModel.resetNavigationStates();
            }
        });

        viewModel.getNavigateToMain().observe(this, navigate -> {
            if (navigate) {
                coordinator.navigateToMain();
                viewModel.resetNavigationStates();
            }
        });

        viewModel.getNavigateToAddMedication().observe(this, navigate -> {
            if (navigate) {
                coordinator.navigateToAddMedication(this, null);
                viewModel.resetNavigationStates();
            }
        });
    }

    public ViewMedicationViewModel getViewModel() {
        return viewModel;
    }
}
