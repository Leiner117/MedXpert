package com.tec.medxpert.di.ProfileModule;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.auth.MockAuthProvider;
import com.tec.medxpert.data.repository.profile.ChangeHistoryRepository;
import com.tec.medxpert.data.repository.profile.DoctorRepository;
import com.tec.medxpert.data.repository.profile.PatientRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt module for providing repository dependencies
 */
@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    @Provides
    @Singleton
    public ChangeHistoryRepository provideChangeHistoryRepository(FirebaseFirestore firestore) {
        return new ChangeHistoryRepository(firestore);
    }

    @Provides
    @Singleton
    public PatientRepository providePatientRepository(
            FirebaseFirestore firestore,
            ChangeHistoryRepository changeHistoryRepository,
            MockAuthProvider mockAuthProvider) {
        return new PatientRepository(firestore, changeHistoryRepository, mockAuthProvider);
    }

    @Provides
    @Singleton
    public DoctorRepository provideDoctorRepository(FirebaseFirestore firestore) {
        return new DoctorRepository(firestore);
    }
}