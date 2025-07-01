package com.example.myapplication.karyawan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends BaseActivity {

    private EditText editNama, editNomorHp, editEmail, editAlamat, editPasswordBaru, editPasswordLama;
    private Button btnSimpan;
    private DatabaseReference mDatabase;
    private String userId;
    private String passwordAsli; // Menyimpan password dari Firebase untuk validasi
    private ImageView imageProfile, btnEditPhoto;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private String imageBase64;  // Menyimpan hasil encode Base64

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityprofile_karyawan);
        setupBottomNavigation(R.id.bottom_profile);

        // Firebase DB
        mDatabase = FirebaseDatabase.getInstance("https://db-convection-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        // Ambil ID user dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi view
        editNama = findViewById(R.id.edit_nama);
        editNomorHp = findViewById(R.id.edit_nomor_hp);
        editEmail = findViewById(R.id.edit_email);
        editAlamat = findViewById(R.id.edit_alamat);
        editPasswordBaru = findViewById(R.id.edit_password_baru);
        editPasswordLama = findViewById(R.id.edit_password_lama);
        btnSimpan = findViewById(R.id.btn_simpan_perubahan);

        // Ambil data user dari Firebase
        loadUserData();

        // Simpan Perubahan
        btnSimpan.setOnClickListener(v -> simpanPerubahan());

        imageProfile = findViewById(R.id.image_profile);
        btnEditPhoto = findViewById(R.id.btn_edit_photo);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Set ImageView langsung dari URI supaya cepat tampil
                            imageProfile.setImageURI(selectedImageUri);

                            // Convert ke Base64 dan simpan ke variabel
                            imageBase64 = encodeImageToBase64(selectedImageUri);

                            // Contoh: simpan ke DB jika ingin
                            // simpanBase64ToFirebase(imageBase64);
                        }
                    }
                });

        btnEditPhoto.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    // Tambah decode method
    private Bitmap decodeBase64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Contoh method simpan ke Firebase jika mau
    private void simpanBase64ToFirebase(String base64) {
        if (base64 == null) return;
        // Contoh path penyimpanan:
        mDatabase.child(userId).child("fotoProfilBase64").setValue(base64);
    }

    private void loadUserData() {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nama = snapshot.child("nama").getValue(String.class);
                    String email = snapshot.child("username").getValue(String.class); // 'username' = email
                    String nomorHp = snapshot.child("nomor_hp").getValue(String.class);
                    String alamat = snapshot.child("alamat").getValue(String.class);
                    String passDb = snapshot.child("password").getValue(String.class);
                    passwordAsli = passDb != null ? passDb : "";

                    editNama.setText(nama != null ? nama : "");
                    editEmail.setText(email != null ? email : "");
                    editNomorHp.setText(nomorHp != null ? nomorHp : "");
                    editAlamat.setText(alamat != null ? alamat : "");
                    // Ambil foto profil
                    String fotoProfilBase64 = snapshot.child("fotoProfilBase64").getValue(String.class);
                    if (fotoProfilBase64 != null && !fotoProfilBase64.isEmpty()) {
                        Bitmap bitmap = decodeBase64ToBitmap(fotoProfilBase64);
                        imageProfile.setImageBitmap(bitmap);
                        imageProfile.setClipToOutline(true);
                        imageBase64 = fotoProfilBase64; // update variabel lokal juga
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void simpanPerubahan() {
        String nama = editNama.getText().toString().trim();
        String nomorHp = editNomorHp.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String alamat = editAlamat.getText().toString().trim();
        String passwordLama = editPasswordLama.getText().toString().trim();
        String passwordBaru = editPasswordBaru.getText().toString().trim();

        if (nama.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan Email wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi password lama jika ingin mengganti password
        if (!passwordBaru.isEmpty()) {
            if (!passwordLama.equals(passwordAsli)) {
                Toast.makeText(this, "Kata sandi lama salah", Toast.LENGTH_SHORT).show();
                return;
            }
            mDatabase.child(userId).child("password").setValue(passwordBaru);
        }

        // Simpan perubahan ke Firebase
        mDatabase.child(userId).child("nama").setValue(nama);
        mDatabase.child(userId).child("username").setValue(email); // username = email
        mDatabase.child(userId).child("nomor_hp").setValue(nomorHp);
        mDatabase.child(userId).child("alamat").setValue(alamat);

        if (!passwordBaru.isEmpty()) {
            mDatabase.child(userId).child("password").setValue(passwordBaru);
        }

        // Simpan foto profil Base64 ke Firebase (jika ada perubahan)
        if (imageBase64 != null) {
            mDatabase.child(userId).child("fotoProfilBase64").setValue(imageBase64);
        }

        Toast.makeText(this, "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show();
    }
}
