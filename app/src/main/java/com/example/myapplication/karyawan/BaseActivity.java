package com.example.myapplication.karyawan;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Login;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNavigation(int selectedItemId) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(selectedItemId);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == selectedItemId) {
                return true; // sudah di halaman ini
            }

            Intent intent = null;

            if (itemId == R.id.bottom_home) {
                intent = new Intent(this, com.example.myapplication.karyawan.MenuActivity.class);
            } else if (itemId == R.id.bottom_search) {
                intent = new Intent(this, RiwayatActivity.class);
            } else if (itemId == R.id.bottom_settings) {
                intent = new Intent(this, com.example.myapplication.karyawan.SettingsActivity.class);
            } else if (itemId == R.id.bottom_profile) {
                intent = new Intent(this, com.example.myapplication.karyawan.ProfileActivity.class);
            } else if (itemId == R.id.bottom_logout) {
                logout();
                return true;
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return false;
        });
    }
    private void logout() {
        Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Login.class); // Ganti ke Login.class
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Optional: bersihkan backstack
        startActivity(intent);
        finish();
    }

}
