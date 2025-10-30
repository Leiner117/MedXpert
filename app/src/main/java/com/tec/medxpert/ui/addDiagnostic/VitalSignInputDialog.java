package com.tec.medxpert.ui.addDiagnostic;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tec.medxpert.R;

public class VitalSignInputDialog extends Dialog {
    public enum VitalSignType {
        HEARTBEAT("Heartbeat", "bpm", "numberDecimal"),
        TEMPERATURE("Temperature", "°C", "numberDecimal"),
        BLOOD_PRESSURE("Blood Pressure", "mmHg (ej: 120/80)", "text"),
        OXYGEN_SATURATION("Oxygen Saturation", "%", "numberDecimal");

        private final String title;
        private final String unit;
        private final String inputType;

        VitalSignType(String title, String unit, String inputType) {
            this.title = title;
            this.unit = unit;
            this.inputType = inputType;
        }

        public String getTitle() { return title; }
        public String getUnit() { return unit; }
        public String getInputType() { return inputType; }
    }

    public interface OnVitalSignSavedListener {
        void onVitalSignSaved(VitalSignType type, String value);
    }

    private VitalSignType vitalSignType;
    private OnVitalSignSavedListener listener;
    private TextView tvDialogTitle, tvDialogDescription;
    private EditText etVitalSignValue;
    private Button btnCancel, btnSave;

    public VitalSignInputDialog(Context context, VitalSignType type, OnVitalSignSavedListener listener) {
        super(context);
        this.vitalSignType = type;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_vital_sign_input);

        initViews();
        setupDialog();
        setupListeners();
    }

    private void initViews() {
        tvDialogTitle = findViewById(R.id.tvDialogTitle);
        tvDialogDescription = findViewById(R.id.tvDialogDescription);
        etVitalSignValue = findViewById(R.id.etVitalSignValue);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupDialog() {
        tvDialogTitle.setText(vitalSignType.getTitle());
        tvDialogDescription.setText(getContext().getString(R.string.dialog_description, vitalSignType.getTitle().toLowerCase()));
        etVitalSignValue.setHint(getContext().getString(R.string.hint_vital_sign_value, vitalSignType.getUnit()));

        switch (vitalSignType.getInputType()) {
            case "numberDecimal":
                etVitalSignValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case "text":
                etVitalSignValue.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                break;
        }
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String value = etVitalSignValue.getText().toString().trim();

            if (TextUtils.isEmpty(value)) {
                Toast.makeText(getContext(), getContext().getString(R.string.error_enter_value), Toast.LENGTH_SHORT).show();
                return;
            }

            if (vitalSignType == VitalSignType.BLOOD_PRESSURE) {
                if (!value.matches("\\d+/\\d+")) {
                    Toast.makeText(getContext(), getContext().getString(R.string.error_invalid_format_blood_pressure), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (listener != null) {
                listener.onVitalSignSaved(vitalSignType, value);
            }
            dismiss();
        });
    }
}
