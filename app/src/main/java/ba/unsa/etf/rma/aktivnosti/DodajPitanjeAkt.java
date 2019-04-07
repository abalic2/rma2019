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

        pitanjaKviza = (ArrayList<Pitanje>) getIntent().getSerializableExtra("listaPitanja");
        mogucaPitanjaKviza = (ArrayList<Pitanje>) getIntent().getSerializableExtra("listaMogucihPitanja");

        nazivPitanja.setText("");
        final ArrayAdapter<String> adapterOdgovora;
        adapterOdgovora = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, odgovori){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if(odgovori.get(position).equals(novoPitanje.getTacan()))
                    v.setBackgroundColor(getResources().getColor(R.color.zelena));
                else
                    v.setBackgroundColor(getResources().getColor(R.color.bijela));
                return v;
            }
        };
        listaOdgovora.setAdapter(adapterOdgovora);

        dodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(odgovor.getText().toString().trim().equals("") || odgovori.contains(odgovor.getText().toString())){
                    odgovor.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                }
                else {
                    odgovori.add(odgovor.getText().toString());
                    odgovor.setText("");
                    adapterOdgovora.notifyDataSetChanged();
                    odgovor.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
                }
            }
        });

        dodajTacan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(odgovor.getText().toString().trim().equals("") || odgovori.contains(odgovor.getText().toString())){
                    odgovor.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                }
                else {
                    imaTacanOdgovor = true;
                    dodajTacan.setEnabled(false);
                    odgovori.add(odgovor.getText().toString());
                    adapterOdgovora.notifyDataSetChanged();
                    novoPitanje.setTacan(odgovor.getText().toString());
                    odgovor.setText("");
                    odgovor.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
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
        nazivPitanja.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
        odgovor.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
        boolean nemaGreska = true;
        if(!imaTacanOdgovor){
            nemaGreska = false;
            odgovor.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
        }
        if(nazivPitanja.getText().toString().trim().equals("")){
            nazivPitanja.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
            nemaGreska = false;
        }
        for(Pitanje p : pitanjaKviza){
            if(p.getNaziv().equals(nazivPitanja.getText().toString())){
                nazivPitanja.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                nemaGreska = false;
            }
        }
        for(Pitanje p : mogucaPitanjaKviza){
            if(p.getNaziv().equals(nazivPitanja.getText().toString())){
                nazivPitanja.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                nemaGreska = false;
            }
        }
        return nemaGreska;
    }
}
