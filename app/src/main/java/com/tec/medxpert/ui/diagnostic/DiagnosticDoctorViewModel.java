package com.tec.medxpert.ui.diagnostic;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Diagnostic;
import com.tec.medxpert.data.repository.DiagnosticRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class DiagnosticDoctorViewModel extends AndroidViewModel {

    private Application application;
    private PublishSubject<List<Diagnostic>> diagnosticSubject = PublishSubject.create();
    private PublishSubject<String> filterSubject = PublishSubject.create();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<Diagnostic> diagnosticsList;
    private String currentFilter = "patientName";
    private String currentSearchQuery = "";


    @Inject
    DiagnosticRepository repository;
    @Inject
    public DiagnosticDoctorViewModel(Application application) {
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

    public void setDiagnostics(List<Diagnostic> diagnostic2s) {
        diagnosticsList.clear();

        diagnostic2s.sort((d1, d2) -> d1.getPatientName().compareToIgnoreCase(d2.getPatientName()));

        diagnosticsList.addAll(diagnostic2s);
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
        compositeDisposable.add(
            repository.getDiagnostics()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::setDiagnostics,
                throwable -> Log.e("DiagnosticsViewModel", application.getString(R.string.error_fetching_diagnostics), throwable)
            )
        );
    }

    private void applyCurrentFilter() {
        List<Diagnostic> filteredList = new ArrayList<>();
        for (Diagnostic diagnostic : diagnosticsList) {
            boolean matchesSearch = false;

            switch (currentFilter) {
                case "patientName":
                    matchesSearch = diagnostic.getPatientName().toLowerCase().contains(currentSearchQuery.toLowerCase());
                    break;
                case "idNumber":
                    matchesSearch = diagnostic.getIdNumber().toLowerCase().contains(currentSearchQuery.toLowerCase());
                    break;
                case "updatedAt":
                    if (diagnostic.getUpdatedAt() != null) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                        String formattedDate = formatter.format(diagnostic.getUpdatedAt().toDate()).toLowerCase();
                        matchesSearch = formattedDate.contains(currentSearchQuery.toLowerCase());
                    }
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