package com.example.myapplication;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class NightModeOFF extends Application {
    public void onCreate() {
        super.onCreate();

        // Nonaktifkan mode malam untuk seluruh aplikasi
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
