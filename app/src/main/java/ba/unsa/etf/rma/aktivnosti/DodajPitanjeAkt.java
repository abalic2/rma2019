package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

import static android.graphics.Color.rgb;

public class DodajPitanjeAkt extends AppCompatActivity {
    private EditText nazivPitanja;
    private ListView listaOdgovora;
    private EditText odgovor;
    private Button dodaj;
    private Button dodajTacan;
    private Button spasiPitanje;
    private ArrayList<String> odgovori = new ArrayList<>();
    private ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();
    private ArrayList<Pitanje> mogucaPitanjaKviza = new ArrayList<>();
    private boolean imaTacanOdgovor = false;
    private int red = rgb(240,128,128);
    private int bijela = rgb(245,245,245);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje);

        final Pitanje novoPitanje = new Pitanje();

        nazivPitanja = (EditText) findViewById(R.id.etNaziv);
        listaOdgovora = (ListView) findViewById(R.id.lvOdgovori);
        odgovor = (EditText) findViewById(R.id.etOdgovor);
        dodaj = (Button) findViewById(R.id.btnDodajOdgovor);
        dodajTacan = (Button) findViewById(R.id.btnDodajTacan);
        spasiPitanje = (Button) findViewById(R.id.btnDodajPitanje);
        nazivPitanja.setBackgroundColor(bijela);
        odgovor.setBackgroundColor(bijela);

        pitanjaKviza = (ArrayList<Pitanje>) getIntent().getSerializableExtra("listaPitanja");
        mogucaPitanjaKviza = (ArrayList<Pitanje>) getIntent().getSerializableExtra("listaMogucihPitanja");

        nazivPitanja.setText("");
        final ArrayAdapter<String> adapterOdgovora;
        adapterOdgovora = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, odgovori){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if(odgovori.get(position).equals(novoPitanje.getTacan()))
                    v.setBackgroundColor(rgb(60,179,113));
                else
                    v.setBackgroundColor(bijela);
                return v;
            }
        };
        listaOdgovora.setAdapter(adapterOdgovora);

        dodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(odgovori.contains(odgovor.getText().toString())){
                    odgovor.setBackgroundColor(red);
                }
                else {
                    odgovori.add(odgovor.getText().toString());
                    odgovor.setText("");
                    adapterOdgovora.notifyDataSetChanged();
                }
            }
        });

        dodajTacan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(odgovori.contains(odgovor.getText().toString())){
                    odgovor.setBackgroundColor(red);
                }
                else {
                    imaTacanOdgovor = true;
                    dodajTacan.setEnabled(false);
                    odgovori.add(odgovor.getText().toString());
                    adapterOdgovora.notifyDataSetChanged();
                    novoPitanje.setTacan(odgovor.getText().toString());
                    odgovor.setText("");
                }
            }
        });

        spasiPitanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(jeLiSveValidno()) {
                    novoPitanje.setNaziv(nazivPitanja.getText().toString());
                    novoPitanje.setTekstPitanja(nazivPitanja.getText().toString());
                    novoPitanje.setOdgovori(odgovori);
                    pitanjaKviza.add(novoPitanje);
                    Intent myIntent = new Intent();
                    myIntent.putExtra("pitanja", pitanjaKviza);
                    setResult(Activity.RESULT_OK, myIntent);
                    finish();
                }
            }
        });

        listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(odgovori.get(position).equals(novoPitanje.getTacan())){
                    imaTacanOdgovor = false;
                    novoPitanje.setTacan("");
                    dodajTacan.setEnabled(true);
                }
                odgovori.remove(position);
                adapterOdgovora.notifyDataSetChanged();
            }
        });
    }

    private boolean jeLiSveValidno() {
        nazivPitanja.setBackgroundColor(bijela);
        odgovor.setBackgroundColor(bijela);
        boolean nemaGreska = true;
        if(!imaTacanOdgovor){
            nemaGreska = false;
            odgovor.setBackgroundColor(red);
        }
        if(nazivPitanja.getText().toString().equals("")){
            nazivPitanja.setBackgroundColor(red);
            nemaGreska = false;
        }
        for(Pitanje p : pitanjaKviza){
            if(p.getNaziv().equals(nazivPitanja.getText().toString())){
                nazivPitanja.setBackgroundColor(red);
                nemaGreska = false;
            }
        }
        for(Pitanje p : mogucaPitanjaKviza){
            if(p.getNaziv().equals(nazivPitanja.getText().toString())){
                nazivPitanja.setBackgroundColor(red);
                nemaGreska = false;
            }
        }
        return nemaGreska;
    }
}
