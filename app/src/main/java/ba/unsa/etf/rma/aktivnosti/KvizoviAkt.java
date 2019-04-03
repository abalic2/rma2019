package ba.unsa.etf.rma.aktivnosti;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.SpinnerAdapter;
import ba.unsa.etf.rma.klase.Kategorija;

public class KvizoviAkt extends AppCompatActivity {
    private Spinner spinner;
    private ListView lista;
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private SpinnerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Kategorija pocetna = new Kategorija("Svi","0");
        Kategorija k1 = new Kategorija("Azra","1");
        Kategorija k2 = new Kategorija("Balic","2");
        kategorije.add(pocetna); kategorije.add(k1); kategorije.add(k2);

        spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);
        lista = (ListView) findViewById(R.id.lvKvizovi);

        adapter = new SpinnerAdapter(this,android.R.layout.simple_list_item_1,kategorije);

        spinner.setAdapter(adapter);








    }
}
