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
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.admin.upload.UploadMerek;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminMerekActivity extends AppCompatActivity {

    private LinearLayout merekContainer;
    private DatabaseReference databaseRef;
    private EditText searchBar;
    private List<com.example.myapplication.admin.model.MerekModel> allMerek = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_merek);

        merekContainer = findViewById(R.id.merekContainer);
        searchBar = findViewById(R.id.searchBar);
        databaseRef = FirebaseDatabase.getInstance().getReference("merek");

        loadData();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }
        });

        FloatingActionButton fab = findViewById(R.id.fabAddMerek);
        fab.setOnClickListener(v -> startActivity(new Intent(this, com.example.myapplication.admin.upload.UploadMerek.class)));
    }

    private void loadData() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allMerek.clear();
                merekContainer.removeAllViews();

                for (DataSnapshot data : snapshot.getChildren()) {
                    com.example.myapplication.admin.model.MerekModel merek = data.getValue(com.example.myapplication.admin.model.MerekModel.class);
                    if (merek != null) {
                        allMerek.add(merek);
                    }
                }

                showData(allMerek);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminMerekActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showData(List<com.example.myapplication.admin.model.MerekModel> dataList) {
        merekContainer.removeAllViews();

        for (com.example.myapplication.admin.model.MerekModel merek : dataList) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_merek, merekContainer, false);

            TextView txtNama = itemView.findViewById(R.id.txtNamaMerek);
            TextView txtKode = itemView.findViewById(R.id.txtKodeId);
            ImageView imgLogo = itemView.findViewById(R.id.imgLogo);

            ImageButton btnEdit = itemView.findViewById(R.id.btnEdit);
            ImageButton btnDelete = itemView.findViewById(R.id.btnDelete);

            txtNama.setText(merek.namaMerek);
            txtKode.setText("Kode: " + merek.kodeId);

            if (merek.gambarBase64 != null && !merek.gambarBase64.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.decode(merek.gambarBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imgLogo.setImageBitmap(bitmap);
                } catch (Exception e) {
                    imgLogo.setImageResource(R.drawable.logo_app);
                }
            } else if (merek.logoUrl != null && !merek.logoUrl.isEmpty()) {
                Glide.with(this).load(merek.logoUrl).into(imgLogo);
            } else {
                imgLogo.setImageResource(R.drawable.logo_app);
            }

            // Event tombol Edit
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(AdminMerekActivity.this, com.example.myapplication.admin.upload.UploadMerek.class);
                // Kirim data yang akan diedit ke UploadMerek (misal via extras)
                intent.putExtra("edit_merek", merek);
                startActivity(intent);
            });

            // Event tombol Delete
            btnDelete.setOnClickListener(v -> {
                // Konfirmasi hapus (misal dialog)
                new androidx.appcompat.app.AlertDialog.Builder(AdminMerekActivity.this)
                        .setTitle("Hapus Merek")
                        .setMessage("Apakah Anda yakin ingin menghapus merek ini?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            hapusMerek(merek);
                        })
                        .setNegativeButton("Tidak", null)
                        .show();
            });

            merekContainer.addView(itemView);
        }
    }

    private void filterData(String keyword) {
        List<com.example.myapplication.admin.model.MerekModel> filtered = new ArrayList<>();
        for (com.example.myapplication.admin.model.MerekModel m : allMerek) {
            if (m.namaMerek != null && m.namaMerek.toLowerCase().contains(keyword.toLowerCase()) ||
                    m.kodeId != null && m.kodeId.toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(m);
            }
        }
        showData(filtered);
    }

    private void hapusMerek(com.example.myapplication.admin.model.MerekModel merek) {
        // Misal key = kodeId, atau kalau kamu punya key node Firebase, pakai itu.
        // Contoh hapus berdasarkan kodeId:
        databaseRef.orderByChild("kodeId").equalTo(merek.kodeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            data.getRef().removeValue();
                        }
                        Toast.makeText(AdminMerekActivity.this, "Merek berhasil dihapus", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminMerekActivity.this, "Gagal menghapus merek", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
