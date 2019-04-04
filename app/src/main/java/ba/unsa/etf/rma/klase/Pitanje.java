package ba.unsa.etf.rma.klase;

import java.io.Serializable;
import java.util.ArrayList;

public class Pitanje implements Serializable {
    private String naziv;
    private String tekstPitanja;
    private ArrayList<String> odgovori;
    private String tacan;

    public Pitanje(String naziv, String tekstPitanja, ArrayList<String> odgovori, String tacan) {
        this.naziv = naziv;
        this.tekstPitanja = tekstPitanja;
        this.odgovori = odgovori;
        this.tacan = tacan;
    }

    public Pitanje() {
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getTekstPitanja() {
        return tekstPitanja;
    }

    public void setTekstPitanja(String tekstPitanja) {
        this.tekstPitanja = tekstPitanja;
    }

    public ArrayList<String> getOdgovori() {
        return odgovori;
    }

    public void setOdgovori(ArrayList<String> odgovori) {
        this.odgovori = odgovori;
    }

    public String getTacan() {
        return tacan;
    }

    public void setTacan(String tacan) {
        this.tacan = tacan;
    }

    ArrayList<String> dajRandomOdgovore(){
        ArrayList<String> odgovori = new ArrayList<>();
        return odgovori;

    }
}
