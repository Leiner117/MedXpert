package com.tec.medxpert.di.AppointmentModule;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.data.repository.AppointmentRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
@Module
@InstallIn(SingletonComponent.class)
public class AppointmentModule {
    @Provides
    @Singleton
    public AppointmentRepository provideAppointmentRepository(FirebaseFirestore firestore) {
        return new AppointmentRepository(firestore);
    }
}
