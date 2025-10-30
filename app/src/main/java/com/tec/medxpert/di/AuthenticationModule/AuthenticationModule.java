package com.tec.medxpert.di.AuthenticationModule;

import android.app.Activity;

import com.tec.medxpert.navigation.authentication.AuthenticationCoordinator;
import com.tec.medxpert.navigation.authentication.AuthenticationNavigator;
import com.tec.medxpert.navigation.authentication.AuthenticationCoordinator;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.scopes.ActivityScoped;

@Module
@InstallIn(ActivityComponent.class)
public class AuthenticationModule {

    @Provides
    @ActivityScoped
    public AuthenticationNavigator provideNavigator(Activity activity) {
        return new AuthenticationCoordinator(activity);
    }
}
