package com.example.myapplication.karyawan;

import android.os.Bundle;

import com.example.myapplication.R;

public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitysettings_karyawan);
        setupBottomNavigation(R.id.bottom_settings);
    }
}
