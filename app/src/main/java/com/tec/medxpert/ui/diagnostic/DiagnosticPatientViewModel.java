package com.tec.medxpert.ui.diagnostic;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Diagnostic;
import com.tec.medxpert.data.repository.DiagnosticRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class DiagnosticPatientViewModel extends AndroidViewModel {

    private Application application;
    private PublishSubject<List<Diagnostic>> diagnosticSubject = PublishSubject.create();
    private PublishSubject<String> filterSubject = PublishSubject.create();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<Diagnostic> diagnosticsList;
    private String currentFilter = "consultation_reason";
    private String currentSearchQuery = "";


    @Inject
    DiagnosticRepository repository;
    @Inject
    public DiagnosticPatientViewModel(Application application) {
        super(application);
        this.application = application;
        diagnosticsList = new ArrayList<>();
    }

    public Observable<List<Diagnostic>> getDiagnostics() {
        return diagnosticSubject.hide();
    }

    public Observable<String> getFilter() {
        return filterSubject.hide();
    }

    public void setDiagnostics(List<Diagnostic> diagnostics) {
        diagnosticsList.clear();

        diagnosticsList.addAll(diagnostics);
        applyCurrentFilter();
    }

    public void applyFilter(String filter) {
        currentFilter = filter;
        filterSubject.onNext(filter);
        applyCurrentFilter();
    }

    public void filterDiagnostics(String query) {
        currentSearchQuery = query;
        applyCurrentFilter();
    }

    public void fetchDiagnosticsFromFirebase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e("DiagnosticsViewModel", application.getString(R.string.error_user_not_logged_in));
            return;
        }

        String currentUserId = currentUser.getUid();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        compositeDisposable.add(
            repository.getDiagnostics()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                diagnostics -> {
                    List<Diagnostic> filteredDiagnostics = new ArrayList<>();

                    for (Diagnostic diagnostic : diagnostics) {
                        String patientId = diagnostic.getPatientId();

                        firestore.collection("patients")
                        .document(patientId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                String userId = documentSnapshot.getString("userId");

                                if (currentUserId.equals(userId)) {
                                    filteredDiagnostics.add(diagnostic);
                                    setDiagnostics(filteredDiagnostics);
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e("DiagnosticsViewModel", "Error fetching patient data: " + e.getMessage()));
                    }
                },
                throwable -> Log.e("DiagnosticsViewModel", application.getString(R.string.error_fetching_diagnostics), throwable)
            )
        );
    }

    private void applyCurrentFilter() {
        List<Diagnostic> filteredList = new ArrayList<>();
        for (Diagnostic diagnostic : diagnosticsList) {
            boolean matchesSearch = false;

            switch (currentFilter) {
                case "consultation_reason":
                    matchesSearch = diagnostic.getConsultation_reason().toLowerCase().contains(currentSearchQuery.toLowerCase());
                    break;
                case "updatedAt":
                    matchesSearch = diagnostic.getUpdatedAt().toString().contains(currentSearchQuery.toLowerCase());
                    break;
            }
            if (matchesSearch) {
                filteredList.add(diagnostic);
            }
        }
        diagnosticSubject.onNext(filteredList);
    }

    public void filterByDateRange(long startMillis, long endMillis) {

        List<Diagnostic> filteredList = new ArrayList<>();

        for (Diagnostic diagnostic : diagnosticsList) {
            com.google.firebase.Timestamp timestamp = diagnostic.getUpdatedAt();
            if (timestamp != null) {
                Date diagnosticDate = timestamp.toDate();
                if (diagnosticDate.getTime() >= startMillis && diagnosticDate.getTime() <= endMillis) {
                    filteredList.add(diagnostic);
                }
            }
        }

        diagnosticSubject.onNext(filteredList);
    }

    public void onDestroy() {
        compositeDisposable.clear();
    }
}