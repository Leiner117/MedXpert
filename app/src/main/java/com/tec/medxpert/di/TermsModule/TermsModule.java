package com.tec.medxpert.di.TermsModule;

import android.app.Activity;

import com.tec.medxpert.navigation.terms.TermsCoordinator;
import com.tec.medxpert.navigation.terms.TermsNavigator;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;

@Module
@InstallIn(ActivityComponent.class)
public class TermsModule {

    @Provides
    TermsNavigator provideTermsNavigator(Activity activity) {
        return new TermsCoordinator(activity);
    }
}

