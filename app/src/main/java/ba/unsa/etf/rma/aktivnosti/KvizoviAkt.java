package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.ListaAdapter;
import ba.unsa.etf.rma.adapteri.SpinnerAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class KvizoviAkt extends AppCompatActivity {
    private Spinner spinner;
    private ListView lista;
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();
    private SpinnerAdapter spAdapter;
    private ListaAdapter lsAdapter;

    private void odaberiKvizove(Kategorija kategorija){
        odabraniKvizovi.clear();
        if(kategorija.getNaziv().equalsIgnoreCase("Svi")) {
            odabraniKvizovi.addAll(kvizovi);
        }
        else{
            for(Kviz kviz : kvizovi)
                if(kviz.getKategorija().getNaziv().equalsIgnoreCase(kategorija.getNaziv()))
                    odabraniKvizovi.add(kviz);
        }
        lsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Kategorija pocetna = new Kategorija("Svi", "0");
        Kategorija k1 = new Kategorija("Azra", "1");
        Kategorija k2 = new Kategorija("Balic", "2");
        ArrayList<String> odgovori = new ArrayList<>();
        odgovori.add("Da"); odgovori.add("Ne");
        Pitanje p1 = new Pitanje("Pitanje 1", "Pitanje 1", odgovori,"Ne");
        kategorije.add(pocetna);
        kategorije.add(k1);
        kategorije.add(k2);
        ArrayList<Pitanje> pitanja = new ArrayList<>();
        pitanja.add(p1);

        Kviz kviz = new Kviz("Kviz 1", pitanja, k2);
        kvizovi.add(kviz);
        kvizovi.add(new Kviz("Kviz 2", null, k1));

        spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);
        lista = (ListView) findViewById(R.id.lvKvizovi);

        spAdapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_1, kategorije);
        spinner.setAdapter(spAdapter);

        lsAdapter = new ListaAdapter(this, odabraniKvizovi, getResources());
        lista.setAdapter(lsAdapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                myIntent.putExtra("kviz", odabraniKvizovi.get(position));
                ArrayList<Kategorija> sveRazliciteKategorije = new ArrayList<>();
                sveRazliciteKategorije.addAll(kategorije);
                sveRazliciteKategorije.remove(0);
                myIntent.putParcelableArrayListExtra("kategorije", sveRazliciteKategorije);
                KvizoviAkt.this.startActivity(myIntent);
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                odaberiKvizove(kategorije.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        View footer = getLayoutInflater().inflate(R.layout.footer_liste, null);
        lista.addFooterView(footer);


    }
}
