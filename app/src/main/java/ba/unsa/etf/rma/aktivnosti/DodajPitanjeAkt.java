package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import ba.unsa.etf.rma.intentServisi.DodajPitanje;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.receiveri.DodajPitanjeRec;

public class DodajPitanjeAkt extends AppCompatActivity implements DodajPitanjeRec.Receiver {
    private EditText nazivPitanja;
    private ListView listaOdgovora;
    private EditText odgovor;
    private Button dodaj;
    private Button dodajTacan;
    private Button spasiPitanje;

    private ArrayList<String> odgovori = new ArrayList<>();

    private boolean imaTacanOdgovor = false;
    private Pitanje novoPitanje;

    private DodajPitanjeRec mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje);

        mReceiver = new DodajPitanjeRec(new Handler());
        mReceiver.setReceiver(this);

        novoPitanje = new Pitanje();

        nazivPitanja = (EditText) findViewById(R.id.etNaziv);
        listaOdgovora = (ListView) findViewById(R.id.lvOdgovori);
        odgovor = (EditText) findViewById(R.id.etOdgovor);
        dodaj = (Button) findViewById(R.id.btnDodajOdgovor);
        dodajTacan = (Button) findViewById(R.id.btnDodajTacan);
        spasiPitanje = (Button) findViewById(R.id.btnDodajPitanje);


        nazivPitanja.setText("");
        final ArrayAdapter<String> adapterOdgovora;
        adapterOdgovora = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, odgovori) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (odgovori.get(position).equals(novoPitanje.getTacan()))
                    v.setBackgroundColor(getResources().getColor(R.color.zelenkasta));
                else
                    v.setBackgroundColor(getResources().getColor(R.color.bijela));
                return v;
            }
        };
        listaOdgovora.setAdapter(adapterOdgovora);

        dodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (odgovor.getText().toString().trim().equals("") || odgovori.contains(odgovor.getText().toString())) {
                    odgovor.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                } else {
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
                if (odgovor.getText().toString().trim().equals("") || odgovori.contains(odgovor.getText().toString())) {
                    odgovor.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                } else {
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
                if (jeLiSveValidno()) {
                    novoPitanje.setNaziv(nazivPitanja.getText().toString());
                    novoPitanje.setTekstPitanja(nazivPitanja.getText().toString());
                    novoPitanje.setOdgovori(odgovori);

                    Intent intentServis = new Intent(Intent.ACTION_SYNC, null, DodajPitanjeAkt.this, DodajPitanje.class);
                    intentServis.putExtra("pitanje", novoPitanje);
                    intentServis.putExtra("receiver", mReceiver);
                    startService(intentServis);
                }
            }
        });

        listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (odgovori.get(position).equals(novoPitanje.getTacan())) {
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
        if (!imaTacanOdgovor) {
            nemaGreska = false;
            odgovor.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
        }
        if (nazivPitanja.getText().toString().trim().equals("")) {
            nazivPitanja.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
            nemaGreska = false;
        }

        return nemaGreska;
    }

    @Override
    public void onReceiveResultPitanje(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                Intent myIntent = new Intent();
                myIntent.putExtra("novoPitanje", novoPitanje);
                setResult(Activity.RESULT_OK, myIntent);
                finish();
                break;
            case 2:
                nazivPitanja.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                prikaziAlertdialog("Pitanje sa tim tekstom vec postoji!");
                break;
        }
    }

    private void prikaziAlertdialog(String poruka) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(poruka);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}