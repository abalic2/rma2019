package ba.unsa.etf.rma.aktivnosti;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.SQLiteBaza;
import ba.unsa.etf.rma.adapteri.ListaAdapter;
import ba.unsa.etf.rma.adapteri.SpinnerAdapter;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.intentServisi.DajSvaPitanja;
import ba.unsa.etf.rma.intentServisi.DajSveIzRangListe;
import ba.unsa.etf.rma.intentServisi.DajSveKategorije;
import ba.unsa.etf.rma.intentServisi.DajSveKvizove;
import ba.unsa.etf.rma.intentServisi.DajSveKvizoveKategorije;
import ba.unsa.etf.rma.intentServisi.DodajURangListuVise;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.receiveri.DajSvaPitanjaRec;
import ba.unsa.etf.rma.receiveri.DajSveIzRangListeRec;
import ba.unsa.etf.rma.receiveri.DajSveKategorijeRec;
import ba.unsa.etf.rma.receiveri.DajSveKvizoveKategorijaRec;
import ba.unsa.etf.rma.receiveri.DajSveKvizoveRec;
import ba.unsa.etf.rma.receiveri.DodajURangListuViseRec;

public class KvizoviAkt extends AppCompatActivity implements ListaFrag.OnItemClick, DetailFrag.OnItemClick,
        DajSveKvizoveKategorijaRec.Receiver, DajSveKategorijeRec.Receiver, DajSveKvizoveRec.Receiver,
        DajSvaPitanjaRec.Receiver, DajSveIzRangListeRec.Receiver, DodajURangListuViseRec.Receiver {

    private Spinner spinner;
    private ListView lista;
    private SpinnerAdapter spAdapter;
    private ListaAdapter lsAdapter;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();
    private ArrayList<Pitanje> svaPitanja = new ArrayList<>();
    private ArrayList<Pair<Kviz, Pair<String, Double>>> rangLista = new ArrayList<>();
    private ArrayList<Pair<Kviz, Pair<String, Double>>> listaSQLite = new ArrayList<>();

    private int pozicijaKategorija = 0;

    private DajSveKvizoveKategorijaRec mReceiver;
    private DajSveKategorijeRec kReceiver;
    private DajSveKvizoveRec nReceiver;
    private DajSvaPitanjaRec pReceiver;
    private DajSveIzRangListeRec rReceiver;
    private DodajURangListuViseRec cReceiver;

    private boolean imaInterneta = true;
    private boolean ucitavanjeSvegaZbogBaze = false;
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

    private void pocniAzuriranjeBaze() {
        Toast.makeText(getApplicationContext(), "Azuriranje baze...", Toast.LENGTH_SHORT).show();
        ucitavanjeSvegaZbogBaze = true;
        popuniKategorijeIzBaze();
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

    public void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        updateNetworkState();
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReceiver = new DajSveKvizoveKategorijaRec(new Handler());
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

        if (savedInstanceState != null) {
            pozicijaKategorija = savedInstanceState.getInt("pozicija");
        }

        FrameLayout detalji = (FrameLayout) findViewById(R.id.detailPlace);
        if (detalji == null) {
            spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);
            lista = (ListView) findViewById(R.id.lvKvizovi);

            spAdapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_1, kategorije);
            spinner.setAdapter(spAdapter);

            lsAdapter = new ListaAdapter(this, odabraniKvizovi, getResources());
            lista.setAdapter(lsAdapter);

            lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    prepraviKviz(position, 1);
                    return true;
                }
            });

            lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    igrajKviz(position);
                }
            });


            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    promijeniKvizove(kategorije.get(position));
                    pozicijaKategorija = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


            View footer = getLayoutInflater().inflate(R.layout.footer_liste, null);
            lista.addFooterView(footer);

            //za dodavanje kviza
            footer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dodajKviz(1);
                    return true;
                }
            });

        }


        updateNetworkState();
        if (imaInterneta) {
            pocniAzuriranjeBaze();
        } else {
            popuniIzSQLiteBaze();
        }

        if (savedInstanceState != null) {
            pozicijaKategorija = savedInstanceState.getInt("pozicija");
        }


    }

    private void popuniIzSQLiteBaze() {
        SQLiteBaza baza = new SQLiteBaza(this);
        kategorije.clear();
        svaPitanja.clear();
        kvizovi.clear();
        kategorije.addAll(baza.dajSveKategorije());
        svaPitanja.clear();
        svaPitanja.addAll(baza.dajSvaPitanja());
        kvizovi.addAll(baza.dajSveKvizove());
        popuniRangListuIzSQLite();
        prilagodiKategorije();

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

    private void popuniSvaPitanjaIzBaze() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSvaPitanja.class);
        intent.putExtra("receiver", pReceiver);
        startService(intent);
    }

    private void popuniKategorijeIzBaze() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, KvizoviAkt.this, DajSveKategorije.class);
        intent.putExtra("receiver", kReceiver);
        startService(intent);
    }

    private void ucitajRangListu() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, KvizoviAkt.this, DajSveIzRangListe.class);
        intent.putExtra("receiver", rReceiver);
        intent.putExtra("kvizovi", kvizovi);
        startService(intent);
    }


    private void prepraviKviz(int position, int kod) {
        updateNetworkState();
        if (imaInterneta) {
            Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
            myIntent.putExtra("idKviza", odabraniKvizovi.get(position).getId());
            KvizoviAkt.this.startActivityForResult(myIntent, kod);
        } else {
            Toast.makeText(getApplicationContext(), "Nema interneta", Toast.LENGTH_SHORT).show();
        }
    }

    private void igrajKviz(int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)) {
                    Toast.makeText(getApplicationContext(), "Dozvolite koristenje kalendara", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, 50);
            } else {
                upaliKviz(position);
            }
        } else {
            upaliKviz(position);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 50) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Igranje omoguceno", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Ne mozete igrati", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void upaliKviz(int position) {
        if (jeLiSlobodnoVrijeme(odabraniKvizovi.get(position).getPitanja().size())) {
            Intent myIntent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);
            myIntent.putExtra("kviz", odabraniKvizovi.get(position));
            KvizoviAkt.this.startActivityForResult(myIntent, 3);
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

    private boolean jeLiSlobodnoVrijeme(int size) {
        int trajanjeAlarma = (int) Math.ceil(size / 2.);
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.MINUTE, trajanjeAlarma); //gleda se samo pocetak eventa kao u postavci

        String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};

        Calendar startTime = Calendar.getInstance();

        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";

        Cursor cursor = this.getBaseContext().getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);


        if (cursor.moveToFirst()) { //ako ima icega znaci da ne moze igrati
            Date datumEventa = new Date(cursor.getLong(3));
            cursor.close();
            Date trenutniDatum = new Date();

            long duration = datumEventa.getTime() - trenutniDatum.getTime();

            long y = TimeUnit.MILLISECONDS.toMinutes(duration);

            String poruka = "Imate događaj u kalendaru za " + y + 1 + " minuta!";
            prikaziAlertdialog(poruka);
            return false;
        }
        return true;
    }

    private void dodajKviz(int kod) {
        updateNetworkState();
        if (imaInterneta) {
            Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
            myIntent.putExtra("idKviza", (String) null);
            if (kod == 1) {
                myIntent.putExtra("oznacenaKategorija", (Kategorija) spinner.getSelectedItem());
            } else {
                myIntent.putExtra("oznacenaKategorija", kategorije.get(pozicijaKategorija));
            }
            KvizoviAkt.this.startActivityForResult(myIntent, kod);
        } else {
            Toast.makeText(getApplicationContext(), "Nema interneta", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateNetworkState();
        pozicijaKategorija = 0;
        if (imaInterneta) pocniAzuriranjeBaze();
        else popuniIzSQLiteBaze();


    }

    @Override
    public void onItemClicked(int pos) { //odabir kategorije u sirokom ekranu
        //Priprema novog fragmenta FragmentDetalji
        pozicijaKategorija = pos;
        promijeniKvizove(kategorije.get(pos));
    }

    void posaljiDedailFragment() {
        Bundle arguments = new Bundle();
        arguments.putSerializable("kvizovi", odabraniKvizovi);
        DetailFrag fd = new DetailFrag();
        fd.setArguments(arguments);
        getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, fd).commit();
    }

    void posaljiListaFragment() {
        ListaFrag fl = new ListaFrag();
        Bundle argumenti = new Bundle();
        argumenti.putSerializable("kategorije", kategorije);
        fl.setArguments(argumenti);
        getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, fl).commit();
    }

    void promijeniKvizove(Kategorija kk) {
        if (imaInterneta) {
            if (kk.getNaziv().equals("Svi")) {
                zovniDajSveKvizove(true);
            } else {
                zovniDajSveKvizoveKategorija(kk);
            }
        } else {
            odabraniKvizovi.clear();
            if (kk.getNaziv().equals("Svi")) {
                odabraniKvizovi.addAll(kvizovi);
            } else {
                for (Kviz k : kvizovi) {
                    if (k.getKategorija().getNaziv().equals(kk.getNaziv())) {
                        odabraniKvizovi.add(k);
                    }
                }
            }
            FrameLayout d = (FrameLayout) findViewById(R.id.detailPlace);
            if (d == null) {
                lsAdapter.notifyDataSetChanged();
            } else {
                posaljiDedailFragment();
            }
        }
    }

    void zovniDajSveKvizove(boolean dodaj) {
        Intent intent1 = new Intent(Intent.ACTION_SYNC, null, this, DajSveKvizove.class);
        intent1.putExtra("receiver", nReceiver);
        intent1.putExtra("dodaj", dodaj);
        startService(intent1);
    }

    void zovniDajSveKvizoveKategorija(Kategorija k) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSveKvizoveKategorije.class);
        intent.putExtra("kategorija", k);
        intent.putExtra("receiver", mReceiver);
        startService(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("pozicija", pozicijaKategorija);
    }


    @Override
    public void onItemClickedGrid(int pos) {
        igrajKviz(pos);
    }

    @Override
    public void onItemLongClickedGrid(int pos) {
        prepraviKviz(pos, 2);
    }

    @Override
    public void dodajKvizGrid() {
        dodajKviz(2);
    }

    @Override
    public void onReceiveResultKvizoviKategorija(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                odabraniKvizovi.clear();
                ArrayList<Kviz> k2 = (ArrayList<Kviz>) resultData.get("kvizovi");
                odabraniKvizovi.addAll(k2);
                FrameLayout d = (FrameLayout) findViewById(R.id.detailPlace);
                if (d == null) {
                    lsAdapter.notifyDataSetChanged();
                } else {
                    posaljiDedailFragment();
                }

                if (ucitavanjeSvegaZbogBaze) {
                    popuniSvaPitanjaIzBaze();
                }

        }
    }

    private void prilagodiKategorije() {
        FrameLayout d = (FrameLayout) findViewById(R.id.detailPlace);
        if (d == null) {
            spAdapter.notifyDataSetChanged();
            spinner.setSelection(pozicijaKategorija);
        } else {
            posaljiListaFragment();
        }
        if (ucitavanjeSvegaZbogBaze) {
            zovniDajSveKvizove(false);
        } else {
            promijeniKvizove(kategorije.get(pozicijaKategorija));
        }

    }

    @Override
    public void onReceiveResultKategorije(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                kategorije.clear();
                kategorije.add(new Kategorija("Svi", "0"));
                ArrayList<Kategorija> k3 = (ArrayList<Kategorija>) resultData.get("kategorije");
                kategorije.addAll(k3);

                prilagodiKategorije();


        }
    }

    @Override
    public void onReceiveResultKvizovi(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                ArrayList<Kviz> k = (ArrayList<Kviz>) resultData.get("kvizovi");
                kvizovi.clear();
                kvizovi.addAll(k);

                if (resultData.getBoolean("dodaj") || (ucitavanjeSvegaZbogBaze && kategorije.get(pozicijaKategorija).getNaziv().equals("Svi"))) {
                    odabraniKvizovi.clear();
                    odabraniKvizovi.addAll(k);
                    FrameLayout d = (FrameLayout) findViewById(R.id.detailPlace);
                    if (d == null) {
                        lsAdapter.notifyDataSetChanged();
                    } else {
                        posaljiDedailFragment();
                    }
                    if (ucitavanjeSvegaZbogBaze && kategorije.get(pozicijaKategorija).getNaziv().equals("Svi")) {
                        popuniSvaPitanjaIzBaze();
                    }
                }
                if (ucitavanjeSvegaZbogBaze && !kategorije.get(pozicijaKategorija).getNaziv().equals("Svi")) {
                    promijeniKvizove(kategorije.get(pozicijaKategorija));
                }
                break;

        }
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
                    ucitavanjeSvegaZbogBaze = false;
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
}



