package com.tec.medxpert.navigation.availability;

import android.content.Context;
import android.content.Intent;

import com.tec.medxpert.ui.availability.AddAvailabilityActivity;

public class AppAvailabilityCoordinator implements AvailabilityCoordinator {

    private final Context context;
    public AppAvailabilityCoordinator(Context context) {
        this.context = context;
    }
    @Override
    public void navigateToAddAvailability() {
        Intent intent = new Intent(context, AddAvailabilityActivity.class);
        context.startActivity(intent);
    }

}
