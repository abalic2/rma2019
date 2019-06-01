package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import ba.unsa.etf.rma.intentServisi.DajKviz;
import ba.unsa.etf.rma.intentServisi.DajSvaPitanja;
import ba.unsa.etf.rma.intentServisi.DajSveKategorije;
import ba.unsa.etf.rma.intentServisi.DodajKviz;
import ba.unsa.etf.rma.intentServisi.EditKviz;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.receiveri.DajKvizRec;
import ba.unsa.etf.rma.receiveri.DajSvaPitanjaRec;
import ba.unsa.etf.rma.receiveri.DajSveKategorijeRec;
import ba.unsa.etf.rma.receiveri.DodajKvizRec;
import ba.unsa.etf.rma.receiveri.EditKvizRec;

public class DodajKvizAkt extends AppCompatActivity implements DajSveKategorijeRec.Receiver, DajKvizRec.Receiver,
        DajSvaPitanjaRec.Receiver, DodajKvizRec.Receiver, EditKvizRec.Receiver {

    private Spinner spinner;
    private SpinnerAdapter spAdapter;
    private ListView listaPitanja;
    private ListView listaMogucihPitanja;
    private PitanjaAdapter adapterPitanja;
    private PitanjaAdapter adapterMogucihPitanja;
    private EditText imeKviza;
    private Button dugme;
    private Button dugmeImportuj;


    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> pitanja = new ArrayList<>();

    private int pozicijaKliknutog;
    private String idKviza;
    private boolean mijenjanjeKategorije = false;
    private boolean dodavanjeNovogKviza = false;
    private Kategorija novaKategorija;

    private DajKvizRec rReceiver;
    private DajSveKategorijeRec kReceiver;
    private DajSvaPitanjaRec nReceiver;
    private DodajKvizRec mReceiver;
    private EditKvizRec oReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz);

        kReceiver = new DajSveKategorijeRec(new Handler());
        kReceiver.setReceiver(this);
        rReceiver = new DajKvizRec(new Handler());
        rReceiver.setReceiver(this);
        nReceiver = new DajSvaPitanjaRec(new Handler());
        nReceiver.setReceiver(this);
        mReceiver = new DodajKvizRec(new Handler());
        mReceiver.setReceiver(this);
        oReceiver = new EditKvizRec(new Handler());
        oReceiver.setReceiver(this);

        imeKviza = (EditText) findViewById(R.id.etNaziv);
        spinner = (Spinner) findViewById(R.id.spKategorije);
        listaPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
        listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
        dugme = (Button) findViewById(R.id.btnDodajKviz);
        dugmeImportuj = (Button) findViewById(R.id.btnImportKviz);

        idKviza = getIntent().getStringExtra("idKviza");
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
                    DodajKvizAkt.this.startActivityForResult(myIntent, 3);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


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
                DodajKvizAkt.this.startActivityForResult(myIntent, 2);
            }
        });

        dugme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jeLiSveValidno()) {
                    Kviz novi = new Kviz(imeKviza.getText().toString(), pitanja, (Kategorija) spinner.getSelectedItem(), idKviza);
                    if(dodavanjeNovogKviza){
                        dodajNoviKviz(novi);
                    }
                    else{
                        editujKviz(novi);
                    }

                }

            }
        });

        dugmeImportuj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                String[] mimeTypes = {"text/csv", "text/plain","text/comma-separated-values"};
                sendIntent.setType("*/*");
                sendIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(sendIntent, 44);
                }

            }

        });

        popuniKategorijeIzBaze();

    }

    private void dodajNoviKviz(Kviz kviz) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DodajKviz.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("kviz", kviz);
        startService(intent);
    }

    private void editujKviz(Kviz kviz) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, EditKviz.class);
        intent.putExtra("receiver", oReceiver);
        intent.putExtra("kviz", kviz);
        startService(intent);
    }

    private void ucitajKvizIzBaze(String idKviza) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajKviz.class);
        intent.putExtra("receiver", rReceiver);
        intent.putExtra("idKviza", idKviza);
        startService(intent);
    }

    @Override
    public void onReceiveResultKategorije(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 3:
                ArrayList<Kategorija> kategorijeIzBaze = (ArrayList<Kategorija>) resultData.get("kategorije");
                kategorije.clear();
                kategorije.add(new Kategorija("Svi", "0"));
                kategorije.addAll(kategorijeIzBaze);
                Kategorija dodaj = new Kategorija("Dodaj kategoriju",null);
                kategorije.add(dodaj);
                spAdapter.notifyDataSetChanged();

                if (idKviza == null) {
                    dodavanjeNovogKviza = true;
                } else {
                    dodavanjeNovogKviza = false;
                    ucitajKvizIzBaze(idKviza);
                }

                if(dodavanjeNovogKviza) {
                    Kategorija kategorija = (Kategorija) getIntent().getSerializableExtra("oznacenaKategorija");
                    int brojac = 0;
                    for (Kategorija k : kategorije) {
                        if (k.getNaziv().equals(kategorija.getNaziv())) break;
                        brojac++;
                    }
                    spinner.setSelection(brojac);

                    ucitajMogucaPitanja();
                }


        }
    }

    @Override
    public void onReceiveResultKviz(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                Kviz kviz = (Kviz) resultData.getSerializable("kviz");
                ArrayList<Pitanje> m = (ArrayList<Pitanje>) resultData.getSerializable("mogucaPitanja");

                imeKviza.setText(kviz.getNaziv());
                pitanja.clear();
                pitanja.addAll(kviz.getPitanja());
                adapterPitanja.notifyDataSetChanged();

                mogucaPitanja.clear();
                mogucaPitanja.addAll(m);
                adapterMogucihPitanja.notifyDataSetChanged();

                if(mijenjanjeKategorije) {
                    int brojac = 0;
                    for (Kategorija k : kategorije) {
                        if (k.getNaziv().equals(novaKategorija.getNaziv())) break;
                        brojac++;
                    }
                    spinner.setSelection(brojac);
                }
                else {
                    //da namjesti na njegovu kategoriju
                    int indeks = 0;
                    for (Kategorija k : kategorije) {
                        if (k.getNaziv().equals(kviz.getKategorija().getNaziv())) break;
                        indeks++;
                    }
                    spinner.setSelection(indeks);
                }


        }
    }

    private void ucitajMogucaPitanja() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSvaPitanja.class);
        intent.putExtra("receiver", nReceiver);
        startService(intent);
    }

    private boolean jeLiSveValidno() {
        boolean imaGreska = false;
        imeKviza.setBackground(getResources().getDrawable(R.drawable.bijela_okvir));
        String naziv = imeKviza.getText().toString();
        if (naziv.trim().equals("")) {
            imaGreska = true;
            imeKviza.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
        }
        return !imaGreska;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            //povratak iz DodajPitanjaAkt
            if (resultCode == Activity.RESULT_OK) {
                Pitanje novoPitanje = (Pitanje) data.getSerializableExtra("novoPitanje");
                pitanja.add(novoPitanje);
                adapterPitanja.notifyDataSetChanged();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        } else if (requestCode == 3) {
            //povratak iz DodajKategorijuAkt
            if (resultCode == Activity.RESULT_OK) {
                mijenjanjeKategorije = true;
                novaKategorija = (Kategorija) data.getSerializableExtra("novaKategorija");
                popuniKategorijeIzBaze();

            } else {
                spinner.setSelection(0);
            }
        } else if (requestCode == 44) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String datoteka = vratiStringizCVSa(data);
                    Kviz noviKviz = vratiKvizIzDatoteke(datoteka);
                    if (noviKviz != null) {
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
        alertDialog.setMessage(poruka);
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

    private void popuniKategorijeIzBaze() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DajSveKategorije.class);
        intent.putExtra("receiver", kReceiver);
        startService(intent);
    }

    private Kviz vratiKvizIzDatoteke(String datoteka) {
        /*Kviz noviKviz = new Kviz();
        String imeKviza = new String();
        Kategorija kategorija = new Kategorija();
        ArrayList<Pitanje> pitanja = new ArrayList<>();
        String[] linije = datoteka.split("\n");
        boolean imaKategorija = false;
        int brReda = 0;
        for (String linija : linije) {
            String[] elementi = linija.split(",");
            if (brReda == 0) {
                if (elementi.length != 3) {
                    prikaziAlertdialog("Datoteka kviza kojeg importujete nema ispravan format!");
                    return null;
                }
                //ima ll vec imena
                for (Kviz k : kvizovi) {
                    if (k.getNaziv().equals(elementi[0])) {
                        prikaziAlertdialog("Kviz kojeg importujete već postoji!");
                        return null;
                    }
                }
                imeKviza = elementi[0];
                if (!jeLiiInt(elementi[2])) {
                    prikaziAlertdialog("Datoteka kviza kojeg importujete nema ispravan format!");
                    return null;
                }
                if (linije.length - 1 != Integer.parseInt(elementi[2].trim())) {
                    prikaziAlertdialog("Kviz kojeg imporujete ima neispravan broj pitanja!");
                    return null;
                }
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
                //mora imati bar ime pitanja, broj odgovora, i jedan tacan odg
                if(elementi.length < 4){
                    prikaziAlertdialog("Datoteka kviza kojeg importujete nema ispravan format!");
                    return null;
                }
                ArrayList<String> odgovori = new ArrayList<>();
                Pitanje pitanje = new Pitanje();
                String tekstPitanja = elementi[0];
                for (Pitanje p : pitanja) {
                    if (p.getNaziv().equals(tekstPitanja)) {
                        prikaziAlertdialog("Kviz nije ispravan postoje dva pitanja sa istim nazivom!");
                        return null;
                    }
                }
                pitanje.setNaziv(tekstPitanja);
                pitanje.setTekstPitanja(tekstPitanja);

                if (!jeLiiInt(elementi[1])) {
                    prikaziAlertdialog("Datoteka kviza kojeg importujete nema ispravan format!");
                    return null;
                }
                int brojOdgovora = Integer.parseInt(elementi[1].trim());
                if (brojOdgovora != elementi.length - 3) {
                    prikaziAlertdialog("Kviz kojeg importujete ima neispravan broj odgovora!");
                    return null;
                }
                //indeks tacnog odgovora
                if (!jeLiiInt(elementi[2])) {
                    prikaziAlertdialog("Datoteka kviza kojeg importujete nema ispravan format!");
                    return null;
                }
                int indexTacnog = Integer.parseInt(elementi[2].trim());
                if (indexTacnog < 0 || indexTacnog >= brojOdgovora) {
                    prikaziAlertdialog("Kviz kojeg importujete ima neispravan\n" +
                            "index tačnog odgovora!");
                    return null;
                }

                for (int i = 3; i < brojOdgovora + 3; i++) {
                    if (!odgovori.contains(elementi[i])) {
                        odgovori.add(elementi[i]);
                    } else {
                        prikaziAlertdialog("Kviz kojeg importujete nije ispravan postoji ponavljanje odgovora!");
                        return null;
                    }
                }
                pitanje.setTacan(elementi[indexTacnog + 3]);
                pitanje.setOdgovori(odgovori);
                pitanja.add(pitanje);
            }
            brReda++;
        }
        noviKviz.setNaziv(imeKviza);
        noviKviz.setKategorija(kategorija);
        if (!imaKategorija) {
            int n = kategorije.size();
            kategorije.add(n - 1, kategorija);
            spAdapter.notifyDataSetChanged();
        }
        noviKviz.setPitanja(pitanja);
        return noviKviz;*/
        return null;
    }

    private static boolean jeLiiInt(String str) {
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onReceiveResultPitanja(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                mogucaPitanja.clear();
                ArrayList<Pitanje> p = (ArrayList<Pitanje>) resultData.get("pitanja");
                mogucaPitanja.addAll(p);

                adapterMogucihPitanja.notifyDataSetChanged();


        }
    }

    @Override
    public void onReceiveResultNoviKviz(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                Intent myIntent = new Intent();
                setResult(Activity.RESULT_OK, myIntent);
                finish();
                break;
            case 2:
                imeKviza.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                prikaziAlertdialog("Kviz sa tim nazivom vec postoji!");
                break;
        }

    }

    @Override
    public void onReceiveResultEditKviz(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                Intent myIntent = new Intent();
                setResult(Activity.RESULT_OK, myIntent);
                finish();
                break;
            case 2:
                imeKviza.setBackground(getResources().getDrawable(R.drawable.crvena_okvir));
                prikaziAlertdialog("Kviz sa tim nazivom vec postoji!");
                break;
        }
    }
}
