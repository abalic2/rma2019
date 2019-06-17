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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.SQLiteBaza;
import ba.unsa.etf.rma.intentServisi.DajSvaPitanja;
import ba.unsa.etf.rma.intentServisi.DajSveIzRangListe;
import ba.unsa.etf.rma.intentServisi.DajSveKategorije;
import ba.unsa.etf.rma.intentServisi.DajSveKvizove;
import ba.unsa.etf.rma.intentServisi.DodajKategoriju;
import ba.unsa.etf.rma.intentServisi.DodajURangListuVise;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.receiveri.DajSvaPitanjaRec;
import ba.unsa.etf.rma.receiveri.DajSveIzRangListeRec;
import ba.unsa.etf.rma.receiveri.DajSveKategorijeRec;
import ba.unsa.etf.rma.receiveri.DajSveKvizoveRec;
import ba.unsa.etf.rma.receiveri.DodajKategorijuRec;
import ba.unsa.etf.rma.receiveri.DodajURangListuViseRec;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback, DodajKategorijuRec.Receiver,
        DajSveKategorijeRec.Receiver, DajSveKvizoveRec.Receiver, DajSvaPitanjaRec.Receiver,
        DajSveIzRangListeRec.Receiver, DodajURangListuViseRec.Receiver {
    private EditText nazivKategorije;
    private EditText ikona;
    private Button dodajIkonu;
    private Button dodajKategoriju;

    private Icon[] selectedIcons;

    private Kategorija novaKategorija;

    private DodajKategorijuRec mReceiver;

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

    private void popuniKategorijeIzBaze() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSveKategorije.class);
        intent.putExtra("receiver", kReceiver);
        startService(intent);
    }

    private void ucitajRangListu(){
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSveIzRangListe.class);
        intent.putExtra("receiver", rReceiver);
        intent.putExtra("kvizovi", kvizovi);
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
        setContentView(R.layout.activity_dodaj_kategoriju);

        mReceiver = new DodajKategorijuRec(new Handler());
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

        novaKategorija = new Kategorija();

        nazivKategorije = (EditText) findViewById(R.id.etNaziv);
        ikona = (EditText) findViewById(R.id.etIkona);
        dodajIkonu = (Button) findViewById(R.id.btnDodajIkonu);
        ikona.setText("");
        ikona.setEnabled(false);
        dodajKategoriju = (Button) findViewById(R.id.btnDodajKategoriju);

        nazivKategorije.setText("");

        kvizovi.clear();
        SQLiteBaza baza = new SQLiteBaza(this);
        kvizovi.addAll(baza.dajSveKvizove());
        popuniRangListuIzSQLite();

        final IconDialog iconDialog = new IconDialog();
        dodajIkonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconDialog.setSelectedIcons(selectedIcons);
                iconDialog.show(getSupportFragmentManager(), "icon_dialog");
            }
        });
        dodajKategoriju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imaInterneta) {
                    if (jeLiSveValidno()) {
                        novaKategorija.setNaziv(nazivKategorije.getText().toString());
                        novaKategorija.setId(ikona.getText().toString());
                        dodajKategorijuUBazu(novaKategorija);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Nema interneta", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void dodajKategorijuUBazu(Kategorija novaKategorija) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DodajKategoriju.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("kategorija", novaKategorija);
        startService(intent);
    }

    private boolean jeLiSveValidno() {
        postaviBoje();
        boolean nemaGreska = true;
        String ime = nazivKategorije.getText().toString();
        if (ime.trim().equals("")) {
            nemaGreska = false;
            nazivKategorije.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
        }

        if (ikona.getText().toString().equals("")) {
            ikona.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
            nemaGreska = false;
        }
        return nemaGreska;
    }

    private void postaviBoje() {
        nazivKategorije.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
        ikona.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        ikona.setText(String.valueOf(selectedIcons[0].getId()));
        ikona.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
    }

    @Override
    public void onReceiveResultNovaKategorija(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:

                SQLiteBaza baza = new SQLiteBaza(DodajKategorijuAkt.this);
                baza.ubaciJednuKategoriju(novaKategorija);

                Intent myIntent = new Intent();
                myIntent.putExtra("novaKategorija", novaKategorija);
                setResult(Activity.RESULT_OK, myIntent);
                finish();
                break;
            case 2:
                nazivKategorije.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                prikaziAlertdialog("Unesena kategorija već postoji!”");
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
