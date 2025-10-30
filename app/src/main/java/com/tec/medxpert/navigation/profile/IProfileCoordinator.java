package com.tec.medxpert.navigation.profile;

import android.content.Context;

import androidx.fragment.app.Fragment;

/**
 * Interface for the profile coordinator
 */
public interface IProfileCoordinator {
    void navigateToProfileTab(Fragment fragment);
    void navigateToHistoryTab(Fragment fragment);
    void navigateToAllergiesScreen(Fragment fragment);
    void navigateToPersonalMedicalHistory(Fragment fragment);
    void navigateToFamilyMedicalHistory(Fragment fragment);
    void navigateToPatientProfile(Fragment fragment, String patientId);
    void navigateToPatientList(Fragment fragment);

    void showIdTypeSelectionDialog(Context context, String currentValue, IdTypeSelectedListener listener);
    void showBloodTypeSelectionDialog(Context context, String currentValue, BloodTypeSelectedListener listener);
    void showEditTextDialog(Context context, String title, String currentValue, EditTextDialogListener listener);
    void showNumberEditDialog(Context context, String title, Double currentValue, NumberEditDialogListener listener);

    interface IdTypeSelectedListener {
        void onIdTypeSelected(String idType);
    }

    interface BloodTypeSelectedListener {
        void onBloodTypeSelected(String bloodType);
    }

    interface EditTextDialogListener {
        void onTextEntered(String text);
    }

    interface NumberEditDialogListener {
        void onNumberEntered(Double number);
    }
}

