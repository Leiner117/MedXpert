package com.tec.medxpert.di.ProfileModule;

import com.tec.medxpert.navigation.profile.DoctorCoordinator;
import com.tec.medxpert.navigation.profile.IDoctorCoordinator;
import com.tec.medxpert.navigation.profile.IProfileCoordinator;
import com.tec.medxpert.navigation.profile.ProfileCoordinator;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public abstract class ProfileModule {

    @Binds
    @Singleton
    public abstract IProfileCoordinator bindProfileCoordinator(ProfileCoordinator coordinator);

    @Binds
    @Singleton
    public abstract IDoctorCoordinator bindDoctorCoordinator(DoctorCoordinator coordinator);
}