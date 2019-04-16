package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
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

public class KvizoviAkt extends AppCompatActivity implements ListaFrag.OnItemClick {
    private Spinner spinner;
    private ListView lista;
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();
    private SpinnerAdapter spAdapter;
    private ListaAdapter lsAdapter;
    private int pozicijaKliknutog;
    private boolean siriL = false;

    private void odaberiKvizove(Kategorija kategorija) {
        odabraniKvizovi.clear();
        if (kategorija.getNaziv().equalsIgnoreCase("Svi")) {
            odabraniKvizovi.addAll(kvizovi);
        } else {
            for (Kviz kviz : kvizovi)
                if (kviz.getKategorija().getNaziv().equalsIgnoreCase(kategorija.getNaziv()))
                    odabraniKvizovi.add(kviz);
        }
        lsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Kategorija pocetna = new Kategorija("Svi", "0");
        kategorije.add(pocetna);

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
                Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                myIntent.putExtra("kviz", odabraniKvizovi.get(position));
                myIntent.putExtra("kategorije", kategorije);
                myIntent.putExtra("redniBroj", pozicijaKliknutog);
                myIntent.putExtra("kvizovi", kvizovi);
                KvizoviAkt.this.startActivityForResult(myIntent, 1);
                return true;
            }
        });

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);
                myIntent.putExtra("kviz", odabraniKvizovi.get(position));
                KvizoviAkt.this.startActivity(myIntent);
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                odaberiKvizove(kategorije.get(position));
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
                Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                myIntent.putExtra("kviz", (Kviz) null);
                myIntent.putExtra("kategorije", kategorije);
                myIntent.putExtra("kvizovi", kvizovi);
                myIntent.putExtra("oznacenaKategorija", (Kategorija) spinner.getSelectedItem());
                KvizoviAkt.this.startActivityForResult(myIntent, 1);
            }
        });

        /*FrameLayout detalji = (FrameLayout)findViewById(R.id.detailPlace);
        if(detalji != null){
            siriL=true;
            DetailFrag fd = (DetailFrag) getSupportFragmentManager().findFragmentById(R.id.detailPlace);
            if(fd==null) {
                fd = new DetailFrag();
                getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, fd).commit();
            }
        }
        ListaFrag fl = (ListaFrag) getSupportFragmentManager().findFragmentById(R.id.listPlace);
        if(fl==null){
            fl = new ListaFrag();
            Bundle argumenti=new Bundle();
            argumenti.putSerializable("kategorije",kategorije);
            fl.setArguments(argumenti);
            getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, fl).commit();
        }else{
            getSupportFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }*/



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            //vracanje iz DodajKvizAkt
            if (resultCode == Activity.RESULT_OK) {
                Kviz vraceniKviz = (Kviz) data.getSerializableExtra("kviz");
                boolean jeLiNoviDodan = data.getExtras().getBoolean("jeLiNovi");
                ArrayList<Kategorija> vraceneKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                kategorije.clear();
                kategorije.addAll(vraceneKategorije);
                spAdapter.notifyDataSetChanged();
                if (jeLiNoviDodan) {
                    kvizovi.add(vraceniKviz);
                } else {
                    odabraniKvizovi.get(pozicijaKliknutog).setNaziv(vraceniKviz.getNaziv());
                    odabraniKvizovi.get(pozicijaKliknutog).setPitanja(vraceniKviz.getPitanja());
                    odabraniKvizovi.get(pozicijaKliknutog).setKategorija(vraceniKviz.getKategorija());
                }
                odaberiKvizove(kategorije.get(0));
                lsAdapter.notifyDataSetChanged();
                spinner.setSelection(0);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //ako je bila dodana kategorija da se spasi
                ArrayList<Kategorija> vraceneKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                kategorije.clear();
                kategorije.addAll(vraceneKategorije);
                spAdapter.notifyDataSetChanged();
                odaberiKvizove(kategorije.get(0));
                lsAdapter.notifyDataSetChanged();
                spinner.setSelection(0);
            }
        }
    }

    @Override
    public void onItemClicked(int pos) {
//Priprema novog fragmenta FragmentDetalji
        Bundle arguments = new Bundle();
        arguments.putSerializable("kategorija", kategorije.get(pos));
        arguments.putSerializable("kvizovi",kvizovi);
        DetailFrag fd = new DetailFrag();
        fd.setArguments(arguments);
        if (siriL) {
            getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, fd).commit();
        } else {
            //????
            //getFragmentManager().beginTransaction().replace(R.id.mjestoF1, fd).addToBackStack(null).commit();
        }
    }


}
