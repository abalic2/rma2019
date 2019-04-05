package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

        final ArrayAdapter<String> adapterOdgovora;
        adapterOdgovora = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, odgovori);
        listaOdgovora.setAdapter(adapterOdgovora);

        dodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                odgovori.add(odgovor.getText().toString());
                odgovor.setText("");
                adapterOdgovora.notifyDataSetChanged();
            }
        });

        dodajTacan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                odgovori.add(odgovor.getText().toString());
                adapterOdgovora.notifyDataSetChanged();
                novoPitanje.setTacan(odgovor.getText().toString());
                odgovor.setText("");
            }
        });

        spasiPitanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                novoPitanje.setNaziv(nazivPitanja.getText().toString());
                novoPitanje.setTekstPitanja(nazivPitanja.getText().toString());
                novoPitanje.setOdgovori(odgovori);
                pitanjaKviza.add(novoPitanje);
                Intent myIntent = new Intent();
                myIntent.putExtra("pitanja",pitanjaKviza);
                setResult(Activity.RESULT_OK, myIntent);
                finish();
            }
        });

        listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                odgovori.remove(position);
                adapterOdgovora.notifyDataSetChanged();
            }
        });
    }
}
