package com.example.myapplication.admin.model;

import java.io.Serializable;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties

public class ModelJenisKain implements Serializable {
    public String kodeId;
    public String jenisKain;
    public String logoUrl;
    public String gambarBase64;

    public ModelJenisKain() {
        // Default constructor
    }

    public ModelJenisKain(String kodeId, String jenisKain, String logoUrl, String gambarBase64) {
        this.kodeId = kodeId;
        this.jenisKain = jenisKain;
        this.logoUrl = logoUrl;
        this.gambarBase64 = gambarBase64;
    }
}
