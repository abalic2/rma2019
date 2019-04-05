package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.PitanjaAdapter;
import ba.unsa.etf.rma.adapteri.SpinnerAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajKvizAkt extends AppCompatActivity {

    private Spinner spinner;
    private SpinnerAdapter spAdapter;
    private ListView listaPitanja;
    private ListView listaMogucihPitanja;
    private PitanjaAdapter adapterPitanja;
    private PitanjaAdapter adapterMogucihPitanja;
    private ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> pitanja = new ArrayList<>();
    private TextView imeKviza;
    private Button dugme;
    private boolean dodavanjeNovogKviza = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz);

        imeKviza = (TextView) findViewById(R.id.etNaziv);
        spinner = (Spinner) findViewById(R.id.spKategorije);
        listaPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
        listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
        dugme = (Button) findViewById(R.id.btnDodajKviz);

        final ArrayList<Kategorija> kategorije = (ArrayList<Kategorija>) getIntent().getSerializableExtra("kategorije");
        Kviz kviz = (Kviz) getIntent().getSerializableExtra("kviz");

        adapterPitanja = new PitanjaAdapter(this, pitanja, getResources());
        listaPitanja.setAdapter(adapterPitanja);
        adapterMogucihPitanja = new PitanjaAdapter(this, mogucaPitanja, getResources());
        listaMogucihPitanja.setAdapter(adapterMogucihPitanja);

        spAdapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_1, kategorije);
        spinner.setAdapter(spAdapter);



        if(kviz == null){
            dodavanjeNovogKviza = true;
            Kviz noviKviz = new Kviz();
            Kategorija odabrana = (Kategorija) getIntent().getSerializableExtra("oznacenaKategorija");
            int indeks = 0;
            for(Kategorija k : kategorije) {
                if(k.getNaziv().equalsIgnoreCase(odabrana.getNaziv())) break;
                indeks++;
            }
            spinner.setSelection(indeks);
        }
        else {
            imeKviza.setText(kviz.getNaziv());
            pitanja.addAll(kviz.getPitanja());
            int indeks = 0;
            for(Kategorija k : kategorije) {
                if(k.getNaziv().equalsIgnoreCase(kviz.getKategorija().getNaziv())) break;
                indeks++;
            }
            spinner.setSelection(indeks);
        }

        listaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pitanje prebaci = pitanja.get(position);
                pitanja.remove(prebaci);
                mogucaPitanja.add(prebaci);
                adapterMogucihPitanja.notifyDataSetChanged();
                adapterPitanja.notifyDataSetChanged();
            }
        });

        listaMogucihPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pitanje prebaci = mogucaPitanja.get(position);
                pitanja.add(prebaci);
                mogucaPitanja.remove(prebaci);
                adapterMogucihPitanja.notifyDataSetChanged();
                adapterPitanja.notifyDataSetChanged();
            }
        });

        dugme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Kviz novi = new Kviz(imeKviza.getText().toString(),pitanja, (Kategorija) spinner.getSelectedItem());

                Intent myIntent = new Intent();
                myIntent.putExtra("kviz", novi);
                myIntent.putExtra("jeLiNovi", dodavanjeNovogKviza);
                myIntent.putExtra("kategorije", kategorije);
                setResult(Activity.RESULT_OK, myIntent);
                finish();

                /*Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish(); ako necu da vracam*/
            }
        });


    }
}
