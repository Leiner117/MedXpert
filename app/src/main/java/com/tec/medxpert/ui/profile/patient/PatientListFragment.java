package com.tec.medxpert.ui.profile.patient;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.profile.Patient;
import com.tec.medxpert.navigation.profile.IProfileCoordinator;
import com.tec.medxpert.util.Resource;
import com.tec.medxpert.ui.profile.patient.viewmodel.PatientListViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment for displaying and filtering the patient list
 */
@AndroidEntryPoint
public class PatientListFragment extends Fragment implements PatientListAdapter.OnPatientClickListener {

    @Inject
    IProfileCoordinator profileCoordinator;

    private PatientListViewModel viewModel;
    private RecyclerView patientListView;
    private EditText searchInput;
    private ImageButton filterButton;
    private LinearLayout filterOptionsMenu;
    private TextView sortByNameOption;
    private TextView sortByDateOption;
    private TextView emptyView;
    private ProgressBar loadingView;
    private PatientListAdapter adapter;
    private ImageButton backButton;

    private List<Patient> allPatients = new ArrayList<>();
    private boolean sortNameAscending = true;
    private boolean sortDateAscending = false;
    private String currentFilter = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PatientListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        patientListView = view.findViewById(R.id.patientList);
        searchInput = view.findViewById(R.id.searchInput);
        filterButton = view.findViewById(R.id.filterButton);
        filterOptionsMenu = view.findViewById(R.id.filterOptionsMenu);
        sortByNameOption = view.findViewById(R.id.sortByNameOption);
        sortByDateOption = view.findViewById(R.id.sortByDateOption);
        emptyView = view.findViewById(R.id.emptyView);
        loadingView = view.findViewById(R.id.loadingView);
        backButton = view.findViewById(R.id.backButton);

        // Set up back button
        backButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Set up RecyclerView
        adapter = new PatientListAdapter(this);
        patientListView.setAdapter(adapter);

        // Set up search input
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filterPatients(s.toString(), allPatients);
                updateEmptyView();
            }
        });

        // Set up filter button
        filterButton.setOnClickListener(v -> {
            if (filterOptionsMenu.getVisibility() == View.VISIBLE) {
                filterOptionsMenu.setVisibility(View.GONE);
            } else {
                filterOptionsMenu.setVisibility(View.VISIBLE);
            }
        });

        // Set up sort options
        sortByNameOption.setOnClickListener(v -> {
            sortNameAscending = !sortNameAscending;
            viewModel.sortPatientsByName(sortNameAscending);
            filterOptionsMenu.setVisibility(View.GONE);

            // Update search hint to indicate name filter is active
            currentFilter = getString(R.string.name) + " (" +
                    getString(sortNameAscending ? R.string.filter_a_z : R.string.filter_z_a) + ")";
            updateSearchHint();
        });

        sortByDateOption.setOnClickListener(v -> {
            sortDateAscending = !sortDateAscending;
            viewModel.sortPatientsByDate(sortDateAscending);
            filterOptionsMenu.setVisibility(View.GONE);

            // Update search hint to indicate date filter is active
            currentFilter = getString(R.string.registration_date) + " (" +
                    getString(sortDateAscending ? R.string.filter_oldest : R.string.filter_newest) + ")";
            updateSearchHint();
        });

        // Close filter menu when clicking outside
        view.setOnClickListener(v -> {
            if (filterOptionsMenu.getVisibility() == View.VISIBLE &&
                    v.getId() != R.id.filterButton &&
                    v.getId() != R.id.filterOptionsMenu) {
                filterOptionsMenu.setVisibility(View.GONE);
            }
        });

        // Observe patient list
        observeViewModel();
    }

    /**
     * Updates the search hint based on the current filter
     */
    private void updateSearchHint() {
        if (currentFilter.isEmpty()) {
            searchInput.setHint(getString(R.string.search_by_name_or_id));
        } else {
            searchInput.setHint(getString(R.string.search_by_name_or_id) + " • " + currentFilter);
        }
    }

    /**
     * Clears the current filter and resets the search hint
     */
    private void clearFilter() {
        currentFilter = "";
        updateSearchHint();
        viewModel.loadPatients();
    }

    private void observeViewModel() {
        viewModel.getPatientList().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading(true);
            } else {
                showLoading(false);

                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    allPatients = resource.data;
                    adapter.setPatients(allPatients);
                    updateEmptyView();
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    updateEmptyView();
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            patientListView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            patientListView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        if (loadingView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onPatientClick(Patient patient) {
        // Navigate to patient profile
        if (patient != null && patient.getPatientId() != null) {
            profileCoordinator.navigateToPatientProfile(this, patient.getPatientId());
        }
    }
}