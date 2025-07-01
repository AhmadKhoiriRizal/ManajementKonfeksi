package com.example.myapplication.admin.upload;

import static java.lang.Double.parseDouble;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.admin.model.StokKainModel;
import com.example.myapplication.admin.AdminStokKainActivity;
import com.google.firebase.database.*;

import java.util.*;

public class UploadStokKain extends AppCompatActivity {
    EditText inputKode, inputPenjahit, inputGudang, inputTotal;
    Spinner spinnerMerek, spinnerModel, spinnerJenis;
    Button btnUpload, btnKembali;
    DatabaseReference dbRef;
    StokKainModel edit;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_upload_stok_kain);

        inputKode = findViewById(R.id.inputKodeId);
        spinnerMerek = findViewById(R.id.spinnerMerek);
        spinnerModel = findViewById(R.id.spinnerModelProduk);
        spinnerJenis = findViewById(R.id.spinnerJenisKain);
        inputPenjahit = findViewById(R.id.inputStokPenjahit);
        inputGudang = findViewById(R.id.inputStokGudang);
        inputTotal = findViewById(R.id.inputTotalStok);
        btnUpload = findViewById(R.id.btnUploadData);
        btnKembali = findViewById(R.id.kembali);

        dbRef = FirebaseDatabase.getInstance().getReference("stok_kain");

        edit = (StokKainModel) getIntent().getSerializableExtra("edit_stok");
        setupSpinners();

        if (edit != null) bindEditData();

        btnKembali.setOnClickListener(v -> finish());
        btnUpload.setOnClickListener(v -> saveData());

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                double penjahit = parseDouble(inputPenjahit);
                double gudang = parseDouble(inputGudang);
                if (penjahit >= 0 && gudang >= 0) {
                    double total = penjahit + gudang;
                    inputTotal.setText(String.valueOf(total));
                } else {
                    inputTotal.setText("");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        inputPenjahit.addTextChangedListener(watcher);
        inputGudang.addTextChangedListener(watcher);

    }

    private void setupSpinners() {
        loadSpinnerData("merek", spinnerMerek, "namaMerek");
        loadSpinnerData("model_produk", spinnerModel, "namaModel");
        loadSpinnerData("jenis_kain", spinnerJenis, "jenisKain");
    }

    private void loadSpinnerData(String nodePath, Spinner spinner, String keyField) {
        FirebaseDatabase.getInstance().getReference(nodePath)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> dataList = new ArrayList<>();
                        dataList.add("-- Pilih --"); // Tambahkan opsi kosong di awal

                        for (DataSnapshot child : snapshot.getChildren()) {
                            String value = child.child(keyField).getValue(String.class);
                            if (value != null) dataList.add(value);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(UploadStokKain.this,
                                android.R.layout.simple_spinner_dropdown_item, dataList);
                        spinner.setAdapter(adapter);

                        // Jika sedang edit, set selection sesuai data sebelumnya
                        if (edit != null) {
                            String selected = "";
                            if (keyField.equals("namaMerek")) selected = edit.merek;
                            else if (keyField.equals("namaModel")) selected = edit.modelProduk;
                            else if (keyField.equals("jenisKain")) selected = edit.jenisKain;

                            int idx = adapter.getPosition(selected);
                            if (idx >= 0) spinner.setSelection(idx);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UploadStokKain.this, "Gagal ambil data " + nodePath, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindEditData() {
        inputKode.setText(edit.kodeId);
        spinnerMerek.setSelection(findIndex(spinnerMerek, edit.merek));
        spinnerModel.setSelection(findIndex(spinnerModel, edit.modelProduk));
        spinnerJenis.setSelection(findIndex(spinnerJenis, edit.jenisKain));
        inputPenjahit.setText(String.valueOf(edit.stokPenjahit));
        inputGudang.setText(String.valueOf(edit.stokGudang));
        inputTotal.setText(String.valueOf(edit.totalStok));
        inputKode.setEnabled(false);
    }

    private int findIndex(Spinner sp, String value) {
        for (int i = 0; i < sp.getCount(); i++)
            if (sp.getItemAtPosition(i).equals(value)) return i;
        return 0;
    }

    private void saveData() {
        String kode = inputKode.getText().toString().trim();
        String mrk = spinnerMerek.getSelectedItem().toString();
        String mdl = spinnerModel.getSelectedItem().toString();
        String jns = spinnerJenis.getSelectedItem().toString();

        if (kode.isEmpty()) {
            Toast.makeText(this, "Kode ID wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mrk.equals("-- Pilih --") || mdl.equals("-- Pilih --") || jns.equals("-- Pilih --")) {
            Toast.makeText(this, "Pilih semua opsi Merek, Model, dan Jenis Kain", Toast.LENGTH_SHORT).show();
            return;
        }

        double penjahit = parseDouble(inputPenjahit);
        double gudang = parseDouble(inputGudang);

        if (penjahit < 0 || gudang < 0) {
            Toast.makeText(this, "Stok harus >= 0", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = penjahit + gudang;
        inputTotal.setText(String.valueOf(total)); // tampilkan hasil otomatis

        StokKainModel s = new StokKainModel(kode, mrk, mdl, jns, penjahit, gudang, total);
        dbRef.child(kode).setValue(s)
                .addOnSuccessListener(u -> {
                    Toast.makeText(this, "Berhasil simpan", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private int parseDouble(EditText et) {
        String t = et.getText().toString().trim();
        try { return Integer.parseInt(t); }
        catch (Exception e) { return -1; }
    }
}
