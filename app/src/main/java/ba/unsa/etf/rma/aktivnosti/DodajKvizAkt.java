package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.PitanjaAdapter;
import ba.unsa.etf.rma.adapteri.SpinnerAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajKvizAkt extends AppCompatActivity {

    private Spinner spinner;
    private SpinnerAdapter spAdapter;
    private ListView listaPitanja;
    private ListView listaMogucihPitanja;
    private PitanjaAdapter adapterPitanja;
    private PitanjaAdapter adapterMogucihPitanja;
    private ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> pitanja = new ArrayList<>();
    private EditText imeKviza;
    private Button dugme;
    private Button dugmeImportuj;
    private boolean dodavanjeNovogKviza = false;
    private ArrayList<Kategorija> kategorije;
    private ArrayList<Kviz> kvizovi;
    private int pozicijaKliknutog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz);

        imeKviza = (EditText) findViewById(R.id.etNaziv);
        spinner = (Spinner) findViewById(R.id.spKategorije);
        listaPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
        listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
        dugme = (Button) findViewById(R.id.btnDodajKviz);
        dugmeImportuj = (Button) findViewById(R.id.btnImportKviz);

        kategorije = (ArrayList<Kategorija>) getIntent().getSerializableExtra("kategorije");
        kvizovi = (ArrayList<Kviz>) getIntent().getSerializableExtra("kvizovi");
        pozicijaKliknutog = getIntent().getExtras().getInt("redniBroj");
        Kategorija dodaj = new Kategorija();
        dodaj.setNaziv("Dodaj kategoriju");
        kategorije.add(dodaj);
        final Kviz kviz = (Kviz) getIntent().getSerializableExtra("kviz");
        imeKviza.setText("");

        adapterPitanja = new PitanjaAdapter(this, pitanja, getResources());
        listaPitanja.setAdapter(adapterPitanja);
        adapterMogucihPitanja = new PitanjaAdapter(this, mogucaPitanja, getResources());
        listaMogucihPitanja.setAdapter(adapterMogucihPitanja);

        spAdapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_1, kategorije);
        spinner.setAdapter(spAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (kategorije.get(position).getNaziv().equalsIgnoreCase("Dodaj kategoriju")) {
                    Intent myIntent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
                    myIntent.putExtra("kategorije", kategorije);
                    DodajKvizAkt.this.startActivityForResult(myIntent, 3);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (kviz == null) {
            dodavanjeNovogKviza = true;
            Kategorija odabrana = (Kategorija) getIntent().getSerializableExtra("oznacenaKategorija");
            int indeks = 0;
            //da namjesti na kateogriju koja je bila u proslom prozoru
            for (Kategorija k : kategorije) {
                if (k.getNaziv().equalsIgnoreCase(odabrana.getNaziv())) break;
                indeks++;
            }
            spinner.setSelection(indeks);
        } else {
            imeKviza.setText(kviz.getNaziv());
            pitanja.addAll(kviz.getPitanja());
            //da namjesti na njegovu kategoriju
            int indeks = 0;
            for (Kategorija k : kategorije) {
                if (k.getNaziv().equalsIgnoreCase(kviz.getKategorija().getNaziv())) break;
                indeks++;
            }
            spinner.setSelection(indeks);
        }

        listaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pitanje prebaci = pitanja.get(position);
                pitanja.remove(prebaci);
                mogucaPitanja.add(prebaci);
                adapterMogucihPitanja.notifyDataSetChanged();
                adapterPitanja.notifyDataSetChanged();
            }
        });

        listaMogucihPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pitanje prebaci = mogucaPitanja.get(position);
                pitanja.add(prebaci);
                mogucaPitanja.remove(prebaci);
                adapterMogucihPitanja.notifyDataSetChanged();
                adapterPitanja.notifyDataSetChanged();
            }
        });

        View footer = getLayoutInflater().inflate(R.layout.footer_pitanja, null);
        listaPitanja.addFooterView(footer);

        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dodavanje pitanja
                Intent myIntent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                myIntent.putExtra("listaPitanja", pitanja);
                myIntent.putExtra("listaMogucihPitanja", mogucaPitanja);
                DodajKvizAkt.this.startActivityForResult(myIntent, 2);
            }
        });

        dugme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jeLiSveValidno()) {
                    Kviz novi = new Kviz(imeKviza.getText().toString(), pitanja, (Kategorija) spinner.getSelectedItem());
                    kategorije.remove(kategorije.size() - 1);
                    Intent myIntent = new Intent();
                    myIntent.putExtra("kviz", novi);
                    myIntent.putExtra("jeLiNovi", dodavanjeNovogKviza);
                    myIntent.putExtra("kategorije", kategorije);
                    setResult(Activity.RESULT_OK, myIntent);
                    finish();
                }

            }
        });

        dugmeImportuj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                String [] mimeTypes = {"text/csv", "text/plain"};
                sendIntent.setType("*/*");
                sendIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(sendIntent, 44);
                }

            }

        });

    }

    private boolean jeLiSveValidno() {
        boolean imaGreska = false;
        imeKviza.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
        String naziv = imeKviza.getText().toString();
        if (naziv.trim().equals("")) {
            imaGreska = true;
            imeKviza.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
        }
        ArrayList<Kviz> kk = new ArrayList<>();
        kk.addAll(kvizovi);
        if (!dodavanjeNovogKviza) kk.remove(pozicijaKliknutog); //da ne gleda sebe
        for (Kviz k : kk) {
            if (k.getNaziv().equals(naziv)) {
                imaGreska = true;
                imeKviza.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
            }
        }
        return !imaGreska;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            //povratak iz DodajPitanjaAkt
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<Pitanje> vracenaPitanja = (ArrayList<Pitanje>) data.getSerializableExtra("pitanja");
                pitanja.clear();
                pitanja.addAll(vracenaPitanja);
                adapterPitanja.notifyDataSetChanged();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        } else if (requestCode == 3) {
            //povratak iz DodajKategorijuAkt
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<Kategorija> vraceneKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                kategorije.clear();
                kategorije.addAll(vraceneKategorije);
                spAdapter.notifyDataSetChanged();
                spinner.setSelection(kategorije.size() - 2);
            } else {
                spinner.setSelection(0);
            }
        } else if (requestCode == 44) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String datoteka = vratiStringizCVSa(data);
                    Kviz noviKviz = vratiKvizIzDatoteke(datoteka);
                    if(noviKviz != null) {
                        int indeks = 0;
                        for (Kategorija k : kategorije) {
                            if (k.getNaziv().equals(noviKviz.getKategorija().getNaziv())) {
                                spinner.setSelection(indeks);
                            }
                            indeks++;
                        }
                        imeKviza.setText(noviKviz.getNaziv());
                        pitanja.clear();
                        mogucaPitanja.clear();
                        pitanja.addAll(noviKviz.getPitanja());
                        adapterPitanja.notifyDataSetChanged();
                        adapterMogucihPitanja.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        //ako pritisne back da moze ipak dodane kategorije prenijet
        kategorije.remove(kategorije.size() - 1);
        Intent myIntent = new Intent();
        myIntent.putExtra("kategorije", kategorije);
        setResult(Activity.RESULT_CANCELED, myIntent);
        super.onBackPressed();
    }

    private void prikaziAlertdialog(String poruka) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(poruka);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private String vratiStringizCVSa(Intent data) {
        Uri uri = data.getData();
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String rezultat = "";
            String line;
            do {
                line = reader.readLine();
                if (line == null) break;
                rezultat = rezultat + line + "\n";
            } while (true);
            if (inputStream != null) {
                inputStream.close();
            }
            return rezultat;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Kviz vratiKvizIzDatoteke(String datoteka){
        Kviz noviKviz = new Kviz();
        String imeKviza = new String();
        Kategorija kategorija = new Kategorija();
        ArrayList<Pitanje> pitanja = new ArrayList<>();
        String[] linije = datoteka.split("\n");
        int brReda = 0;
        for (String linija : linije) {
            String[] elementi = linija.split(",");
            if (brReda == 0) {
                //ima ll vec imena
                for (Kviz k : kvizovi) {
                    if (k.getNaziv().equals(elementi[0])) {
                        prikaziAlertdialog("Kviz kojeg importujete već postoji!");
                        return null;
                    }
                }
                imeKviza = elementi[0];

                if (linije.length - 1 != Integer.parseInt(elementi[2])) {
                    prikaziAlertdialog("Kviz kojeg imporujete ima neispravan broj pitanja!");
                    return null;
                }

                boolean imaKategorija = false;
                for (Kategorija k : kategorije) {
                    if (k.getNaziv().equals(elementi[1])) {
                        kategorija.setId(k.getId());
                        kategorija.setNaziv(k.getNaziv());
                        imaKategorija = true;
                        break;
                    }
                }
                if (!imaKategorija) {
                    kategorija.setNaziv(elementi[1]);
                    kategorija.setId("25");
                }
            } else {
                ArrayList<String> odgovori = new ArrayList<>();
                Pitanje pitanje = new Pitanje();
                pitanje.setNaziv(elementi[0]);
                pitanje.setTekstPitanja(elementi[0]);
                int brojOdgovora = Integer.parseInt(elementi[1]);
                if(brojOdgovora != elementi.length - 3) {
                    prikaziAlertdialog("Kviz kojeg importujete ima neispravan broj odgovora!");
                    return null;
                }
                //indeks tacnog odgovora
                int indexTacnog = Integer.parseInt(elementi[2]);
                if(indexTacnog < 0 || indexTacnog >= brojOdgovora ) {
                    prikaziAlertdialog("Kviz kojeg importujete ima neispravan\n" +
                            "index tačnog odgovora!");
                    return null;
                }
                pitanje.setTacan(elementi[indexTacnog+3]);
                for (int i = 3; i < brojOdgovora + 3; i++)
                    odgovori.add(elementi[i]);
                pitanje.setOdgovori(odgovori);
                pitanja.add(pitanje);
            }
            brReda++;
        }
        noviKviz.setNaziv(imeKviza);
        noviKviz.setKategorija(kategorija);
        if(!kategorije.contains(kategorija)){
            int n = kategorije.size();
            kategorije.add(n-1,kategorija);
            spAdapter.notifyDataSetChanged();
        }
        noviKviz.setPitanja(pitanja);
        return noviKviz;
    }
}
