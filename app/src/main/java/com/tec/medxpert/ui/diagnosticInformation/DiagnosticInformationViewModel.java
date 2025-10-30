package com.tec.medxpert.ui.diagnosticInformation;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Diagnostic;
import com.tec.medxpert.data.model.diagnostic.Medicine;
import com.tec.medxpert.data.repository.DiagnosticRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import com.google.firebase.firestore.FirebaseFirestore;
@HiltViewModel
public class DiagnosticInformationViewModel extends ViewModel {

    private final MutableLiveData<List<Medicine>> medicines = new MutableLiveData<>();
    private final MutableLiveData<Diagnostic> selectedDiagnostic = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> userRole = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDoctor = new MutableLiveData<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Inject
    public Application application;

    private final DiagnosticRepository repository;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    @Inject
    public DiagnosticInformationViewModel(DiagnosticRepository repository) {
        this.repository = repository;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void getCurrentUserRole() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            firestore.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if (role != null) {
                                userRole.setValue(role);
                                isDoctor.setValue("doctor".equals(role));
                            } else {
                                errorMessage.setValue(application.getString(R.string.error_user_role_not_found));
                            }
                        } else {
                            errorMessage.setValue(application.getString(R.string.error_user_not_found));
                        }
                    })
                    .addOnFailureListener(e -> {
                        errorMessage.setValue(application.getString(R.string.error_loading_user_role));
                    });
        } else {
            errorMessage.setValue(application.getString(R.string.error_user_not_authenticated));
        }
    }

    public LiveData<String> getUserRole() {
        return userRole;
    }

    public LiveData<Boolean> getIsDoctor() {
        return isDoctor;
    }

    public void loadDiagnosticDetails(String diagnosticId) {
        compositeDisposable.add(
            repository.getDiagnostics()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(diagnostics -> {
                for (Diagnostic diagnostic : diagnostics) {
                    if (diagnostic.getPatientId().equals(diagnosticId)) {
                        selectedDiagnostic.setValue(diagnostic);
                        medicines.setValue(diagnostic.getMedicineList());
                        break;
                    }
                }
            }, throwable -> {
                errorMessage.setValue(application.getString(R.string.error_loading_diagnostic_details));
            })
        );
    }

    public void updateMedicineFrequency(String diagnosticId, Medicine updatedMedicine, int position) {
        Diagnostic currentDiagnostic = selectedDiagnostic.getValue();
        if (currentDiagnostic == null) {
            errorMessage.setValue(application.getString(R.string.error_no_diagnosis_found));
            return;
        }

        List<Medicine> currentMedicines = new ArrayList<>(medicines.getValue());
        currentMedicines.set(position, updatedMedicine);

        currentDiagnostic.setMedicineList(currentMedicines);
        compositeDisposable.add(
                repository.updateDiagnostic(diagnosticId, currentDiagnostic)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            medicines.setValue(currentMedicines);
                            selectedDiagnostic.setValue(currentDiagnostic);
                            updateSuccess.setValue(true);
                        }, throwable -> {
                            errorMessage.setValue(application.getString(R.string.error_updating_medication_frequency));
                        })
        );
    }

    public LiveData<Diagnostic> getSelectedDiagnostic() {
        return selectedDiagnostic;
    }

    public LiveData<List<Medicine>> getMedicines() {
        return medicines;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // DiagnosticInformationViewModel.java

    public void loadDiagnosticByAppointmentId(String documentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("diagnostic")
        .whereEqualTo("appointmentId", documentId)
        .get()
        .addOnSuccessListener(querySnapshot -> {
            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                Diagnostic diagnostic = querySnapshot.getDocuments().get(0).toObject(Diagnostic.class);
                selectedDiagnostic.setValue(diagnostic);


                if (diagnostic != null && diagnostic.getMedicineList() != null) {
                    medicines.setValue(diagnostic.getMedicineList());
                } else {
                    medicines.setValue(new ArrayList<>());
                }
            } else {
                errorMessage.setValue(application.getString(R.string.error_no_diagnostic_for_appointment));
            }
        })
                .addOnFailureListener(e -> errorMessage.setValue(application.getString(R.string.error_loading_diagnostic, e != null ? e.getMessage() : application.getString(R.string.error_unknown))));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}