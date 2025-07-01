package com.example.myapplication.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.admin.model.ModelJenisKain;
import com.example.myapplication.admin.upload.UploadJenisKain;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminJenisKainActivity extends AppCompatActivity {

    private LinearLayout merekContainer;
    private EditText searchBar;
    private DatabaseReference databaseRef;
    private List<ModelJenisKain> allJenisKain = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_jenis_kain);

        merekContainer = findViewById(R.id.merekContainer);
        searchBar = findViewById(R.id.searchBar);
        databaseRef = FirebaseDatabase.getInstance().getReference("jenis_kain");

        loadData();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }
        });

        FloatingActionButton fab = findViewById(R.id.fabAddJenisKain);
        fab.setOnClickListener(v -> startActivity(new Intent(this, UploadJenisKain.class)));
    }

    private void loadData() {
        databaseRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allJenisKain.clear();
                merekContainer.removeAllViews();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelJenisKain m = ds.getValue(ModelJenisKain.class);
                    if (m != null) allJenisKain.add(m);
                }
                showData(allJenisKain);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminJenisKainActivity.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showData(List<ModelJenisKain> list) {
        merekContainer.removeAllViews();
        for (ModelJenisKain m : list) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_jenis_kain, merekContainer, false);
            TextView tvJenis = item.findViewById(R.id.txtJenisKain);
            TextView tvKode = item.findViewById(R.id.txtKodeId);
            ImageView ivLogo = item.findViewById(R.id.imgLogo);
            ImageButton btnEdit = item.findViewById(R.id.btnEdit);
            ImageButton btnDelete = item.findViewById(R.id.btnDelete);

            tvJenis.setText(m.jenisKain);
            tvKode.setText("Kode: " + m.kodeId);
            if (m.gambarBase64 != null && !m.gambarBase64.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.decode(m.gambarBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    ivLogo.setImageBitmap(bitmap);
                } catch (Exception e) {
                    ivLogo.setImageResource(R.drawable.logo_app);
                }
            } else if (m.logoUrl != null && !m.logoUrl.isEmpty()) {
                Glide.with(this).load(m.logoUrl).into(ivLogo);
            } else {
                ivLogo.setImageResource(R.drawable.logo_app);
            }

            btnEdit.setOnClickListener(v -> {
                Intent i = new Intent(this, UploadJenisKain.class);
                i.putExtra("edit_model", m);
                startActivity(i);
            });

            btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Hapus Jenis Kain")
                    .setMessage("Yakin ingin menghapus jenis kain ini?")
                    .setPositiveButton("Ya", (d, w) -> deleteJenisKain(m))
                    .setNegativeButton("Tidak", null)
                    .show());

            merekContainer.addView(item);
        }
    }

    private void filterData(String keyword) {
        List<ModelJenisKain> filtered = new ArrayList<>();
        for (ModelJenisKain m : allJenisKain) {
            if ((m.jenisKain != null && m.jenisKain.toLowerCase().contains(keyword.toLowerCase()))
                    || (m.kodeId != null && m.kodeId.toLowerCase().contains(keyword.toLowerCase()))) {
                filtered.add(m);
            }
        }
        showData(filtered);
    }

    private void deleteJenisKain(ModelJenisKain m) {
        databaseRef.child(m.kodeId).removeValue()
                .addOnSuccessListener(u -> Toast.makeText(this, "Data dihapus", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT).show());
    }
}
