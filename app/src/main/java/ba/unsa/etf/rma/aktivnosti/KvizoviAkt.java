package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;

public class KvizoviAkt extends AppCompatActivity implements ListaFrag.OnItemClick, DetailFrag.OnItemClick {
    private Spinner spinner;
    private ListView lista;
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();
    private SpinnerAdapter spAdapter;
    private ListaAdapter lsAdapter;
    private int pozicijaKliknutog;
    private int pozicijaKategorija = 0;

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

        if(savedInstanceState!=null){
            pozicijaKategorija = savedInstanceState.getInt("pozicija");
        }

        final Kategorija pocetna = new Kategorija("Svi", "0");
        kategorije.add(pocetna);

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
            spinner.setSelection(pozicijaKategorija);

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
                    odaberiKvizove(kategorije.get(position));
                    lsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


            View footer = getLayoutInflater().inflate(R.layout.footer_liste, null);
            lista.addFooterView(footer);

            //za dodavanje kviza
            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dodajKviz(1);
                }
            });

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

    private void prepraviKviz(int position, int kod) {
        pozicijaKliknutog = position;
        Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
        myIntent.putExtra("kviz", odabraniKvizovi.get(position));
        myIntent.putExtra("kategorije", kategorije);
        myIntent.putExtra("redniBroj", pozicijaKliknutog);
        myIntent.putExtra("kvizovi", kvizovi);
        KvizoviAkt.this.startActivityForResult(myIntent, kod);
    }

    private void igrajKviz(int position) {
        Intent myIntent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);
        myIntent.putExtra("kviz", odabraniKvizovi.get(position));
        KvizoviAkt.this.startActivity(myIntent);
    }

    private void dodajKviz(int kod) {
        Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
        myIntent.putExtra("kviz", (Kviz) null);
        myIntent.putExtra("kategorije", kategorije);
        myIntent.putExtra("kvizovi", kvizovi);
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
        if (requestCode == 1 || requestCode == 2) { //kod 2 je za siroki ekran
            //vracanje iz DodajKvizAkt
            if (resultCode == Activity.RESULT_OK) {
                Kviz vraceniKviz = (Kviz) data.getSerializableExtra("kviz");
                boolean jeLiNoviDodan = data.getExtras().getBoolean("jeLiNovi");
                ArrayList<Kategorija> vraceneKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                kategorije.clear();
                kategorije.addAll(vraceneKategorije);
                if (jeLiNoviDodan) {
                    kvizovi.add(vraceniKviz);
                } else {
                    odabraniKvizovi.get(pozicijaKliknutog).setNaziv(vraceniKviz.getNaziv());
                    odabraniKvizovi.get(pozicijaKliknutog).setPitanja(vraceniKviz.getPitanja());
                    odabraniKvizovi.get(pozicijaKliknutog).setKategorija(vraceniKviz.getKategorija());
                }
                odaberiKvizove(kategorije.get(0));
                if(requestCode == 1) {
                    lsAdapter.notifyDataSetChanged();
                    spAdapter.notifyDataSetChanged();
                    spinner.setSelection(0);
                }
                else{
                    //posalji sve opet u fragmente
                    posaljiDedailFragment();
                    posaljiListaFragment();
                }
                pozicijaKategorija = 0;
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //ako je bila dodana kategorija da se spasi
                ArrayList<Kategorija> vraceneKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                kategorije.clear();
                kategorije.addAll(vraceneKategorije);
                odaberiKvizove(kategorije.get(0));
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
}
