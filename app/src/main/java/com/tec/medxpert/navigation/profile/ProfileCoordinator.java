package com.tec.medxpert.navigation.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.tec.medxpert.R;
import com.tec.medxpert.ui.profile.patient.PatientListFragment;
import com.tec.medxpert.ui.profile.patient.AllergiesFragment;
import com.tec.medxpert.ui.profile.patient.FamilyMedicalHistoryFragment;
import com.tec.medxpert.ui.profile.patient.HistoryProfileFragment;
import com.tec.medxpert.ui.profile.patient.PersonalMedicalHistoryFragment;
import com.tec.medxpert.ui.profile.patient.PatientProfileFragment;
import com.tec.medxpert.ui.profile.patient.RegisterPatientFragment;

import javax.inject.Inject;

/**
 * Coordinator class for handling navigation in the profile section
 */
public class ProfileCoordinator implements IProfileCoordinator {

    @Inject
    public ProfileCoordinator() {
    }

    @Override
    public void navigateToProfileTab(Fragment fragment) {
        if (fragment != null && fragment.isAdded()) {
            loadChildFragment(fragment.getChildFragmentManager(), new PatientProfileFragment());
        }
    }

    @Override
    public void navigateToHistoryTab(Fragment fragment) {
        if (fragment != null && fragment.isAdded()) {
            loadChildFragment(fragment.getChildFragmentManager(), new HistoryProfileFragment());
        }
    }

    @Override
    public void navigateToAllergiesScreen(Fragment fragment) {
        if (fragment != null && fragment.getActivity() != null) {
            fragment.getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new AllergiesFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void navigateToPersonalMedicalHistory(Fragment fragment) {
        if (fragment != null && fragment.getActivity() != null) {
            fragment.getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PersonalMedicalHistoryFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void navigateToFamilyMedicalHistory(Fragment fragment) {
        if (fragment != null && fragment.getActivity() != null) {
            fragment.getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FamilyMedicalHistoryFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void navigateToPatientProfile(Fragment fragment, String patientId) {
        if (fragment != null && fragment.getActivity() != null) {
            RegisterPatientFragment patientFragment = new RegisterPatientFragment();

            // Pass the patient ID as an argument
            Bundle args = new Bundle();
            args.putString("patientId", patientId);
            patientFragment.setArguments(args);

            fragment.getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, patientFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void navigateToPatientList(Fragment fragment) {
        if (fragment != null && fragment.getActivity() != null) {
            fragment.getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PatientListFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void showIdTypeSelectionDialog(Context context, String currentValue, IdTypeSelectedListener listener) {
        final String[] idTypes = {"National ID", "Passport", "Driver's License", "Other"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select ID Type");

        // Find the current selection index
        int selectedIndex = -1;
        for (int i = 0; i < idTypes.length; i++) {
            if (idTypes[i].equals(currentValue)) {
                selectedIndex = i;
                break;
            }
        }

        builder.setSingleChoiceItems(idTypes, selectedIndex, (dialog, which) -> {
            listener.onIdTypeSelected(idTypes[which]);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void showBloodTypeSelectionDialog(Context context, String currentValue, BloodTypeSelectedListener listener) {
        final String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Blood Type");

        // Find the current selection index
        int selectedIndex = -1;
        for (int i = 0; i < bloodTypes.length; i++) {
            if (bloodTypes[i].equals(currentValue)) {
                selectedIndex = i;
                break;
            }
        }

        builder.setSingleChoiceItems(bloodTypes, selectedIndex, (dialog, which) -> {
            listener.onBloodTypeSelected(bloodTypes[which]);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void showEditTextDialog(Context context, String title, String currentValue, EditTextDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentValue);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString();
            listener.onTextEntered(value);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void showNumberEditDialog(Context context, String title, Double currentValue, NumberEditDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (currentValue != null) {
            input.setText(String.valueOf(currentValue));
        }
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString();
            try {
                Double numValue = Double.parseDouble(value);
                listener.onNumberEntered(numValue);
            } catch (NumberFormatException e) {
                listener.onNumberEntered(null);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void loadChildFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
