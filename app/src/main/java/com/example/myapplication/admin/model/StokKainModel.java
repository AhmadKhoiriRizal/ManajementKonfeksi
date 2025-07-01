package com.example.myapplication.admin.model;

import java.io.Serializable;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class StokKainModel implements Serializable {
    public String kodeId, merek, modelProduk, jenisKain;
    public double stokPenjahit, stokGudang, totalStok;

    public StokKainModel() { }

    public StokKainModel(String kodeId, String merek, String modelProduk, String jenisKain,
                         double stokPenjahit, double stokGudang, double totalStok) {
        this.kodeId = kodeId;
        this.merek = merek;
        this.modelProduk = modelProduk;
        this.jenisKain = jenisKain;
        this.stokPenjahit = stokPenjahit;
        this.stokGudang = stokGudang;
        this.totalStok = totalStok;
    }
}
