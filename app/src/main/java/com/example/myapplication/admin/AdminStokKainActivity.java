package com.example.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.admin.model.StokKainModel;
import com.example.myapplication.admin.upload.UploadStokKain;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AdminStokKainActivity extends AppCompatActivity {
    private LinearLayout stokContainer;
    private EditText searchBar;
    private DatabaseReference dbRef;
    private List<StokKainModel> allStok = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stok_kain);

        stokContainer = findViewById(R.id.stokContainer);
        searchBar = findViewById(R.id.searchBar);
        dbRef = FirebaseDatabase.getInstance().getReference("stok_kain");

        loadData();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void afterTextChanged(Editable s){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){
                filterData(s.toString());
            }
        });

        FloatingActionButton fab = findViewById(R.id.fabAddStokKain);
        fab.setOnClickListener(v -> startActivity(new Intent(this, UploadStokKain.class)));
    }

    private void loadData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                allStok.clear();
                stokContainer.removeAllViews();
                for (DataSnapshot ds : snap.getChildren()) {
                    StokKainModel s = ds.getValue(StokKainModel.class);
                    if (s != null) allStok.add(s);
                }
                showData(allStok);
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {
                Toast.makeText(AdminStokKainActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showData(List<StokKainModel> list) {
        stokContainer.removeAllViews();
        for (StokKainModel s : list) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_stok_kain, stokContainer, false);
            TextView txtKode = item.findViewById(R.id.txtKodeId);
            TextView txtMerek = item.findViewById(R.id.txtMerek);
            TextView txtModel = item.findViewById(R.id.txtModelProduk);
            TextView txtJenis = item.findViewById(R.id.txtJenisKain);
            TextView txtPenjahit = item.findViewById(R.id.txtStokPenjahit);
            TextView txtGudang = item.findViewById(R.id.txtStokGudang);
            TextView txtTotal = item.findViewById(R.id.txtTotalStok);
            ImageButton btnEdit = item.findViewById(R.id.btnEdit);
            ImageButton btnDel = item.findViewById(R.id.btnDelete);

            txtKode.setText(s.kodeId);
            txtMerek.setText(s.merek);
            txtModel.setText(s.modelProduk);
            txtJenis.setText(s.jenisKain);
            txtPenjahit.setText(String.format("%.1", s.stokPenjahit));
            txtGudang.setText(String.format("%.1f", s.stokGudang));
            txtTotal.setText(String.format("%.1f", s.totalStok));


            btnEdit.setOnClickListener(v -> {
                Intent i = new Intent(this, UploadStokKain.class);
                i.putExtra("edit_stok", s);
                startActivity(i);
            });
            btnDel.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Hapus Stok")
                        .setMessage("Yakin ingin menghapus stok ini?")
                        .setPositiveButton("Ya", (d,w) -> hapusStok(s))
                        .setNegativeButton("Tidak",null)
                        .show();
            });

            stokContainer.addView(item);
        }
    }

    private void filterData(String kw) {
        List<StokKainModel> f = new ArrayList<>();
        for (StokKainModel s : allStok) {
            if (s.kodeId.toLowerCase().contains(kw.toLowerCase()) ||
                    s.merek.toLowerCase().contains(kw.toLowerCase()) ||
                    s.jenisKain.toLowerCase().contains(kw.toLowerCase()))
                f.add(s);
        }
        showData(f);
    }

    private void hapusStok(StokKainModel s) {
        dbRef.child(s.kodeId).removeValue()
                .addOnSuccessListener(u -> Toast.makeText(this,"Berhasil dihapus",Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this,"Gagal hapus",Toast.LENGTH_SHORT).show());
    }

//    // 🟩 Tambahkan helper ini di bawah metode lainnya
//    private String removeTrailingZero(double d) {
//        if (d == (long) d)
//            return String.format("%d", (long) d);
//        else
//            return String.format("%s", d);
//    }
}
