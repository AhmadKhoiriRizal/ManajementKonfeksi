package com.example.myapplication.admin.upload;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.admin.AdminMerekActivity;
import com.example.myapplication.admin.model.MerekModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadMerek extends AppCompatActivity {

    EditText inputKodeId, inputNamaMerek, inputLogo;
    ImageView imagePreview;
    Button btnPilihGambar, btnUploadData, btnKembali;

    Bitmap selectedBitmap = null;
    final int PICK_IMAGE_REQUEST = 100;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_upload_merek); // Pastikan nama file XML sesuai

        inputKodeId = findViewById(R.id.inputKodeId);
        inputNamaMerek = findViewById(R.id.inputNamaMerek);
        inputLogo = findViewById(R.id.inputLogo);
        imagePreview = findViewById(R.id.imagePreview);
        btnPilihGambar = findViewById(R.id.btnPilihGambar);
        btnUploadData = findViewById(R.id.btnUploadData);
        btnKembali = findViewById(R.id.kembali);

        databaseReference = FirebaseDatabase.getInstance().getReference("merek");

        btnPilihGambar.setOnClickListener(v -> openImagePicker());
        btnUploadData.setOnClickListener(v -> uploadData());

        MerekModel editMerek = (MerekModel) getIntent().getSerializableExtra("edit_merek");

        if (editMerek != null) {
            // Set input field dengan data merek yang diedit
            inputKodeId.setText(editMerek.kodeId);
            EditText inputNamaMerek = findViewById(R.id.inputNamaMerek);
            EditText inputLogo = findViewById(R.id.inputLogo);
            ImageView imagePreview = findViewById(R.id.imagePreview);

            inputKodeId.setEnabled(false);
            inputNamaMerek.setText(editMerek.namaMerek);
            inputLogo.setText(editMerek.logoUrl);

            // Jika ada gambar base64, tampilkan
            if (editMerek.gambarBase64 != null && !editMerek.gambarBase64.isEmpty()) {
                byte[] imageBytes = Base64.decode(editMerek.gambarBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imagePreview.setImageBitmap(bitmap);
            } else if (editMerek.logoUrl != null && !editMerek.logoUrl.isEmpty()) {
                Glide.with(this).load(editMerek.logoUrl).into(imagePreview);
            }
        }


        btnKembali.setOnClickListener(v -> {
            // Kembali ke AdminMerekActivity
            Intent intent = new Intent(UploadMerek.this, com.example.myapplication.admin.AdminMerekActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imagePreview.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadData() {
        String kodeId = inputKodeId.getText().toString().trim();
        String namaMerek = inputNamaMerek.getText().toString().trim();
        String urlLogo = inputLogo.getText().toString().trim();
        String encodedImage = "";

        if (kodeId.isEmpty() || namaMerek.isEmpty()) {
            Toast.makeText(this, "Kode ID dan Nama Merek wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedBitmap != null) {
            // Resize gambar sebelum di-encode
            Bitmap resized = Bitmap.createScaledBitmap(selectedBitmap, 800, 600, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 80, baos); // Bisa kurangi kualitas jadi 80
            byte[] imageBytes = baos.toByteArray();
            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }

//        if (selectedBitmap != null) {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] imageBytes = baos.toByteArray();
//            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
//        }

        Map<String, Object> merekMap = new HashMap<>();
        merekMap.put("kodeId", kodeId);
        merekMap.put("namaMerek", namaMerek);
        merekMap.put("logoUrl", urlLogo); // jika user mengisi URL
        merekMap.put("gambarBase64", encodedImage); // optional, jika bitmap diupload

        databaseReference.child(kodeId).setValue(merekMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Data berhasil diupload", Toast.LENGTH_SHORT).show();
                    finish(); // close activity
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal upload: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

