package ba.unsa.etf.rma.klase;

import java.io.Serializable;
import java.util.ArrayList;

public class Kviz implements Serializable {
    private String naziv;
    private ArrayList<Pitanje> pitanja;
    private Kategorija kategorija;
    private String id;

    public Kviz(String naziv, ArrayList<Pitanje> pitanja, Kategorija kategorija, String id) {
        this.naziv = naziv;
        this.pitanja = pitanja;
        this.kategorija = kategorija;
        this.id = id;
    }

    public Kviz() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public ArrayList<Pitanje> getPitanja() {
        return pitanja;
    }

    public void setPitanja(ArrayList<Pitanje> pitanja) {
        this.pitanja = pitanja;
    }

    public Kategorija getKategorija() {
        return kategorija;
    }

    public void setKategorija(Kategorija kategorija) {
        this.kategorija = kategorija;
    }

    void dodajPitanje(Pitanje pitanje){
        pitanja.add(pitanje);
    }
}
