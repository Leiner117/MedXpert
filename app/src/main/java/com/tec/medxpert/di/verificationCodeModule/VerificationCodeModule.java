package com.tec.medxpert.di.verificationCodeModule;

import android.app.Activity;
import com.tec.medxpert.navigation.codeSecurity.VerificationCodeCoordinator;
import com.tec.medxpert.navigation.codeSecurity.VerificationCodeNavigator;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.scopes.ActivityScoped;


@Module
@InstallIn(ActivityComponent.class)
public class VerificationCodeModule {

    @Provides
    @ActivityScoped
    public VerificationCodeNavigator provideNavigator(Activity activity) {
        return new VerificationCodeCoordinator(activity);
    }
}

