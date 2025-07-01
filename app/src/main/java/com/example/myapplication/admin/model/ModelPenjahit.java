package com.example.myapplication.admin.model;

import java.io.Serializable;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ModelPenjahit implements Serializable {
    public String userId, nama, nomorHp, email, alamat, fotoBase64;

    public ModelPenjahit() {
    }

    public ModelPenjahit(String userId, String nama, String nomorHp, String email, String alamat, String fotoBase64) {
        this.userId = userId;
        this.nama = nama;
        this.nomorHp = nomorHp;
        this.email = email;
        this.alamat = alamat;
        this.fotoBase64 = fotoBase64;
    }
}
