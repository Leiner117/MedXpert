package com.tec.medxpert.ui.profile.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.util.Resource;
import com.tec.medxpert.ui.profile.patient.viewmodel.ChangeHistoryViewModel;

import com.tec.medxpert.ui.profile.patient.viewmodel.SharedPatientViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HistoryProfileFragment extends Fragment {

    private ChangeHistoryViewModel viewModel;
    private SharedPatientViewModel sharedPatientViewModel;
    private RecyclerView historyList;
    private TextView emptyView;
    private ProgressBar loadingView;
    private ChangeHistoryAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChangeHistoryViewModel.class);
        sharedPatientViewModel = new ViewModelProvider(requireActivity()).get(SharedPatientViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        historyList = view.findViewById(R.id.history_list);
        emptyView = view.findViewById(R.id.empty_view);
        loadingView = view.findViewById(R.id.loading_view);

        // Set up RecyclerView
        historyList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChangeHistoryAdapter();
        historyList.setAdapter(adapter);

        // Observe change history
        observeViewModel();

        // Observe shared patient ID
        sharedPatientViewModel.getPatientId().observe(getViewLifecycleOwner(), patientId -> {
            if (patientId != null && !patientId.isEmpty()) {
                viewModel.setPatientId(patientId);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getChangeHistory().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading(true);
            } else {
                showLoading(false);

                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    if (resource.data.isEmpty()) {
                        showEmptyView(true);
                    } else {
                        showEmptyView(false);
                        adapter.setChangeRecords(resource.data);
                    }
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    showEmptyView(true);
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (loadingView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyView(boolean isEmpty) {
        if (emptyView != null && historyList != null) {
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            historyList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }
}
