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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.admin.model.ModelPenjahit;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminPenjahitActivity extends AppCompatActivity {

    private LinearLayout merekContainer;
    private EditText searchBar;
    private DatabaseReference databaseReference;

    private List<ModelPenjahit> allPenjahitList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_penjahit);

        merekContainer = findViewById(R.id.merekContainer);
        searchBar = findViewById(R.id.searchBar);

        databaseReference = FirebaseDatabase.getInstance("https://db-convection-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        loadData();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }
        });
    }

    private void loadData() {
        databaseReference.orderByChild("role").equalTo("karyawan")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        allPenjahitList.clear();
                        merekContainer.removeAllViews();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String userId = ds.getKey();
                            String nama = ds.child("nama").getValue(String.class);
                            String nomorHp = ds.child("nomor_hp").getValue(String.class);
                            String email = ds.child("username").getValue(String.class);
                            String alamat = ds.child("alamat").getValue(String.class);
                            String fotoBase64 = ds.child("fotoProfilBase64").getValue(String.class);

                            ModelPenjahit m = new ModelPenjahit(userId, nama, nomorHp, email, alamat, fotoBase64);
                            allPenjahitList.add(m);
                        }
                        showData(allPenjahitList);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AdminPenjahitActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showData(List<ModelPenjahit> list) {
        merekContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (ModelPenjahit m : list) {
            View itemView = inflater.inflate(R.layout.item_penjahit, merekContainer, false);

            ((TextView) itemView.findViewById(R.id.txtNamaPenjahit)).setText(m.nama);
            ((TextView) itemView.findViewById(R.id.txtKodeId)).setText("ID: " + m.userId);
            ((TextView) itemView.findViewById(R.id.txtNoHPPenjahit)).setText(m.nomorHp != null ? m.nomorHp : "-");
            ((TextView) itemView.findViewById(R.id.txtEmailPenjahit)).setText(m.email != null ? m.email : "-");
            ((TextView) itemView.findViewById(R.id.txtAlamat)).setText(m.alamat != null ? m.alamat : "-");

            ImageView imageView = itemView.findViewById(R.id.fotoprofil);
            if (m.fotoBase64 != null && !m.fotoBase64.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.decode(m.fotoBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    imageView.setImageResource(R.drawable.logo_app);
                }
            }

            ImageButton btnEdit = itemView.findViewById(R.id.btnEdit);
            ImageButton btnDelete = itemView.findViewById(R.id.btnDelete);

            btnEdit.setOnClickListener(v -> {
                Intent i = new Intent(this, com.example.myapplication.admin.upload.EditPenjahitActivity.class);
                i.putExtra("userId", m.userId);
                i.putExtra("nama", m.nama);
                i.putExtra("nomorHp", m.nomorHp);
                i.putExtra("email", m.email);
                i.putExtra("alamat", m.alamat);
                i.putExtra("fotoBase64", m.fotoBase64);
                startActivity(i);
            });

            btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Hapus Data Penjahit")
                    .setMessage("Yakin ingin menghapus penjahit ini?")
                    .setPositiveButton("Ya", (d, w) -> deletePenjahit(m))
                    .setNegativeButton("Tidak", null)
                    .show());

            merekContainer.addView(itemView);
        }
    }

    private void filterData(String keyword) {
        List<ModelPenjahit> filtered = new ArrayList<>();
        for (ModelPenjahit m : allPenjahitList) {
            if ((m.nama != null && m.nama.toLowerCase().contains(keyword.toLowerCase()))
                    || (m.userId != null && m.userId.toLowerCase().contains(keyword.toLowerCase()))) {
                filtered.add(m);
            }
        }
        showData(filtered);
    }

    private void deletePenjahit(ModelPenjahit m) {
        databaseReference.child(m.userId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Penjahit berhasil dihapus", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                );
    }
}
