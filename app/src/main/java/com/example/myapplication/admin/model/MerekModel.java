package com.example.myapplication.admin.model;

import java.io.Serializable;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties

public class MerekModel implements Serializable {
    public String namaMerek;
    public String kodeId;
    public String gambarBase64;
    public String logoUrl;

    public MerekModel() {
        // Diperlukan untuk Firebase
    }

    public MerekModel(String namaMerek, String kodeId, String gambarBase64, String logoUrl) {
        this.namaMerek = namaMerek;
        this.kodeId = kodeId;
        this.gambarBase64 = gambarBase64;
        this.logoUrl = logoUrl;
    }
}
