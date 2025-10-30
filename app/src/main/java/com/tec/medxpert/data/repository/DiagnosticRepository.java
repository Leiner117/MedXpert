package com.tec.medxpert.data.repository;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.data.model.diagnostic.Diagnostic;
import com.tec.medxpert.data.model.diagnostic.Medicine;
import com.tec.medxpert.data.model.diagnostic.VitalSigns;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.List;
import android.util.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class DiagnosticRepository {

    private final FirebaseFirestore firestore;

    @Inject
    public DiagnosticRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public Single<List<Diagnostic>> getDiagnostics() {
        return Single.create(emitter ->
            firestore.collection("diagnostic")
            .get()
            .addOnSuccessListener(querySnapshot -> {

                List<Single<Diagnostic>> diagnosticSingles = new ArrayList<>();

                for (DocumentSnapshot doc : querySnapshot) {

                    String patient_name = doc.getString("patientName");
                    String patient_id = doc.getString("patientId");
                    String weight = doc.getString("weight");
                    String physical_examination = doc.getString("physical_examination");

                    List<Medicine> medicine_List = new ArrayList<>();
                    List<Map<String, Object>> rawMedicineList = (List<Map<String, Object>>) doc.get("medicineList");
                    if (rawMedicineList != null) {
                        for (Map<String, Object> medMap : rawMedicineList) {
                            Medicine med = new Medicine();
                            med.setId((String) medMap.get("id"));
                            med.setName((String) medMap.get("name"));
                            med.setDosage((String) medMap.get("dosage"));
                            med.setDays(((Number) medMap.get("days")).intValue());
                            med.setHours(((Number) medMap.get("hours")).intValue());
                            medicine_List.add(med);
                        }
                    }

                    String consultation_reason = doc.getString("consultation_reason");
                    String subjective_condition = doc.getString("subjective_condition");
                    String objective_condition = doc.getString("objective_condition");
                    String analysis_and_plan = doc.getString("analysis_and_plan");
                    Date date = doc.getDate("updatedAt");

                    Map<String, Object> vitalSignsMap = (Map<String, Object>) doc.get("vitalSigns");
                    VitalSigns vitalSigns = null;
                    if (vitalSignsMap != null) {
                        String heartbeat = (String) vitalSignsMap.get("heartbeat");
                        String temperature = (String) vitalSignsMap.get("temperature");
                        String bloodPressure = (String) vitalSignsMap.get("bloodPressure");
                        String oxygenSaturation = (String) vitalSignsMap.get("oxygenSaturation");

                        if (heartbeat != null && temperature != null && bloodPressure != null && oxygenSaturation != null) {
                            vitalSigns = new VitalSigns(heartbeat, temperature, bloodPressure, oxygenSaturation);
                        } else {
                            Log.d("Firestore", "Null values in vitalSigns for docId: " + doc.getId());
                        }
                    }

                    List<String> image_urls = (List<String>) doc.get("imageUrls");

                    if (patient_name != null && patient_id != null && weight != null && physical_examination != null &&
                            consultation_reason != null && subjective_condition != null && objective_condition != null &&
                            analysis_and_plan != null && date != null && vitalSigns != null) {

                        Timestamp timestamp = new Timestamp(date);

                        Diagnostic diagnostic = new Diagnostic(patient_name, patient_id, weight, physical_examination,
                                medicine_List, consultation_reason, subjective_condition, objective_condition,
                                analysis_and_plan, timestamp, vitalSigns, image_urls);

                        Single<Diagnostic> singleDiagnostic = getPatientIdNumber(patient_id)
                                .doOnSuccess(diagnostic::setIdNumber)
                                .map(id -> diagnostic);

                        diagnosticSingles.add(singleDiagnostic);
                    }
                }

                if (diagnosticSingles.isEmpty()) {
                    emitter.onSuccess(new ArrayList<>());
                    return;
                }


                Single.zip(diagnosticSingles, results -> {
                    List<Diagnostic> diagnostics = new ArrayList<>();
                    for (Object result : results) {
                        diagnostics.add((Diagnostic) result);
                    }
                    return diagnostics;
                }).subscribe(emitter::onSuccess, emitter::onError);

            })
            .addOnFailureListener(e -> {
                emitter.onError(e);
            })
        );
    }


    public Completable updateDiagnostic(String diagnosticId, Diagnostic diagnostic) {
        return Completable.create(emitter -> {
            firestore.collection("diagnostic")
                .document(diagnosticId)
                .set(diagnostic)
                .addOnSuccessListener(aVoid -> {
                    emitter.onComplete();
                })
                .addOnFailureListener(e -> {
                    emitter.onError(e);
                });
        });
    }

    public Single<String> getPatientIdNumber(String patientId) {
        return Single.create(emitter ->
            firestore.collection("patients")
            .document(patientId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String idNumber = documentSnapshot.getString("personalData.idNumber");
                    if (idNumber != null) {
                        emitter.onSuccess(idNumber);
                    } else {
                        emitter.onError(new Exception("The idNumber field does not exist in the document with patientId: " + patientId));
                    }
                } else {
                    emitter.onError(new Exception("Document with patientId not found: " + patientId));
                }
            })
            .addOnFailureListener(emitter::onError)
        );
    }

}
