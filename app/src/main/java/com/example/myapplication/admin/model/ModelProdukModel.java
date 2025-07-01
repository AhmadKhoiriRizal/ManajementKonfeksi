package com.example.myapplication.admin.model;

import java.io.Serializable;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties

public class ModelProdukModel implements Serializable {
    public String kodeId;
    public String namaModel;
    public String logoUrl;
    public String gambarBase64;

    public ModelProdukModel() {}

    public ModelProdukModel(String kodeId, String namaModel, String logoUrl, String gambarBase64) {
        this.kodeId = kodeId;
        this.namaModel = namaModel;
        this.logoUrl = logoUrl;
        this.gambarBase64 = gambarBase64;
    }
}
