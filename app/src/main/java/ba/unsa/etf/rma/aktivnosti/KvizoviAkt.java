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
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();
    private SpinnerAdapter spAdapter;
    private ListaAdapter lsAdapter;
    private int pozicijaKliknutog;
    private int pozicijaKategorija = 0;

    private DajSveKvizoveKategorijaRec mReceiver;
    private DajSveKategorijeRec kReceiver;
    private DajSveKvizoveRec nReceiver;

    private void odaberiKvizove(Kategorija kategorija) {
        odabraniKvizovi.clear();
        if (kategorija.getNaziv().equalsIgnoreCase("Svi")) {
            odabraniKvizovi.addAll(kvizovi);
        } else {
            for (Kviz kviz : kvizovi)
                if (kviz.getKategorija().getNaziv().equalsIgnoreCase(kategorija.getNaziv()))
                    odabraniKvizovi.add(kviz);
        }

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

        if(savedInstanceState!=null){
            pozicijaKategorija = savedInstanceState.getInt("pozicija");
        }

        final Kategorija pocetna = new Kategorija("Svi", "0");

        FrameLayout detalji = (FrameLayout) findViewById(R.id.detailPlace);
        if (detalji != null) {
            odaberiKvizove(pocetna);
            posaljiDedailFragment();
            posaljiListaFragment();
        } else {
            spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);
            lista = (ListView) findViewById(R.id.lvKvizovi);

            spAdapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_1, kategorije);
            spinner.setAdapter(spAdapter);

            lsAdapter = new ListaAdapter(this, odabraniKvizovi, getResources());
            lista.setAdapter(lsAdapter);

            lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    pozicijaKliknutog = position;
                    prepraviKviz(position,1);
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

            //povlacenje kategorija
            popuniKategorijeIzBaze();

        }

        if (savedInstanceState != null) {
            kvizovi.clear();
            kategorije.clear();
            kvizovi.addAll((ArrayList<Kviz>) savedInstanceState.getSerializable("kvizovi"));
            odabraniKvizovi.addAll((ArrayList<Kviz>) savedInstanceState.getSerializable("odabraniKvizovi"));
            kategorije.addAll((ArrayList<Kategorija>) savedInstanceState.getSerializable("kategorije"));
            pozicijaKategorija = savedInstanceState.getInt("pozicija");
        }



    }

    private void popuniKategorijeIzBaze() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, KvizoviAkt.this, DajSveKategorije.class);
        intent.putExtra("receiver", kReceiver);
        startService(intent);
    }


    private void prepraviKviz(int position, int kod) {
        pozicijaKliknutog = position;
        Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
        myIntent.putExtra("idKviza", odabraniKvizovi.get(position).getId());
        KvizoviAkt.this.startActivityForResult(myIntent, kod);
    }

    private void igrajKviz(int position) {
        Intent myIntent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);
        myIntent.putExtra("kviz", odabraniKvizovi.get(position));
        KvizoviAkt.this.startActivity(myIntent);
    }

    private void dodajKviz(int kod) {
        Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
        myIntent.putExtra("idKviza", (String) null);
        if(kod == 1){
            myIntent.putExtra("oznacenaKategorija", (Kategorija) spinner.getSelectedItem());
        }
        else{
            myIntent.putExtra("oznacenaKategorija", kategorije.get(pozicijaKategorija));
        }
        KvizoviAkt.this.startActivityForResult(myIntent, kod);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FrameLayout d = (FrameLayout) findViewById(R.id.detailPlace);
        //mozda je doslo do promjene
        if(d == null){
            requestCode = 1;
        }
        else{
            requestCode = 2;
        }
        if (requestCode == 1 || requestCode == 2) { //kod 2 je za siroki ekran
            //vracanje iz DodajKvizAkt
            if (resultCode == Activity.RESULT_OK) {
                pozicijaKategorija = 0;
                popuniKategorijeIzBaze();

                /*if(requestCode == 1) {
                    lsAdapter.notifyDataSetChanged();
                    spAdapter.notifyDataSetChanged();
                    spinner.setSelection(0);
                }
                else{
                    //posalji sve opet u fragmente
                    posaljiDedailFragment();
                    posaljiListaFragment();
                }*/

            } else if (resultCode == Activity.RESULT_CANCELED) {
                //ako je bila dodana kategorija da se spasi
                popuniKategorijeIzBaze();
                pozicijaKategorija = 0;
                if(requestCode == 1) {
                    spAdapter.notifyDataSetChanged();
                    lsAdapter.notifyDataSetChanged();
                    spinner.setSelection(0);
                }
                else{
                    posaljiDedailFragment();
                    posaljiListaFragment();
                }
            }
        }
    }

    @Override
    public void onItemClicked(int pos) {
    //Priprema novog fragmenta FragmentDetalji
        pozicijaKategorija = pos;
        odaberiKvizove(kategorije.get(pos));
        posaljiDedailFragment();
    }

    void posaljiDedailFragment(){
        Bundle arguments = new Bundle();
        arguments.putSerializable("kvizovi", odabraniKvizovi);
        DetailFrag fd = new DetailFrag();
        fd.setArguments(arguments);
        getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, fd).commit();
    }

    void posaljiListaFragment(){
        ListaFrag fl = new ListaFrag();
        Bundle argumenti = new Bundle();
        argumenti.putSerializable("kategorije", kategorije);
        fl.setArguments(argumenti);
        getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, fl).commit();
    }

    void promijeniKvizove(Kategorija k){
        if(k.getNaziv().equals("Svi")){
            zovniDajSveKvizove(true);
        }
        else{
            zovniDajSveKvizoveKategorija(k);
        }
    }

    void zovniDajSveKvizove(boolean dodaj){
        Intent intent1 = new Intent(Intent.ACTION_SYNC, null, this, DajSveKvizove.class);
        intent1.putExtra("receiver", nReceiver);
        intent1.putExtra("dodaj",dodaj);
        startService(intent1);
    }

    void zovniDajSveKvizoveKategorija(Kategorija k){
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSveKvizoveKategorije.class);
        intent.putExtra("kategorija", k);
        intent.putExtra("receiver", mReceiver);
        startService(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("kategorije", kategorije);
        savedInstanceState.putSerializable("kvizovi", kvizovi);
        savedInstanceState.putSerializable("odabraniKvizovi", odabraniKvizovi);
        savedInstanceState.putInt("pozicija",pozicijaKategorija);
    }


    @Override
    public void onItemClickedGrid(int pos) {
        igrajKviz(pos);
    }

    @Override
    public void onItemLongClickedGrid(int pos) {
        prepraviKviz(pos,2);
    }

    @Override
    public void dodajKvizGrid() {
        dodajKviz(2);
    }

    @Override
    public void onReceiveResultKvizoviKategorija(int resultCode, Bundle resultData) {
        switch (resultCode) {

            case 2:

                ArrayList<Kviz> k2 = (ArrayList<Kviz>) resultData.get("kvizovi");
                odabraniKvizovi.clear();
                odabraniKvizovi.addAll(k2);
                lsAdapter.notifyDataSetChanged();


        }
    }

    @Override
    public void onReceiveResultKategorije(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 3:
                ArrayList<Kategorija> k3 = (ArrayList<Kategorija>) resultData.get("kategorije");
                kategorije.clear();
                kategorije.add(new Kategorija("Svi", "0"));
                kategorije.addAll(k3);
                spAdapter.notifyDataSetChanged();
                spinner.setSelection(pozicijaKategorija);

        }
    }

    @Override
    public void onReceiveResultKvizovi(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                ArrayList<Kviz> k = (ArrayList<Kviz>) resultData.get("kvizovi");
                kvizovi.clear();
                kvizovi.addAll(k);

                if(resultData.getBoolean("dodaj")){
                    System.out.println("idu svi");
                    odabraniKvizovi.clear();
                    odabraniKvizovi.addAll(k);
                    lsAdapter.notifyDataSetChanged();

                }

                break;

        }
    }


}



