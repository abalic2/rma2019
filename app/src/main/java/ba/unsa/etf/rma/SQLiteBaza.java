package ba.unsa.etf.rma;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class SQLiteBaza {
    Context context;
    KvizoviDBOpenHelper helper;

    public SQLiteBaza(Context context) {
        this.context = context;
        helper = new KvizoviDBOpenHelper(context, KvizoviDBOpenHelper.DATABASE_NAME, null,
                KvizoviDBOpenHelper.DATABASE_VERSION);
    }

    public void ubaciKategorije(ArrayList<Kategorija> kategorije) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_KATEGORIJE, null, null);
        db.close();

        for (Kategorija k : kategorije) {
            ubaciJednuKategoriju(k);
        }

        return;
    }

    public void ubaciJednuKategoriju(Kategorija k){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues novaKategorija = new ContentValues();
        novaKategorija.put(KvizoviDBOpenHelper.KATEGORIJA_NAZIV, k.getNaziv());
        novaKategorija.put(KvizoviDBOpenHelper.KATEGORIJA_IDIKONICE, k.getId());
        db.insert(KvizoviDBOpenHelper.DATABASE_TABLE_KATEGORIJE, null, novaKategorija);

        db.close();

    }

    public ArrayList<Kategorija> dajSveKategorije(){
        ArrayList<Kategorija> kategorije = new ArrayList<>();

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] koloneRezultat = new String[]{KvizoviDBOpenHelper.KATEGORIJA_NAZIV, KvizoviDBOpenHelper.KATEGORIJA_IDIKONICE};

        Cursor cursor = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_KATEGORIJE,
                koloneRezultat, null, null, null, null, null);
        int INDEX_KOLONE_NAZIV = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.KATEGORIJA_NAZIV);
        int INDEX_KOLONE_IKONE = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.KATEGORIJA_IDIKONICE);
        while (cursor.moveToNext()) {
            String naziv = cursor.getString(INDEX_KOLONE_NAZIV);
            String ikona = cursor.getString(INDEX_KOLONE_IKONE);
            Kategorija novaKategorija = new Kategorija(naziv,ikona);
            kategorije.add(novaKategorija);
        }
        cursor.close();
        db.close();

        return kategorije;

    }

    public void ubaciPitanjaIOdgovore(ArrayList<Pitanje> pitanja) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJA, null, null);
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_ODGOVORI, null, null);
        db.close();

        for (Pitanje p : pitanja) {
            ubaciJednoPitanjeSaOdgovorima(p);
        }

        return;
    }

    public void ubaciJednoPitanjeSaOdgovorima(Pitanje p){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues novoPitanje = new ContentValues();
        novoPitanje.put(KvizoviDBOpenHelper.PITANJE_NAZIV, p.getNaziv());
        novoPitanje.put(KvizoviDBOpenHelper.PITANJE_TACAN_ODG, p.getTacan());
        db.insert(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJA, null, novoPitanje);


        String[] koloneRezultat = new String[]{KvizoviDBOpenHelper.PITANJE_ID};
        String where = KvizoviDBOpenHelper.PITANJE_NAZIV + "= ?";
        String whereArgs[] = new String[]{p.getNaziv()};

        Cursor cursor = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJA,
                koloneRezultat, where, whereArgs, null, null, null);
        int INDEX_KOLONE_ID = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.PITANJE_ID);
        int idPitanja = 0;
        while (cursor.moveToNext()) {
            idPitanja = cursor.getInt(INDEX_KOLONE_ID);
        }
        cursor.close();
        for (String o : p.getOdgovori()) {
            ContentValues noviOdgovor = new ContentValues();
            noviOdgovor.put(KvizoviDBOpenHelper.ODGOVOR_TEKST, o);
            noviOdgovor.put(KvizoviDBOpenHelper.ODGOVOR_PITANJE_FK, idPitanja);
            db.insert(KvizoviDBOpenHelper.DATABASE_TABLE_ODGOVORI, null, noviOdgovor);
        }
        db.close();
    }

    public ArrayList<Pitanje> dajSvaPitanja(){
        ArrayList<Pitanje> pitanja = new ArrayList<>();

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] koloneRezultat = new String[]{KvizoviDBOpenHelper.PITANJE_ID,
                KvizoviDBOpenHelper.PITANJE_NAZIV, KvizoviDBOpenHelper.PITANJE_TACAN_ODG};

        Cursor cursor = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJA,
                koloneRezultat, null, null, null, null, null);
        int INDEX_KOLONE_ID = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.PITANJE_ID);
        int INDEX_KOLONE_NAZIV = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.PITANJE_NAZIV);
        int INDEX_KOLONE_TACAN = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.PITANJE_TACAN_ODG);
        while (cursor.moveToNext()) {
            String naziv = cursor.getString(INDEX_KOLONE_NAZIV);
            String tacan = cursor.getString(INDEX_KOLONE_TACAN);

            String[] koloneRezultatO = new String[]{KvizoviDBOpenHelper.ODGOVOR_TEKST};
            String where = KvizoviDBOpenHelper.ODGOVOR_PITANJE_FK + "= ?";
            String[] whereArgs = new String[]{String.valueOf(cursor.getInt(INDEX_KOLONE_ID))};

            Cursor cursor2 = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_ODGOVORI,
                    koloneRezultatO, where, whereArgs, null, null, null);
            int INDEX_KOLONE_ODGOVORA = cursor2.getColumnIndexOrThrow(KvizoviDBOpenHelper.ODGOVOR_TEKST);

            ArrayList<String> odgovori = new ArrayList<>();
            while (cursor2.moveToNext()) {
                String odgovor = cursor.getString(INDEX_KOLONE_ODGOVORA);
                odgovori.add(odgovor);
            }
            cursor2.close();

            Pitanje novoPitanje = new Pitanje(naziv,naziv,odgovori,tacan);
            pitanja.add(novoPitanje);

        }
        cursor.close();
        db.close();
        return pitanja;

    }

    public void ubaciKvizove(ArrayList<Kviz> kvizovi) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_KVIZOVI, null, null);
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJE_I_KVIZ, null, null);

        for (Kviz k : kvizovi) {
            ContentValues noviKviz = new ContentValues();
            noviKviz.put(KvizoviDBOpenHelper.KVIZ_NAZIV, k.getNaziv());
            noviKviz.put(KvizoviDBOpenHelper.KVIZ_ID_DOKUMENTA, k.getId());

            String[] koloneRezultat = new String[]{KvizoviDBOpenHelper.KATEGORIJA_ID};
            String where = KvizoviDBOpenHelper.KATEGORIJA_NAZIV + "= ?";
            String[] whereArgs = new String[]{k.getKategorija().getNaziv()};

            Cursor cursor = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_KATEGORIJE,
                    koloneRezultat, where, whereArgs, null, null, null);
            int INDEX_KOLONE_ID = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.KATEGORIJA_ID);
            int idKategorije = 0;
            while (cursor.moveToNext()) {
                idKategorije = cursor.getInt(INDEX_KOLONE_ID);
            }
            cursor.close();
            noviKviz.put(KvizoviDBOpenHelper.KVIZ_KATEGORIJA_FK, idKategorije);
            db.insert(KvizoviDBOpenHelper.DATABASE_TABLE_KVIZOVI, null, noviKviz);


            koloneRezultat = new String[]{KvizoviDBOpenHelper.KVIZ_ID};
            where = KvizoviDBOpenHelper.KVIZ_NAZIV + "= ?";
            whereArgs = new String[]{k.getNaziv()};

            cursor = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_KVIZOVI,
                    koloneRezultat, where, whereArgs, null, null, null);
            INDEX_KOLONE_ID = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.KVIZ_ID);
            int idKviza = 0;
            while (cursor.moveToNext()) {
                idKviza = cursor.getInt(INDEX_KOLONE_ID);
            }
            cursor.close();

            for (Pitanje p : k.getPitanja()) {

                koloneRezultat = new String[]{KvizoviDBOpenHelper.PITANJE_ID};
                where = KvizoviDBOpenHelper.PITANJE_NAZIV + "= ?";
                whereArgs = new String[]{p.getNaziv()};

                cursor = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJA,
                        koloneRezultat, where, whereArgs, null, null, null);
                INDEX_KOLONE_ID = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.PITANJE_ID);
                int idPitanja = 0;
                while (cursor.moveToNext()) {
                    idPitanja = cursor.getInt(INDEX_KOLONE_ID);
                }
                cursor.close();

                ContentValues noviUnos = new ContentValues();
                noviUnos.put(KvizoviDBOpenHelper.PIK_KVIZ_FK, idKviza);
                noviUnos.put(KvizoviDBOpenHelper.PIK_PITANJE_FK, idPitanja);
                db.insert(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJE_I_KVIZ, null, noviUnos);
            }
        }

        db.close();

        return;
    }

    public ArrayList<Kviz> dajSveKvizove(){
        ArrayList<Kviz> kvizovi = new ArrayList<>();

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] koloneRezultat = new String[]{KvizoviDBOpenHelper.KVIZ_ID,
                 KvizoviDBOpenHelper.KVIZ_NAZIV,KvizoviDBOpenHelper.KVIZ_ID_DOKUMENTA, KvizoviDBOpenHelper.KVIZ_KATEGORIJA_FK};

        Cursor cursor = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_KVIZOVI,
                koloneRezultat, null, null, null, null, null);
        int INDEX_KOLONE_ID = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.KVIZ_ID);
        int INDEX_KOLONE_NAZIV = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.KVIZ_NAZIV);
        int INDEX_KOLONE_ID_DOC = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.KVIZ_ID_DOKUMENTA);
        int INDEX_KOLONE_KAT = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.KVIZ_KATEGORIJA_FK);

        while (cursor.moveToNext()) {
            String naziv = cursor.getString(INDEX_KOLONE_NAZIV);
            String idDokumenta = cursor.getString(INDEX_KOLONE_ID_DOC);

            String[] koloneRezultatN = new String[]{KvizoviDBOpenHelper.KATEGORIJA_NAZIV, KvizoviDBOpenHelper.KATEGORIJA_IDIKONICE};
            String where = KvizoviDBOpenHelper.KATEGORIJA_ID + "= ?";
            String[] whereArgs = new String[]{String.valueOf(cursor.getInt(INDEX_KOLONE_KAT))};

            //nalazenje kategorije
            Cursor cursor2 = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_KATEGORIJE,
                    koloneRezultatN, where, whereArgs, null, null, null);

            int INDEX_NAZIV = cursor2.getColumnIndexOrThrow(KvizoviDBOpenHelper.KATEGORIJA_NAZIV);
            int INDEX_IKONE = cursor2.getColumnIndexOrThrow(KvizoviDBOpenHelper.KATEGORIJA_IDIKONICE);
            Kategorija novaKategorija = null;
            while (cursor2.moveToNext()) {
                String nazivK = cursor2.getString(INDEX_NAZIV);
                String ikonaK = cursor2.getString(INDEX_IKONE);
                novaKategorija = new Kategorija(nazivK,ikonaK);
            }
            cursor2.close();

            //nalazenje pitanja
            koloneRezultat = new String[]{KvizoviDBOpenHelper.PIK_PITANJE_FK};
            where = KvizoviDBOpenHelper.PIK_KVIZ_FK + "= ?";
            whereArgs = new String[]{String.valueOf(cursor.getInt(INDEX_KOLONE_ID))};

            cursor2 = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJE_I_KVIZ,
                    koloneRezultat, where, whereArgs, null, null, null);

            int INDEX_ID_PITANJA = cursor2.getColumnIndexOrThrow(KvizoviDBOpenHelper.PIK_PITANJE_FK);

            ArrayList<Pitanje> pitanja = new ArrayList<>();
            while (cursor2.moveToNext()) {
                int idPitanja = cursor2.getInt(INDEX_ID_PITANJA);

                //imam id pitanja i sad uzimam to pitanje i njegove odgovore
                String[] koloneRezultatP = new String[]{
                        KvizoviDBOpenHelper.PITANJE_NAZIV, KvizoviDBOpenHelper.PITANJE_TACAN_ODG};
                String whereP = KvizoviDBOpenHelper.PITANJE_ID + "= ?";
                String[] whereArgsP = new String[]{String.valueOf(idPitanja)};

                Cursor cursor3 = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJA,
                        koloneRezultatP, whereP, whereArgsP, null, null, null);

                int INDEX_KOLONE_P_NAZIV = cursor3.getColumnIndexOrThrow(KvizoviDBOpenHelper.PITANJE_NAZIV);
                int INDEX_KOLONE_TACAN = cursor3.getColumnIndexOrThrow(KvizoviDBOpenHelper.PITANJE_TACAN_ODG);
                while (cursor3.moveToNext()) {
                    String nazivP = cursor.getString(INDEX_KOLONE_P_NAZIV);
                    String tacan = cursor.getString(INDEX_KOLONE_TACAN);

                    String[] koloneRezultatO = new String[]{KvizoviDBOpenHelper.ODGOVOR_TEKST};
                    String whereO = KvizoviDBOpenHelper.ODGOVOR_PITANJE_FK + "= ?";
                    String[] whereArgsO = new String[]{String.valueOf(idPitanja)};

                    Cursor cursor4 = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_ODGOVORI,
                            koloneRezultatO, whereO, whereArgsO, null, null, null);
                    int INDEX_KOLONE_ODGOVORA = cursor4.getColumnIndexOrThrow(KvizoviDBOpenHelper.ODGOVOR_TEKST);

                    ArrayList<String> odgovori = new ArrayList<>();
                    while (cursor4.moveToNext()) {
                        String odgovor = cursor4.getString(INDEX_KOLONE_ODGOVORA);
                        odgovori.add(odgovor);
                    }
                    cursor4.close();

                    Pitanje novoPitanje = new Pitanje(nazivP,nazivP,odgovori,tacan);
                    pitanja.add(novoPitanje);

                }
                cursor3.close();
            }
            cursor2.close();


            Kviz noviKviz = new Kviz(naziv,pitanja,novaKategorija,idDokumenta);
            kvizovi.add(noviKviz);


        }
        cursor.close();
        db.close();
        return kvizovi;

    }


    private ArrayList<Pair<String, Double>> dajSveIzRangListeKviza(int idKviza){
        ArrayList<Pair<String, Double>> listaRezultata = new ArrayList<>();

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] koloneRezultat = new String[]{KvizoviDBOpenHelper.RANG_IME_IGRACA, KvizoviDBOpenHelper.RANG_PROCENAT};
        String where = KvizoviDBOpenHelper.RANG_KVIZ_FK + "= ?";
        String whereArgs[] = new String[]{String.valueOf(idKviza)};

        Cursor cursor = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_RANG,
                koloneRezultat, where, whereArgs, null, null, null);
        int INDEX_KOLONE_IME = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.RANG_IME_IGRACA);
        int INDEX_KOLONE_PROCENAT = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.RANG_PROCENAT);
        while (cursor.moveToNext()) {
            String naziv = cursor.getString(INDEX_KOLONE_IME);
            double procenat = cursor.getDouble(INDEX_KOLONE_PROCENAT);
            listaRezultata.add(new Pair<String, Double>(naziv,procenat));
        }
        cursor.close();
        db.close();

        return listaRezultata;
    }



    private void sortirajListu(ArrayList<Pair<String, Double>> lista) {
        Collections.sort(lista, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                if (o1.second > o2.second) {
                    return -1;
                } else if (o1.second.equals(o2.second)) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
    }

    public void ubaciIgruURangListu(String imeIgraca, double procenat, Kviz kviz){
        SQLiteDatabase db = helper.getWritableDatabase();

        String[] koloneRezultat = new String[]{KvizoviDBOpenHelper.KVIZ_ID};
        String where = KvizoviDBOpenHelper.KVIZ_NAZIV + "= ?";
        String[] whereArgs = new String[]{String.valueOf(kviz.getNaziv())};

        Cursor cursor = db.query(KvizoviDBOpenHelper.DATABASE_TABLE_KVIZOVI,
                koloneRezultat, where, whereArgs, null, null, null);
        int INDEX_KOLONE_ID = cursor.getColumnIndexOrThrow(KvizoviDBOpenHelper.KVIZ_ID);
        int idKviza = 0;
        while (cursor.moveToNext()) {
            idKviza = cursor.getInt(INDEX_KOLONE_ID);
        }
        cursor.close();

        ArrayList<Pair<String,Double>> listaKviza = dajSveIzRangListeKviza(idKviza);
        listaKviza.add(new Pair<String, Double>(imeIgraca,procenat));

        sortirajListu(listaKviza);

        //obrisi sve podatke iz rang liste za taj kviz
        where = KvizoviDBOpenHelper.RANG_KVIZ_FK + "= ?";
        whereArgs = new String[]{String.valueOf(idKviza)};
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_RANG, where, whereArgs);

        //sad ubaci sve iz liste
        int brojac = 0;
        for(Pair<String,Double> par : listaKviza) {
            ContentValues noviUnos = new ContentValues();
            noviUnos.put(KvizoviDBOpenHelper.RANG_IME_IGRACA, par.first);
            noviUnos.put(KvizoviDBOpenHelper.RANG_PROCENAT, par.second);
            noviUnos.put(KvizoviDBOpenHelper.RANG_POZICIJA, brojac + 1);
            noviUnos.put(KvizoviDBOpenHelper.RANG_KVIZ_FK,idKviza);
            db.insert(KvizoviDBOpenHelper.DATABASE_TABLE_RANG, null, noviUnos);
            brojac++;
        }

        db.close();
    }

    public void ubaciRangListu(ArrayList<Pair<Kviz, Pair<String, Double>>> rangLista) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_RANG, null, null);
        db.close();

        for(Pair par : rangLista){
            Kviz k = (Kviz) par.first;
            Pair<String, Double> igra = (Pair<String, Double>) par.second;
            ubaciIgruURangListu(igra.first, igra.second, k);
        }
    }
}
