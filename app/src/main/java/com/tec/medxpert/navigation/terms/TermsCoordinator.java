package com.tec.medxpert.navigation.terms;

import android.app.Activity;
import android.content.Intent;

import com.tec.medxpert.ui.terms.TermsAndConditionsActivity;

import javax.inject.Inject;


public class TermsCoordinator implements TermsNavigator  {

    private final Activity activity;

    @Inject
    public TermsCoordinator(Activity activity) {
        this.activity = activity;
    }

    public void navigateToTerms(int requestCode) {
        Intent intent = new Intent(activity, TermsAndConditionsActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }
}
