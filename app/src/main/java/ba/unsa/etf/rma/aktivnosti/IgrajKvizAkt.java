package ba.unsa.etf.rma.aktivnosti;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangListaFrag;
import ba.unsa.etf.rma.intentServisi.DodajURangListu;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.receiveri.DodajURangListuRec;

public class IgrajKvizAkt extends AppCompatActivity implements PitanjeFrag.OnItemClick, DodajURangListuRec.Receiver {
    private Kviz kviz;
    private ArrayList<Pitanje> pitanja;
    private ArrayList<String> odgovori;
    private Pitanje pitanje;
    private int brojTacnih = 0;
    private int brojPreostalih;
    private int ukupanBrojPitanja;
    private String imeIgraca;
    private DodajURangListuRec mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz);

        mReceiver = new DodajURangListuRec(new Handler());
        mReceiver.setReceiver(this);

        kviz = (Kviz) getIntent().getSerializableExtra("kviz");
        brojPreostalih = kviz.getPitanja().size() - 1;
        ukupanBrojPitanja = kviz.getPitanja().size();
        pitanja = new ArrayList<>();
        pitanja.addAll(kviz.getPitanja());
        Collections.shuffle(pitanja);

        InformacijeFrag fi = (InformacijeFrag) getSupportFragmentManager().findFragmentById(R.id.informacijePlace);

        if (fi == null) {
            fi = new InformacijeFrag();
            Bundle argumenti = new Bundle();
            argumenti.putSerializable("kviz", kviz);
            argumenti.putInt("tacni", brojTacnih);
            argumenti.putInt("preostali", brojPreostalih);
            argumenti.putInt("ukupanBroj",ukupanBrojPitanja);
            fi.setArguments(argumenti);
            getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, fi).commit();
        }

        PitanjeFrag fp = (PitanjeFrag) getSupportFragmentManager().findFragmentById(R.id.pitanjePlace);

        if (fp == null) {
            odgovori = new ArrayList<>();
            fp = new PitanjeFrag();
            if(brojPreostalih != -1){
                pitanje = pitanja.get(0);
                pitanja.remove(0);
                odgovori.addAll(pitanje.dajRandomOdgovore());
            }
            else {
                pitanje = null;
                unesiIme();

            }
            Bundle argumenti = new Bundle();
            argumenti.putSerializable("pitanje", pitanje);
            if(pitanje != null) argumenti.putStringArrayList("odgovori", odgovori);
            fp.setArguments(argumenti);
            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, fp).commit();
        }

    }

    private void unesiIme() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Unesite ime:");
        final EditText edittext = new EditText(this);
        builder.setView(edittext);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String ime = edittext.getText().toString();
                Boolean wantToCloseDialog = !ime.trim().equals("");
                if(wantToCloseDialog) {
                    int proslo = ukupanBrojPitanja - brojPreostalih - 1;
                    double procenat = 0;
                    if (proslo != 0) {
                        procenat = (double) brojTacnih / proslo * 100;
                    }
                    dodajRezultatUBazu(ime, procenat );
                    dialog.dismiss();
                }
                else{
                    edittext.setBackgroundColor(getResources().getColor(R.color.crvenkasta));
                }
            }
        });



    }

    private void dodajRezultatUBazu(String ime, double procenat) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DodajURangListu.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("imeIgraca", ime);
        intent.putExtra("procenat", procenat);
        intent.putExtra("idKviza", kviz.getId());
        intent.putExtra("nazivKviza",kviz.getNaziv());
        startService(intent);
    }

    @Override
    public void onItemClicked(int pos) {

        String odgovor = odgovori.get(pos);
        if (odgovor.equals(pitanje.getTacan())) {
            brojTacnih++;
        }
        brojPreostalih--;

        Bundle argumenti = new Bundle();
        argumenti.putSerializable("kviz", kviz);
        argumenti.putInt("tacni", brojTacnih);
        argumenti.putInt("preostali", brojPreostalih);
        argumenti.putInt("ukupanBroj",ukupanBrojPitanja);
        InformacijeFrag finovi = new InformacijeFrag();
        finovi.setArguments(argumenti);
        getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, finovi).commit();

        //čekanje 2s
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PitanjeFrag fpnovo = new PitanjeFrag();
                odgovori = new ArrayList<>();
                if (brojPreostalih != -1) {
                    pitanje = pitanja.get(0);
                    pitanja.remove(0);
                    odgovori.addAll(pitanje.dajRandomOdgovore());
                } else {
                    pitanje = null;
                    unesiIme();
                }

                Bundle argumenti = new Bundle();
                argumenti.putSerializable("pitanje", pitanje);
                if(pitanje != null) argumenti.putStringArrayList("odgovori", odgovori);
                fpnovo.setArguments(argumenti);
                try {
                    getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, fpnovo).commit();
                }
                catch (Exception e){
                    finish();
                }
            }
        }, 2000);
    }

    @Override
    public void onReceiveResultRanglista(int resultCode, Bundle resultData) {
        switch(resultCode){
            case 1:
                ArrayList<Pair<String,Double>> lista = (ArrayList<Pair<String, Double>>) resultData.getSerializable("lista");
                Bundle argumenti = new Bundle();
                argumenti.putSerializable("lista", lista);
                RangListaFrag rlf = new RangListaFrag();
                rlf.setArguments(argumenti);
                getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rlf).commit();

        }

    }
}
