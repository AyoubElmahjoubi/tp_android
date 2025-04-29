package com.example.tp_app;

import android.graphics.Bitmap;

public class Etudiant {

    private Integer id ;
    private String Nom;
    private String Prénom;
    private String classe ;
    private String phone;
    private Bitmap photo;

    public Etudiant(Integer id, String nom, String prénom, String classe, Bitmap photo,String phone) {
        this.id = id;
        this.Nom = nom;
        this.Prénom = prénom;
        this.classe = classe;
        this.photo = photo;
        this.phone = phone;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return Nom;
    }

    public void setNom(String nom) {
        Nom = nom;
    }

    public String getPrénom() {
        return Prénom;
    }

    public void setPrénom(String prénom) {
        Prénom = prénom;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
