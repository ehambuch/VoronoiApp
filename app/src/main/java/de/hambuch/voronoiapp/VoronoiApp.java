package de.hambuch.voronoiapp;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class VoronoiApp extends Application {
    public static final String APPNAME = "VoronoiApp";

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this); // Material 3 Design
    }
}
