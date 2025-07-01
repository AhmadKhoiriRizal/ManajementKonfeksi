package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.admin.AdminBerandaActivity;
import com.example.myapplication.admin.AdminLaporanProduksiActivity;
import com.example.myapplication.admin.AdminMerekActivity;
import com.example.myapplication.admin.AdminModelProdukActivity;
import com.example.myapplication.admin.AdminJenisKainActivity;
import com.example.myapplication.admin.AdminPengaturanActivity;
import com.example.myapplication.admin.AdminPenjahitActivity;
import com.example.myapplication.admin.AdminStokKainActivity;
import com.example.myapplication.admin.AdminTugasProduksiActivity;
import com.google.android.material.card.MaterialCardView;

public class MenuActivity extends AppCompatActivity {

    MaterialCardView  beranda, stokKain, pemotonganKain, modelProduk, merek;
    MaterialCardView  penjahit, tugasProduksi, laporanProduksi, pengaturan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Inisialisasi semua ImageView dari layout
        beranda = findViewById(R.id.beranda);
        stokKain = findViewById(R.id.stok_kain);
        pemotonganKain = findViewById(R.id.pemotongan_kain);
        modelProduk = findViewById(R.id.model_produk);
        merek = findViewById(R.id.merek);
        penjahit = findViewById(R.id.penjahit);
        tugasProduksi = findViewById(R.id.tugas_produksi);
        laporanProduksi = findViewById(R.id.laporan_produksi);
        pengaturan = findViewById(R.id.pengaturan);
        MaterialCardView logout = findViewById(R.id.nav_logout); // ✅ BENAR


        // Contoh pindah ke halaman admin dari setiap tombol
        beranda.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, AdminBerandaActivity.class));
        });

        stokKain.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, AdminStokKainActivity.class));
        });

        pemotonganKain.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, AdminJenisKainActivity.class));
        });

        modelProduk.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, AdminModelProdukActivity.class));
        });

        merek.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, AdminMerekActivity.class));
        });

        penjahit.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, AdminPenjahitActivity.class));
        });

        tugasProduksi.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, AdminTugasProduksiActivity.class));
        });

        laporanProduksi.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, AdminLaporanProduksiActivity.class));
        });

        pengaturan.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, AdminPengaturanActivity.class));
        });

        logout.setOnClickListener(view -> {
            // Hapus semua data yang tersimpan di SharedPreferences
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();

            // Buat Intent untuk kembali ke halaman login
            Intent intent = new Intent(MenuActivity.this, Login.class);

            // Hapus seluruh aktivitas sebelumnya agar user tidak bisa kembali dengan tombol back
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Jalankan aktivitas baru
            startActivity(intent);

            // Opsional: hentikan aktivitas saat ini
            finish();

            // Opsional: tampilkan toast sebagai notifikasi logout
            Toast.makeText(MenuActivity.this, "Logout berhasil!", Toast.LENGTH_SHORT).show();
        });

    }
}
