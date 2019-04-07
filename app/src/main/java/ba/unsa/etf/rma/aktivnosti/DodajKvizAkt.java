package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

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
    private EditText imeKviza;
    private Button dugme;
    private boolean dodavanjeNovogKviza = false;
    private ArrayList<Kategorija> kategorije;
    private ArrayList<Kviz> kvizovi;
    private int pozicijaKliknutog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz);

        imeKviza = (EditText) findViewById(R.id.etNaziv);
        spinner = (Spinner) findViewById(R.id.spKategorije);
        listaPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
        listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
        dugme = (Button) findViewById(R.id.btnDodajKviz);

        kategorije = (ArrayList<Kategorija>) getIntent().getSerializableExtra("kategorije");
        kvizovi = (ArrayList<Kviz>) getIntent().getSerializableExtra("kvizovi");
        pozicijaKliknutog = getIntent().getExtras().getInt("redniBroj");
        Kategorija dodaj = new Kategorija();
        dodaj.setNaziv("Dodaj kategoriju");
        kategorije.add(dodaj);
        final Kviz kviz = (Kviz) getIntent().getSerializableExtra("kviz");
        imeKviza.setText("");

        adapterPitanja = new PitanjaAdapter(this, pitanja, getResources());
        listaPitanja.setAdapter(adapterPitanja);
        adapterMogucihPitanja = new PitanjaAdapter(this, mogucaPitanja, getResources());
        listaMogucihPitanja.setAdapter(adapterMogucihPitanja);

        spAdapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_1, kategorije);
        spinner.setAdapter(spAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(kategorije.get(position).getNaziv().equalsIgnoreCase("Dodaj kategoriju")){
                    Intent myIntent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
                    myIntent.putExtra("kategorije", kategorije);
                    DodajKvizAkt.this.startActivityForResult(myIntent,3);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if(kviz == null){
            dodavanjeNovogKviza = true;
            Kategorija odabrana = (Kategorija) getIntent().getSerializableExtra("oznacenaKategorija");
            int indeks = 0;
            //da namjesti na kateogriju koja je bila u proslom prozoru
            for(Kategorija k : kategorije) {
                if(k.getNaziv().equalsIgnoreCase(odabrana.getNaziv())) break;
                indeks++;
            }
            spinner.setSelection(indeks);
        }
        else {
            imeKviza.setText(kviz.getNaziv());
            pitanja.addAll(kviz.getPitanja());
            //da namjesti na njegovu kategoriju
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

        View footer = getLayoutInflater().inflate(R.layout.footer_pitanja, null);
        listaPitanja.addFooterView(footer);

        footer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //dodavanje pitanja
                Intent myIntent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                myIntent.putExtra("listaPitanja", pitanja);
                myIntent.putExtra("listaMogucihPitanja", mogucaPitanja);
                DodajKvizAkt.this.startActivityForResult(myIntent,2);
            }
        });

        dugme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(jeLiSveValidno()) {
                    Kviz novi = new Kviz(imeKviza.getText().toString(), pitanja, (Kategorija) spinner.getSelectedItem());
                    kategorije.remove(kategorije.size() - 1);
                    Intent myIntent = new Intent();
                    myIntent.putExtra("kviz", novi);
                    myIntent.putExtra("jeLiNovi", dodavanjeNovogKviza);
                    myIntent.putExtra("kategorije", kategorije);
                    setResult(Activity.RESULT_OK, myIntent);
                    finish();
                }

            }
        });


    }

    private boolean jeLiSveValidno() {
        boolean imaGreska = false;
        imeKviza.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
        String naziv = imeKviza.getText().toString();
        if(naziv.trim().equals("")){
            imaGreska = true;
            imeKviza.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
        }
        ArrayList<Kviz> kk = new ArrayList<>();
        kk.addAll(kvizovi);
        if(!dodavanjeNovogKviza) kk.remove(pozicijaKliknutog); //da ne gleda sebe
        for(Kviz k : kk){
            if (k.getNaziv().equals(naziv)){
                imaGreska = true;
                imeKviza.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
            }
        }
        return !imaGreska;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            //povratak iz DodajPitanjaAkt
            if(resultCode == Activity.RESULT_OK){
                ArrayList<Pitanje> vracenaPitanja = (ArrayList<Pitanje>) data.getSerializableExtra("pitanja");
                pitanja.clear();
                pitanja.addAll(vracenaPitanja);
                adapterPitanja.notifyDataSetChanged();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
        else if(requestCode == 3){
            //povratak iz DodajKategorijuAkt
            if(resultCode == Activity.RESULT_OK){
                ArrayList<Kategorija> vraceneKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                kategorije.clear();
                kategorije.addAll(vraceneKategorije);
                spAdapter.notifyDataSetChanged();
                spinner.setSelection(kategorije.size()-2);
            }
            else  {
                spinner.setSelection(0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //ako pritisne back da moze ipak dodane kategorije prenijet
        kategorije.remove(kategorije.size() - 1);
        Intent myIntent = new Intent();
        myIntent.putExtra("kategorije", kategorije);
        setResult(Activity.RESULT_CANCELED, myIntent);
        super.onBackPressed();
    }
}
