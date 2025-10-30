package com.tec.medxpert.data.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.ui.availability.Availability;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Singleton
public class AvailabilityRepository {

    private final FirebaseFirestore firestore;

    @Inject
    public AvailabilityRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public Completable addAvailability(Availability availability) {
        return Completable.create(emitter ->
                firestore.collection("availabilities")
                        .add(availability)
                        .addOnSuccessListener(documentReference -> emitter.onComplete())
                        .addOnFailureListener(emitter::onError)
        );
    }

    public Observable<List<Availability>> getAvailabilityByDate(String selectedDate) {
        return Observable.create(emitter ->
                firestore.collection("availabilities")
                        .whereEqualTo("date", selectedDate)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            List<Availability> list = new ArrayList<>();
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Availability a = doc.toObject(Availability.class);
                                if (a != null) {
                                    a.setId(doc.getId());
                                    list.add(a);
                                }
                            }
                            emitter.onNext(list);
                            emitter.onComplete();
                        })
                        .addOnFailureListener(emitter::onError)
        );
    }

    public Completable deleteAvailability(Availability availability) {
        return Completable.create(emitter -> {
            firestore.collection("appointments")
                    .whereEqualTo("date", availability.getDate())
                    .whereEqualTo("time", availability.getTime())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            emitter.onError(new Exception(
                                    "Cannot delete availability because there are existing appointments for this date and time."
                            ));
                        } else {
                            firestore.collection("availabilities")
                                    .document(availability.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                                    .addOnFailureListener(emitter::onError);
                        }
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    public Completable deletePastAvailabilities() {
        return Completable.create(emitter -> {
            firestore.collection("availabilities")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Completable> deletes = new ArrayList<>();
                    java.util.Date now = new java.util.Date();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("d/M/yyyy hh:mm a", java.util.Locale.US);

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        try {
                            java.util.Date slotDate = sdf.parse(date + " " + time);
                            if (slotDate != null && slotDate.before(now)) {
                            deletes.add(Completable.create(e ->
                                firestore.collection("appointments")
                                    .whereEqualTo("date", date)
                                    .whereEqualTo("time", time)
                                    .get()
                                    .addOnSuccessListener(appointments -> {
                                        if (appointments.isEmpty()) {
                                            doc.getReference().delete()
                                                    .addOnSuccessListener(a -> e.onComplete())
                                                    .addOnFailureListener(e::onError);
                                        } else {
                                            e.onComplete();
                                        }
                                    })
                                    .addOnFailureListener(e::onError)
                            ));
                            }
                        } catch (Exception ignored) {}
                    }
                    if (deletes.isEmpty()) {
                        emitter.onComplete();
                    } else {
                        Completable.merge(deletes).subscribe(emitter::onComplete, emitter::onError);
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }
}
