package com.example.myapplication.karyawan;

import android.os.Bundle;

import com.example.myapplication.R;

public class RiwayatActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityriwayat_karyawan);
        setupBottomNavigation(R.id.bottom_search);
    }
}
