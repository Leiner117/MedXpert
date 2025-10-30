package com.tec.medxpert.ui.addDiagnostic;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Medicine;

import java.util.List;

public class MedicineSelectorDialogDiagnostic extends Dialog implements MedicineAdapterDiagnostic.OnMedicineSelectedListener {
    private TextView tvSelectedMedicine;
    private TextInputEditText etHours, etDays, etSearch;
    private Button btnAdd;
    private MedicineAdapterDiagnostic adapter;
    private final List<Medicine> medicineList;
    private Medicine selectedMedicine;
    private final OnMedicineAddedListener listener;

    public interface OnMedicineAddedListener {
        void onMedicineAdded(Medicine medicine, int hours, int days);
    }

    public MedicineSelectorDialogDiagnostic(@NonNull Context context, List<Medicine> medicineList, OnMedicineAddedListener listener) {
        super(context);
        this.medicineList = medicineList;
        this.listener = listener;
    }

    public MedicineSelectorDialogDiagnostic(@NonNull Context context, List<Medicine> medicineList,
                                            OnMedicineAddedListener listener, Medicine preselectedMedicine) {
        super(context);
        this.medicineList = medicineList;
        this.listener = listener;
        this.selectedMedicine = preselectedMedicine;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_medicine_selector);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RecyclerView rvMedicineList = findViewById(R.id.rvMedicineList);

        tvSelectedMedicine = findViewById(R.id.tvSelectedMedicine);
        etHours = findViewById(R.id.etHours);
        etDays = findViewById(R.id.etDays);
        etSearch = findViewById(R.id.etSearch);
        Button btnCancel = findViewById(R.id.btnCancel);
        btnAdd = findViewById(R.id.btnAdd);

        rvMedicineList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicineAdapterDiagnostic(medicineList, this);
        rvMedicineList.setAdapter(adapter);

        if (selectedMedicine != null) {
            adapter.setSelectedMedicine(selectedMedicine);
            tvSelectedMedicine.setText(getContext().getString(R.string.selected_medicine,
                    selectedMedicine.getName() != null ? selectedMedicine.getName() : ""));
            btnAdd.setEnabled(true);
        }

        setupSearchFunctionality();

        btnCancel.setOnClickListener(v -> dismiss());
        btnAdd.setOnClickListener(v -> {
            if (validateInputs()) {
                int hours = Integer.parseInt(etHours.getText().toString());
                int days = Integer.parseInt(etDays.getText().toString());
                listener.onMedicineAdded(selectedMedicine, hours, days);
                dismiss();
            }
        });

        btnAdd.setEnabled(selectedMedicine != null);
    }

    private void setupSearchFunctionality() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onMedicineSelected(Medicine medicine) {
        selectedMedicine = medicine;
        tvSelectedMedicine.setText(getContext().getString(R.string.selected_medicine, medicine.getName() != null ? medicine.getName() : ""));
        btnAdd.setEnabled(true);
    }

    private boolean validateInputs() {
        if (selectedMedicine == null) {
            Toast.makeText(getContext(), getContext().getString(R.string.error_select_medication), Toast.LENGTH_SHORT).show();
            return false;
        }

        String hoursStr = etHours.getText().toString();
        String daysStr = etDays.getText().toString();

        if (TextUtils.isEmpty(hoursStr)) {
            etHours.setError(getContext().getString(R.string.error_enter_hours));
            return false;
        }

        if (TextUtils.isEmpty(daysStr)) {
            etDays.setError(getContext().getString(R.string.error_enter_days));
            return false;
        }

        int hours = Integer.parseInt(hoursStr);
        int days = Integer.parseInt(daysStr);

        if (hours <= 0) {
            etHours.setError(getContext().getString(R.string.error_hours_greater_than_zero));
            return false;
        }

        if (days <= 0) {
            etDays.setError(getContext().getString(R.string.error_days_greater_than_zero));
            return false;
        }

        return true;
    }
}
