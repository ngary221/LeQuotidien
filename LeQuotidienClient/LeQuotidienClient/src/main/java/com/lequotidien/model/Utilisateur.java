package com.lequotidien.model;

/**
 * Représente un utilisateur du système Le•Quotidien.
 */
public class Utilisateur {

    private int id;
    private String nom;
    private String email;
    private String role;
    private boolean actif;

    public Utilisateur() {}

    public Utilisateur(int id, String nom, String email, String role, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.actif = actif;
    }

    public int getId()            { return id; }
    public String getNom()        { return nom; }
    public String getEmail()      { return email; }
    public String getRole()       { return role; }
    public boolean isActif()      { return actif; }

    public void setId(int id)          { this.id = id; }
    public void setNom(String nom)     { this.nom = nom; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role)   { this.role = role; }
    public void setActif(boolean actif){ this.actif = actif; }

    @Override
    public String toString() {
        return String.format("Utilisateur{id=%d, nom='%s', email='%s', role='%s', actif=%b}",
                id, nom, email, role, actif);
    }
}
