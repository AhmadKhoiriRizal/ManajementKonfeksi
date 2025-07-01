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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.admin.model.ModelProdukModel;
import com.example.myapplication.admin.upload.UploadModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminModelProdukActivity extends AppCompatActivity {

    private LinearLayout modelContainer;
    private EditText searchBar;
    private DatabaseReference databaseRef;
    private List<ModelProdukModel> allModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_model_produk);

        modelContainer = findViewById(R.id.modelContainer);
        searchBar = findViewById(R.id.searchBar);
        databaseRef = FirebaseDatabase.getInstance().getReference("model_produk");

        loadData();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int bf, int ct) {
                filterData(s.toString());
            }
        });

        FloatingActionButton fab = findViewById(R.id.fabAddModel);
        fab.setOnClickListener(v -> startActivity(new Intent(this, UploadModel.class)));
    }

    private void loadData() {
        databaseRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allModels.clear();
                modelContainer.removeAllViews();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelProdukModel m = ds.getValue(ModelProdukModel.class);
                    if (m != null) allModels.add(m);
                }
                showData(allModels);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminModelProdukActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showData(List<ModelProdukModel> list) {
        modelContainer.removeAllViews();
        for (ModelProdukModel m : list) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_model, modelContainer, false);
            TextView tvName = item.findViewById(R.id.txtNamaModel);
            TextView tvKode = item.findViewById(R.id.txtKodeId);
            ImageView ivLogo = item.findViewById(R.id.imgLogo);
            ImageButton btnEdit = item.findViewById(R.id.btnEdit);
            ImageButton btnDelete = item.findViewById(R.id.btnDelete);

            tvName.setText(m.namaModel);
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
                Intent i = new Intent(this, UploadModel.class);
                i.putExtra("edit_model", m);
                startActivity(i);
            });

            btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Hapus Model")
                    .setMessage("Yakin ingin hapus model ini?")
                    .setPositiveButton("Ya", (d, w) -> deleteModel(m))
                    .setNegativeButton("Tidak", null)
                    .show());

            modelContainer.addView(item);
        }
    }

    private void filterData(String kw) {
        List<ModelProdukModel> filtered = new ArrayList<>();
        for (ModelProdukModel m : allModels) {
            if (m.namaModel != null && m.namaModel.toLowerCase().contains(kw.toLowerCase())
                    || m.kodeId != null && m.kodeId.toLowerCase().contains(kw.toLowerCase())) {
                filtered.add(m);
            }
        }
        showData(filtered);
    }

    private void deleteModel(ModelProdukModel m) {
        databaseRef.child(m.kodeId).removeValue()
                .addOnSuccessListener(u -> Toast.makeText(this, "Model dihapus", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal hapus", Toast.LENGTH_SHORT).show());
    }
}
