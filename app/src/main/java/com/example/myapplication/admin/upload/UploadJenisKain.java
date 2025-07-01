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

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.admin.model.ModelJenisKain;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadJenisKain extends AppCompatActivity {

    private EditText inputKodeId, inputJenisKain, inputLogo;
    private ImageView imagePreview;
    private Button btnPilihGambar, btnUploadData, btnKembali;

    private Bitmap selectedBitmap = null;
    private static final int PICK_IMAGE = 101;
    private DatabaseReference dbRef;
    private ModelJenisKain editModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_upload_jenis_kain);

        inputKodeId = findViewById(R.id.inputKodeId);
        inputJenisKain = findViewById(R.id.inputJenisKain);
        inputLogo = findViewById(R.id.inputLogo);
        imagePreview = findViewById(R.id.imagePreview);
        btnPilihGambar = findViewById(R.id.btnPilihGambar);
        btnUploadData = findViewById(R.id.btnUploadData);
        btnKembali = findViewById(R.id.kembali);

        dbRef = FirebaseDatabase.getInstance().getReference("jenis_kain");

        btnPilihGambar.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, PICK_IMAGE);
        });

        editModel = (ModelJenisKain) getIntent().getSerializableExtra("edit_model");
        if (editModel != null) setupEditMode();

        btnUploadData.setOnClickListener(v -> uploadData());
        btnKembali.setOnClickListener(v -> finish());
    }

    private void setupEditMode() {
        inputKodeId.setText(editModel.kodeId);
        inputJenisKain.setText(editModel.jenisKain);
        inputLogo.setText(editModel.logoUrl);
        inputKodeId.setEnabled(false);
        if (editModel.gambarBase64 != null && !editModel.gambarBase64.isEmpty()) {
            byte[] img = Base64.decode(editModel.gambarBase64, Base64.DEFAULT);
            Bitmap bm = BitmapFactory.decodeByteArray(img, 0, img.length);
            imagePreview.setImageBitmap(bm);
        } else if (editModel.logoUrl != null && !editModel.logoUrl.isEmpty()) {
            Glide.with(this).load(editModel.logoUrl).into(imagePreview);
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (reqCode == PICK_IMAGE && resCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imagePreview.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadData() {
        String kode = inputKodeId.getText().toString().trim();
        String jenis = inputJenisKain.getText().toString().trim();
        String url = inputLogo.getText().toString().trim();

        if (kode.isEmpty() || jenis.isEmpty()) {
            Toast.makeText(this, "Isi lengkap Kode ID & Jenis Kain", Toast.LENGTH_SHORT).show();
            return;
        }

        String encodedImage = "";
        if (selectedBitmap != null) {
            Bitmap resized = Bitmap.createScaledBitmap(selectedBitmap, 800, 800, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } else if (editModel != null) {
            encodedImage = editModel.gambarBase64;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("kodeId", kode);
        map.put("jenisKain", jenis);
        map.put("logoUrl", url);
        map.put("gambarBase64", encodedImage);

        dbRef.child(kode).setValue(map)
                .addOnSuccessListener(u -> {
                    Toast.makeText(this, "Berhasil disimpan", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
