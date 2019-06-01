package ba.unsa.etf.rma.aktivnosti;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class IgrajKvizAkt extends AppCompatActivity implements PitanjeFrag.OnItemClick {
    private Kviz kviz;
    private ArrayList<Pitanje> pitanja;
    private ArrayList<String> odgovori;
    private Pitanje pitanje;
    private int brojTacnih = 0;
    private int brojPreostalih;
    private int ukupanBrojPitanja;
    private String imeIgraca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz);

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
                if(wantToCloseDialog)
                    dialog.dismiss();
                else{
                    edittext.setBackgroundColor(getResources().getColor(R.color.crvenkasta));
                }
            }
        });



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

}
