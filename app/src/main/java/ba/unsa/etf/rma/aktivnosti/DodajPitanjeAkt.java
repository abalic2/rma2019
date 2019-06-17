package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.SQLiteBaza;
import ba.unsa.etf.rma.intentServisi.DajSvaPitanja;
import ba.unsa.etf.rma.intentServisi.DajSveIzRangListe;
import ba.unsa.etf.rma.intentServisi.DajSveKategorije;
import ba.unsa.etf.rma.intentServisi.DajSveKvizove;
import ba.unsa.etf.rma.intentServisi.DodajPitanje;
import ba.unsa.etf.rma.intentServisi.DodajURangListuVise;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.receiveri.DajSvaPitanjaRec;
import ba.unsa.etf.rma.receiveri.DajSveIzRangListeRec;
import ba.unsa.etf.rma.receiveri.DajSveKategorijeRec;
import ba.unsa.etf.rma.receiveri.DajSveKvizoveRec;
import ba.unsa.etf.rma.receiveri.DodajPitanjeRec;
import ba.unsa.etf.rma.receiveri.DodajURangListuViseRec;

public class DodajPitanjeAkt extends AppCompatActivity implements DodajPitanjeRec.Receiver,
        DajSveKategorijeRec.Receiver, DajSveKvizoveRec.Receiver, DajSvaPitanjaRec.Receiver,
        DajSveIzRangListeRec.Receiver , DodajURangListuViseRec.Receiver {
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
    private DajSveKategorijeRec kReceiver;
    private DajSveKvizoveRec nReceiver;
    private DajSvaPitanjaRec pReceiver;
    private DajSveIzRangListeRec rReceiver;
    private DodajURangListuViseRec cReceiver;

    private boolean imaInterneta = true;
    private boolean dodaloSeUFirebase = false;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Pitanje> svaPitanja = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Pair<Kviz, Pair<String, Double>>> rangLista = new ArrayList<>();
    private ArrayList<Pair<Kviz, Pair<String, Double>>> listaSQLite = new ArrayList<>();

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNetworkState();
        }
    };

    public void updateNetworkState() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean staroStanje = imaInterneta;
        imaInterneta = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(staroStanje != imaInterneta) promjena();
    }

    private void promjena() {
        if(imaInterneta){
            //upalio se internet
            pocniAzuriranjeBaze();
        }
        else{
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

    private void pocniAzuriranjeBaze(){
        Toast.makeText(getApplicationContext(),"Azuriranje baze...", Toast.LENGTH_SHORT).show();
        popuniKategorijeIzBaze();
    }

    private void ucitajRangListu(){
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSveIzRangListe.class);
        intent.putExtra("receiver", rReceiver);
        intent.putExtra("kvizovi", kvizovi);
        startService(intent);
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
        setContentView(R.layout.activity_dodaj_pitanje);

        mReceiver = new DodajPitanjeRec(new Handler());
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

        novoPitanje = new Pitanje();

        nazivPitanja = (EditText) findViewById(R.id.etNaziv);
        listaOdgovora = (ListView) findViewById(R.id.lvOdgovori);
        odgovor = (EditText) findViewById(R.id.etOdgovor);
        dodaj = (Button) findViewById(R.id.btnDodajOdgovor);
        dodajTacan = (Button) findViewById(R.id.btnDodajTacan);
        spasiPitanje = (Button) findViewById(R.id.btnDodajPitanje);

        kvizovi.clear();
        SQLiteBaza baza = new SQLiteBaza(this);
        kvizovi.addAll(baza.dajSveKvizove());
        popuniRangListuIzSQLite();


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
                if(imaInterneta) {
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
                else{
                    Toast.makeText(getApplicationContext(),"Nema interneta", Toast.LENGTH_SHORT).show();
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

                SQLiteBaza baza = new SQLiteBaza(DodajPitanjeAkt.this);
                baza.ubaciJednoPitanjeSaOdgovorima(novoPitanje);

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