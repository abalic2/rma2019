package ba.unsa.etf.rma.aktivnosti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.AlarmClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.SQLiteBaza;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangListaFrag;
import ba.unsa.etf.rma.intentServisi.DajSvaPitanja;
import ba.unsa.etf.rma.intentServisi.DajSveIzRangListe;
import ba.unsa.etf.rma.intentServisi.DajSveKategorije;
import ba.unsa.etf.rma.intentServisi.DajSveKvizove;
import ba.unsa.etf.rma.intentServisi.DodajURangListu;
import ba.unsa.etf.rma.intentServisi.DodajURangListuVise;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.receiveri.DajSvaPitanjaRec;
import ba.unsa.etf.rma.receiveri.DajSveIzRangListeRec;
import ba.unsa.etf.rma.receiveri.DajSveKategorijeRec;
import ba.unsa.etf.rma.receiveri.DajSveKvizoveRec;
import ba.unsa.etf.rma.receiveri.DodajURangListuRec;
import ba.unsa.etf.rma.receiveri.DodajURangListuViseRec;

public class IgrajKvizAkt extends AppCompatActivity implements PitanjeFrag.OnItemClick, DodajURangListuRec.Receiver,
        DajSveKategorijeRec.Receiver, DajSveKvizoveRec.Receiver, DajSvaPitanjaRec.Receiver, DajSveIzRangListeRec.Receiver
        , DodajURangListuViseRec.Receiver{
    private Kviz kviz;
    private ArrayList<Pitanje> pitanja;
    private ArrayList<String> odgovori;
    private Pitanje pitanje;
    private int brojTacnih = 0;
    private int brojPreostalih;
    private int ukupanBrojPitanja;


    private boolean imaInterneta = true;
    private ArrayList<Pitanje> svaPitanja = new ArrayList<>();
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Pair<Kviz, Pair<String, Double>>> rangLista = new ArrayList<>();
    private ArrayList<Pair<Kviz, Pair<String, Double>>> listaSQLite = new ArrayList<>();
    private ArrayList<Pair<String, Double>> rangListaKviza = new ArrayList<>();

    private DajSveKategorijeRec kReceiver;
    private DajSveKvizoveRec nReceiver;
    private DajSvaPitanjaRec pReceiver;
    private DodajURangListuRec mReceiver;
    private DajSveIzRangListeRec rReceiver;
    private DodajURangListuViseRec cReceiver;
    private boolean dodaloSeUFirebase = false;

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNetworkState();
        }
    };



    public void updateNetworkState() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean staroStanje = imaInterneta;
        imaInterneta = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (staroStanje != imaInterneta) promjena();
    }

    private void promjena() {
        if (imaInterneta) {
            //upalio se internet
            pocniAzuriranjeBaze();
        } else {
            //ugasio se internet
        }
    }

    public void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        updateNetworkState();
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }

    private void pocniAzuriranjeBaze() {
        Toast.makeText(getApplicationContext(), "Azuriranje baze...", Toast.LENGTH_SHORT).show();
        popuniKategorijeIzBaze();
    }

    private void popuniKategorijeIzBaze() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSveKategorije.class);
        intent.putExtra("receiver", kReceiver);
        startService(intent);
    }

    @Override
    public void onReceiveResultKategorije(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                kategorije.clear();
                kategorije.add(new Kategorija("Svi", "0"));
                ArrayList<Kategorija> k3 = (ArrayList<Kategorija>) resultData.get("kategorije");
                kategorije.addAll(k3);

                zovniDajSveKvizove(false);
        }
    }

    void zovniDajSveKvizove(boolean dodaj) {
        Intent intent1 = new Intent(Intent.ACTION_SYNC, null, this, DajSveKvizove.class);
        intent1.putExtra("receiver", nReceiver);
        intent1.putExtra("dodaj", dodaj);
        startService(intent1);
    }

    @Override
    public void onReceiveResultKvizovi(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                ArrayList<Kviz> k = (ArrayList<Kviz>) resultData.get("kvizovi");
                kvizovi.clear();
                kvizovi.addAll(k);

                popuniSvaPitanjaIzBaze();
                break;

        }
    }

    private void popuniSvaPitanjaIzBaze() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSvaPitanja.class);
        intent.putExtra("receiver", pReceiver);
        startService(intent);
    }

    @Override
    public void onReceiveResultPitanja(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                svaPitanja.clear();
                ArrayList<Pitanje> p = (ArrayList<Pitanje>) resultData.get("pitanja");
                svaPitanja.addAll(p);
                ucitajRangListu();

        }
    }

    private void ucitajRangListu() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSveIzRangListe.class);
        intent.putExtra("receiver", rReceiver);
        intent.putExtra("kvizovi", kvizovi);
        startService(intent);
    }


    @Override
    public void onReceiveResultRangListaSvega(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                ArrayList<Pair<Kviz, Pair<String, Double>>> r = (ArrayList<Pair<Kviz, Pair<String, Double>>>) resultData.get("rangLista");
                if(dodaloSeUFirebase){
                    SQLiteBaza baza = new SQLiteBaza(this);
                    rangLista.clear();
                    rangLista.addAll(r);
                    baza.ubaciRangListu(rangLista);
                    dodaloSeUFirebase = false;
                    Toast.makeText(getApplicationContext(), "Azuriranje baze zavrseno", Toast.LENGTH_SHORT).show();
                }
                else {
                    listaSQLite.clear();
                    listaSQLite.addAll(rangLista);
                    //novo stanje iz online baze
                    rangLista.clear();
                    rangLista.addAll(r);
                    osvjeziSQLiteBazu();
                }
        }
    }

    @Override
    public void onReceiveResultRangListaVise(int resultCode, Bundle resultData) {
        switch (resultCode){
            case 1:
                dodaloSeUFirebase = true;
                ucitajRangListu();
        }
    }

    private void osvjeziSQLiteBazu() {
        SQLiteBaza baza = new SQLiteBaza(this);
        baza.ubaciKategorije(kategorije);
        baza.ubaciPitanjaIOdgovore(svaPitanja);
        baza.ubaciKvizove(kvizovi);
        ubaciUFirebase();


    }

    private void ubaciUFirebase() {
        ArrayList<Kviz> kk = new ArrayList<>();
        ArrayList<String> imena = new ArrayList<>();
        ArrayList<Double> procenti = new ArrayList<>();
        //idem kroz SQLite i gledam ima li u firebaseu
        for (Pair<Kviz, Pair<String, Double>> igra : listaSQLite) {
            boolean ima = false;
            for (Pair<Kviz, Pair<String, Double>> igraUFirebase : rangLista) {
                if (igra.first.getNaziv().equals(igraUFirebase.first.getNaziv()) &&
                        igra.second.first.equals(igraUFirebase.second.first) &&
                        igra.second.second.equals(igraUFirebase.second.second)){
                    ima = true;
                    break;
                }
            }
            if (!ima) {
                kk.add(igra.first);
                imena.add(igra.second.first);
                procenti.add(igra.second.second);
            }
        }
        if(kk.size() != 0){

            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DodajURangListuVise.class);
            intent.putExtra("kvizovi", kk);
            intent.putExtra("procenti", procenti);
            intent.putExtra("imena", imena);
            intent.putExtra("receiver", cReceiver);
            startService(intent);
        }
        else{
            SQLiteBaza baza = new SQLiteBaza(this);
            baza.ubaciRangListu(rangLista);
            Toast.makeText(getApplicationContext(), "Azuriranje baze zavrseno", Toast.LENGTH_SHORT).show();
        }

    }

    private void popuniRangListuIzSQLite() {
        SQLiteBaza baza = new SQLiteBaza(this);
        rangLista.clear();
        for (Kviz k : kvizovi) {
            ArrayList<Pair<String, Double>> igraci = baza.dajRangListu(k);
            for (Pair<String, Double> pp : igraci) {
                rangLista.add(new Pair<Kviz, Pair<String, Double>>(k, pp));
            }
        }
    }


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz);

        mReceiver = new DodajURangListuRec(new Handler());
        mReceiver.setReceiver(this);
        kReceiver = new DajSveKategorijeRec(new Handler());
        kReceiver.setReceiver(this);
        nReceiver = new DajSveKvizoveRec(new Handler());
        nReceiver.setReceiver(this);
        pReceiver = new DajSvaPitanjaRec(new Handler());
        pReceiver.setReceiver(this);
        rReceiver = new DajSveIzRangListeRec(new Handler());
        rReceiver.setReceiver(this);
        cReceiver = new DodajURangListuViseRec(new Handler());
        cReceiver.setReceiver(this);

        kviz = (Kviz) getIntent().getSerializableExtra("kviz");
        brojPreostalih = kviz.getPitanja().size() - 1;
        ukupanBrojPitanja = kviz.getPitanja().size();
        pitanja = new ArrayList<>();
        pitanja.addAll(kviz.getPitanja());
        Collections.shuffle(pitanja);

        kvizovi.clear();
        SQLiteBaza baza = new SQLiteBaza(this);
        kvizovi.addAll(baza.dajSveKvizove());
        popuniRangListuIzSQLite();

        rangListaKviza.clear();
        ArrayList<Pair<String, Double>> igraci = baza.dajRangListu(kviz);
        rangListaKviza.addAll(igraci);


        if(ukupanBrojPitanja != 0) postaviAlarm();

        InformacijeFrag fi = (InformacijeFrag) getSupportFragmentManager().findFragmentById(R.id.informacijePlace);

        if (fi == null) {
            fi = new InformacijeFrag();
            Bundle argumenti = new Bundle();
            argumenti.putSerializable("kviz", kviz);
            argumenti.putInt("tacni", brojTacnih);
            argumenti.putInt("preostali", brojPreostalih);
            argumenti.putInt("ukupanBroj", ukupanBrojPitanja);
            fi.setArguments(argumenti);
            getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, fi).commit();
        }

        PitanjeFrag fp = (PitanjeFrag) getSupportFragmentManager().findFragmentById(R.id.pitanjePlace);

        if (fp == null) {
            odgovori = new ArrayList<>();
            fp = new PitanjeFrag();
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
            if (pitanje != null) argumenti.putStringArrayList("odgovori", odgovori);
            fp.setArguments(argumenti);
            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, fp).commit();
        }

    }

    private void postaviAlarm() {
        int trajanjeAlarma = (int) Math.ceil( ukupanBrojPitanja / 2.);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, trajanjeAlarma);
        int sati = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int sekunde = cal.get(Calendar.SECOND);
        if(sekunde > 0) minute++;

        Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
        i.putExtra(AlarmClock.EXTRA_HOUR, sati);
        i.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);

        startActivity(i);
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ime = edittext.getText().toString();
                Boolean wantToCloseDialog = !ime.trim().equals("");
                if (wantToCloseDialog) {
                    int proslo = ukupanBrojPitanja - brojPreostalih - 1;
                    double procenat = 0;
                    if (proslo != 0) {
                        procenat = (double) brojTacnih / proslo * 100;
                    }
                    dodajRezultatUBazu(ime, procenat);
                    dialog.dismiss();
                } else {
                    edittext.setBackgroundColor(getResources().getColor(R.color.crvenkasta));
                }
            }
        });


    }

    private void dodajRezultatUBazu(String ime, double procenat) {

        //dodavanje u sqlite
        SQLiteBaza baza = new SQLiteBaza(this);
        baza.ubaciIgruURangListu(ime, procenat, kviz);
        rangListaKviza = baza.dajRangListu(kviz);
        if (imaInterneta) {
            //dodavanje u firebase
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DodajURangListu.class);
            intent.putExtra("receiver", mReceiver);
            intent.putExtra("imeIgraca", ime);
            intent.putExtra("procenat", procenat);
            intent.putExtra("idKviza", kviz.getId());
            intent.putExtra("nazivKviza", kviz.getNaziv());
            startService(intent);
        } else {
            Bundle argumenti = new Bundle();
            popuniRangListuIzSQLite();
            napuniArgumente(argumenti, rangListaKviza);
            RangListaFrag rlf = new RangListaFrag();
            rlf.setArguments(argumenti);
            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rlf).commit();
        }

    }

    private void napuniArgumente(Bundle argumenti, ArrayList<Pair<String, Double>> lista) {
        ArrayList<String> imena = new ArrayList<>();
        ArrayList<Double> procenti = new ArrayList<>();
        for(Pair<String, Double> par : lista){
            imena.add(par.first);
            procenti.add(par.second);
        }
        argumenti.putStringArrayList("imena", imena);
        argumenti.putSerializable("procenti", procenti);
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
        argumenti.putInt("ukupanBroj", ukupanBrojPitanja);
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
                if (pitanje != null) argumenti.putStringArrayList("odgovori", odgovori);
                fpnovo.setArguments(argumenti);
                try {
                    getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, fpnovo).commit();
                } catch (Exception e) {
                    finish();
                }
            }
        }, 2000);
    }

    @Override
    public void onReceiveResultRanglista(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                ArrayList<Pair<String, Double>> lista = (ArrayList<Pair<String, Double>>) resultData.getSerializable("lista");
                Bundle argumenti = new Bundle();
                napuniArgumente(argumenti, lista);
                RangListaFrag rlf = new RangListaFrag();
                rlf.setArguments(argumenti);
                getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rlf).commit();

        }

    }
}
