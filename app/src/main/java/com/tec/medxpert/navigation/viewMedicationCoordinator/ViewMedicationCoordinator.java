package com.tec.medxpert.navigation.viewMedicationCoordinator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.Medication;
import com.tec.medxpert.ui.addMedication.AddMedicationActivity;
import com.tec.medxpert.ui.home.DoctorFragment;
import com.tec.medxpert.ui.viewMedication.ViewMedicationActivity;

import javax.inject.Inject;

public class ViewMedicationCoordinator implements IViewMedicationCoordinator {
    private final Activity activity;

    @Inject
    public ViewMedicationCoordinator(Activity activity) {
        this.activity = activity;

    }
    @Override
    public void navigateToMain() {
        activity.finish();
    }

    @Override
    public void navigateToAddMedication(Context context, Medication medication) {
        Intent intent = new Intent(context, AddMedicationActivity.class);
        if (medication != null) {
            intent.putExtra("medication", Medication.createSerializableCopy(medication));
        }
        context.startActivity(intent);
    }
}
