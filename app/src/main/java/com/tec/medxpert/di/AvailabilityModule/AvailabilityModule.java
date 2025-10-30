package com.tec.medxpert.di.AvailabilityModule;

import android.content.Context;

import com.tec.medxpert.navigation.availability.AppAvailabilityCoordinator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AvailabilityModule {

    @Provides
    @Singleton
    public AppAvailabilityCoordinator
    provideAppAvailabilityCoordinator(@ApplicationContext Context context) {
        return new AppAvailabilityCoordinator(context);
    }
}