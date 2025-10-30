package com.tec.medxpert.ui.diagnosticInformation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Medicine;

import java.util.List;

public class DiagnosticMedicineAdapter extends RecyclerView.Adapter<DiagnosticMedicineAdapter.MedicineViewHolder> {

    private final List<Medicine> medicine;
    private OnEditMedicineClickListener editClickListener;
    private boolean isDoctorView = false;

    public interface OnEditMedicineClickListener {
        void onEditMedicineClick(Medicine medicine, int position);
    }

    public DiagnosticMedicineAdapter(List<Medicine> medicine2s) {
        this.medicine = medicine2s;
    }

    public void setOnEditMedicineClickListener(OnEditMedicineClickListener listener) {
        this.editClickListener = listener;
    }

    public void setDoctorView(boolean isDoctorView) {
        this.isDoctorView = isDoctorView;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_diagnostic, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine2 = medicine.get(position);
        holder.tvMedicineName.setText(medicine2.getName());
        holder.tvDosage.setText(medicine2.getDosage());
        holder.tvFrequency.setText(
                holder.itemView.getContext().getString(
                        R.string.medicine_frequency,
                        medicine2.getDays(),
                        medicine2.getHours()
                )
        );

        if (!isDoctorView) {
            holder.ivEditMedicine.setVisibility(View.GONE);
        } else {
            holder.ivEditMedicine.setVisibility(View.VISIBLE);
            holder.ivEditMedicine.setOnClickListener(v -> {
                if (editClickListener != null) {
                    editClickListener.onEditMedicineClick(medicine2, position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return medicine.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvDosage, tvFrequency;
        ImageView ivEditMedicine;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvFrequency = itemView.findViewById(R.id.tvDays);
            ivEditMedicine = itemView.findViewById(R.id.ivEditMedicine);
        }
    }
}
