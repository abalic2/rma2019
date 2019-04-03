package ba.unsa.etf.rma.aktivnosti;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.ListaAdapter;
import ba.unsa.etf.rma.adapteri.SpinnerAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;

public class KvizoviAkt extends AppCompatActivity {
    private Spinner spinner;
    private ListView lista;
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private SpinnerAdapter spAdapter;
    private ListaAdapter lsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Kategorija pocetna = new Kategorija("Svi","0");
        Kategorija k1 = new Kategorija("Azra","1");
        Kategorija k2 = new Kategorija("Balic","2");
        kategorije.add(pocetna); kategorije.add(k1); kategorije.add(k2);

        Kviz kviz = new Kviz("Kviz 1",null,k1);
        kvizovi.add(kviz);
        kvizovi.add(new Kviz("Kviz 2", null, k2));

        spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);
        lista = (ListView) findViewById(R.id.lvKvizovi);

        spAdapter = new SpinnerAdapter(this,android.R.layout.simple_list_item_1,kategorije);
        spinner.setAdapter(spAdapter);

        lsAdapter = new ListaAdapter(this,kvizovi,getResources());
        lista.setAdapter(lsAdapter);

        View footer = getLayoutInflater().inflate(R.layout.footer_liste, null);
        lista.addFooterView(footer);








    }
}
