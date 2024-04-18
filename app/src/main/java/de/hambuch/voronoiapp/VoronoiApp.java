package de.hambuch.voronoiapp;

import android.app.Application;

import androidx.annotation.Nullable;

import com.google.android.material.color.DynamicColors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoronoiApp extends Application {
    public static final String APPNAME = "VoronoiApp";

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this); // Material 3 Design
    }

    protected void executeBackground(@Nullable Runnable runnable) {
        if(runnable != null)
            executorService.execute(runnable);
    }
}
