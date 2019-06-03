package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.ListaAdapter;
import ba.unsa.etf.rma.adapteri.SpinnerAdapter;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.intentServisi.DajSveKategorije;
import ba.unsa.etf.rma.intentServisi.DajSveKvizove;
import ba.unsa.etf.rma.intentServisi.DajSveKvizoveKategorije;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.receiveri.DajSveKategorijeRec;
import ba.unsa.etf.rma.receiveri.DajSveKvizoveKategorijaRec;
import ba.unsa.etf.rma.receiveri.DajSveKvizoveRec;

public class KvizoviAkt extends AppCompatActivity implements ListaFrag.OnItemClick, DetailFrag.OnItemClick,
        DajSveKvizoveKategorijaRec.Receiver, DajSveKategorijeRec.Receiver, DajSveKvizoveRec.Receiver {

    private Spinner spinner;
    private ListView lista;
    private SpinnerAdapter spAdapter;
    private ListaAdapter lsAdapter;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();

    private int pozicijaKategorija = 0;

    private DajSveKvizoveKategorijaRec mReceiver;
    private DajSveKategorijeRec kReceiver;
    private DajSveKvizoveRec nReceiver;

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

        //povlacenje kategorija
        popuniKategorijeIzBaze();

        if (savedInstanceState != null) {
            pozicijaKategorija = savedInstanceState.getInt("pozicija");
        }


    }

    private void popuniKategorijeIzBaze() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, KvizoviAkt.this, DajSveKategorije.class);
        intent.putExtra("receiver", kReceiver);
        startService(intent);
    }


    private void prepraviKviz(int position, int kod) {
        Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
        myIntent.putExtra("idKviza", odabraniKvizovi.get(position).getId());
        KvizoviAkt.this.startActivityForResult(myIntent, kod);
    }

    private void igrajKviz(int position) {
        Intent myIntent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);
        myIntent.putExtra("kviz", odabraniKvizovi.get(position));
        KvizoviAkt.this.startActivityForResult(myIntent, 3);
    }

    private void dodajKviz(int kod) {
        Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
        myIntent.putExtra("idKviza", (String) null);
        if (kod == 1) {
            myIntent.putExtra("oznacenaKategorija", (Kategorija) spinner.getSelectedItem());
        } else {
            myIntent.putExtra("oznacenaKategorija", kategorije.get(pozicijaKategorija));
        }
        KvizoviAkt.this.startActivityForResult(myIntent, kod);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            pozicijaKategorija = 0;
            popuniKategorijeIzBaze();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            popuniKategorijeIzBaze();
            pozicijaKategorija = 0;
        }

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

    void promijeniKvizove(Kategorija k) {
        if (k.getNaziv().equals("Svi")) {
            zovniDajSveKvizove(true);
        } else {
            zovniDajSveKvizoveKategorija(k);
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
                ArrayList<Kviz> k2 = (ArrayList<Kviz>) resultData.get("kvizovi");
                odabraniKvizovi.clear();
                odabraniKvizovi.addAll(k2);
                FrameLayout d = (FrameLayout) findViewById(R.id.detailPlace);
                if (d == null) {
                    lsAdapter.notifyDataSetChanged();
                } else {
                    posaljiDedailFragment();
                }

        }
    }

    @Override
    public void onReceiveResultKategorije(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                ArrayList<Kategorija> k3 = (ArrayList<Kategorija>) resultData.get("kategorije");
                kategorije.clear();
                kategorije.add(new Kategorija("Svi", "0"));
                kategorije.addAll(k3);

                FrameLayout d = (FrameLayout) findViewById(R.id.detailPlace);
                if (d == null) {
                    spAdapter.notifyDataSetChanged();
                    spinner.setSelection(pozicijaKategorija);
                } else {
                    posaljiListaFragment();
                }
                promijeniKvizove(kategorije.get(pozicijaKategorija));

        }
    }

    @Override
    public void onReceiveResultKvizovi(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                ArrayList<Kviz> k = (ArrayList<Kviz>) resultData.get("kvizovi");
                kvizovi.clear();
                kvizovi.addAll(k);

                if (resultData.getBoolean("dodaj")) {
                    odabraniKvizovi.clear();
                    odabraniKvizovi.addAll(k);

                    FrameLayout d = (FrameLayout) findViewById(R.id.detailPlace);
                    if (d == null) {
                        lsAdapter.notifyDataSetChanged();
                    } else {
                        posaljiDedailFragment();
                    }
                }

                break;

        }
    }


}



