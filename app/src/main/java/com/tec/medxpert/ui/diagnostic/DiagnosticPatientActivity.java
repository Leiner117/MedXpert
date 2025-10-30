package com.tec.medxpert.ui.diagnostic;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import com.tec.medxpert.navigation.diagnostic.DiagnosticCoordinator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.tec.medxpert.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class DiagnosticPatientActivity extends AppCompatActivity {

    @Inject
    DiagnosticCoordinator diagnosticCoordinator;
    private DiagnosticPatientViewModel viewModel;
    private RecyclerView diagnosticsRecyclerView;
    private DiagnosticPatientAdapter adapter;
    private EditText searchEditText;
    private ImageView filterButton;
    private TextView dateFilterOption, nameDiagnosticFilterOption;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private TextView dateRangeTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_view);

        diagnosticCoordinator.setContext(this);
        viewModel = new ViewModelProvider(this).get(DiagnosticPatientViewModel.class);

        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setHint(R.string.search_by_diagnostic);

        filterButton = findViewById(R.id.filterButton);
        diagnosticsRecyclerView = findViewById(R.id.diagnosticsRecyclerView);
        dateFilterOption = findViewById(R.id.dateFilterOption);
        nameDiagnosticFilterOption = findViewById(R.id.nameDiagnosticFilterOption);
        diagnosticsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        TextView nameFilterOption = findViewById(R.id.nameFilterOption);
        nameFilterOption.setVisibility(View.GONE);

        TextView idFilterOption = findViewById(R.id.idFilterOption);
        idFilterOption.setVisibility(View.GONE);

        adapter = new DiagnosticPatientAdapter(new ArrayList<>(), diagnosticCoordinator);
        diagnosticsRecyclerView.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView filterByDateIcon = findViewById(R.id.filter_by_date);
        dateRangeTextView = findViewById(R.id.dateRangeTextView);

        btnBack.setOnClickListener(v -> finish());

        filterButton.setOnClickListener(v -> {
            CardView filterPopup = findViewById(R.id.filterPopup);
            if (filterPopup.getVisibility() == View.VISIBLE) {
                filterPopup.setVisibility(View.GONE);
            } else {
                filterPopup.setVisibility(View.VISIBLE);
            }
        });

        //Filter by diagnostic name
        nameDiagnosticFilterOption.setOnClickListener(v -> {
            viewModel.applyFilter("consultation_reason");
            findViewById(R.id.filterPopup).setVisibility(View.GONE);
        });

        // Filter by date
        dateFilterOption.setOnClickListener(v -> {
            viewModel.applyFilter("updatedAt");
            findViewById(R.id.filterPopup).setVisibility(View.GONE);
        });

        filterByDateIcon.setOnClickListener(v -> {
            MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.select_a_date_range)
                .setTheme(R.style.CustomDatePickerTheme)
                .build();

            dateRangePicker.show(getSupportFragmentManager(), "date_range_picker");

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null) {
                    long startMillis = selection.first;
                    long endMillis = selection.second;

                    SimpleDateFormat formatter = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

                    String startDateStr = formatter.format(startMillis);
                    String endDateStr = formatter.format(endMillis);

                    dateRangeTextView.setText(getString(R.string.date_range_format, startDateStr, endDateStr));
                    dateRangeTextView.setVisibility(View.VISIBLE);

                    viewModel.filterByDateRange(startMillis, endMillis);
                }
            });
        });

        viewModel.applyFilter("consultation_reason");

        compositeDisposable.add(viewModel.getDiagnostics()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(diagnostics -> adapter.updateDiagnostics(diagnostics)));

        compositeDisposable.add(viewModel.getFilter()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(filter -> searchEditText.setHint(
                        filter.equals("consultation_reason") ? R.string.search_by_diagnostic :
                                R.string.search_by_date
                )));

        viewModel.fetchDiagnosticsFromFirebase();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterDiagnostics(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.onDestroy();
    }
}