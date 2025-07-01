package com.example.myapplication.admin.upload;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EditPenjahitActivity extends AppCompatActivity {

    private EditText editIdKaryawan, editNama, editNoHp, editEmail, editAlamat;
    private ImageView imagePenjahit;
    private Button btnPilihGambar, btnSimpan, btnKembali;

    private String userId;
    private String imageBase64 = null;
    private DatabaseReference database;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    imagePenjahit.setImageURI(imageUri);
                    imageBase64 = encodeImageToBase64(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_penjahit);

        // Inisialisasi View
        editIdKaryawan = findViewById(R.id.edit_id_karyawan);
        editNama = findViewById(R.id.edit_nama_penjahit);
        editNoHp = findViewById(R.id.edit_no_hp_penjahit);
        editEmail = findViewById(R.id.edit_email_penjahit);
        editAlamat = findViewById(R.id.edit_alamat_penjahit);
        imagePenjahit = findViewById(R.id.image_penjahit);
        btnPilihGambar = findViewById(R.id.btnPilihGambar);
        btnSimpan = findViewById(R.id.btn_simpan_penjahit);
        btnKembali = findViewById(R.id.btn_kembali);

        // Firebase Reference
        database = FirebaseDatabase.getInstance("https://db-convection-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        // Ambil data dari intent
        userId = getIntent().getStringExtra("userId");
        String nama = getIntent().getStringExtra("nama");
        String nomorHp = getIntent().getStringExtra("nomorHp");
        String email = getIntent().getStringExtra("email");
        String alamat = getIntent().getStringExtra("alamat");
        String fotoBase64 = getIntent().getStringExtra("fotoBase64");

        // Isi data
        editIdKaryawan.setText(userId);
        editIdKaryawan.setEnabled(false); // ID tidak bisa diubah
        editNama.setText(nama);
        editNoHp.setText(nomorHp);
        editEmail.setText(email);
        editEmail.setEnabled(false);
        editAlamat.setText(alamat);

        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            byte[] decodedBytes = Base64.decode(fotoBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imagePenjahit.setImageBitmap(bitmap);
            imageBase64 = fotoBase64;
        }

        // Pilih gambar baru
        btnPilihGambar.setOnClickListener(v -> openImagePicker());

        // Simpan perubahan
        btnSimpan.setOnClickListener(v -> {
            String updatedNama = editNama.getText().toString().trim();
            String updatedNoHp = editNoHp.getText().toString().trim();
//            String updatedEmail = editEmail.getText().toString().trim();
            String updatedAlamat = editAlamat.getText().toString().trim();

            if (updatedNama.isEmpty()) {
                Toast.makeText(this, "Nama wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            database.child(userId).child("nama").setValue(updatedNama);
            database.child(userId).child("nomor_hp").setValue(updatedNoHp);
//            database.child(userId).child("username").setValue(updatedEmail);
            database.child(userId).child("alamat").setValue(updatedAlamat);

            if (imageBase64 != null) {
                database.child(userId).child("fotoProfilBase64").setValue(imageBase64);
            }

            Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Tombol kembali
        btnKembali.setOnClickListener(v -> finish());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
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
            return null;
        }
    }
}
