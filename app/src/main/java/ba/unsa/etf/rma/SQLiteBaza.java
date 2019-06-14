package ba.unsa.etf.rma;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class SQLiteBaza {
    Context context;
    KvizoviDBOpenHelper helper;
    SQLiteBaza baza;

    public SQLiteBaza(Context context) {
        this.context = context;
        helper = new KvizoviDBOpenHelper(context, KvizoviDBOpenHelper.DATABASE_NAME, null,
                KvizoviDBOpenHelper.DATABASE_VERSION);
    }

    public void ubaciKategorije(ArrayList<Kategorija> kategorije) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_KATEGORIJE, null, null);

        for (Kategorija k : kategorije) {
            ContentValues novaKategorija = new ContentValues();
            novaKategorija.put(KvizoviDBOpenHelper.KATEGORIJA_NAZIV, k.getNaziv());
            novaKategorija.put(KvizoviDBOpenHelper.KATEGORIJA_IDIKONICE, k.getId());
            db.insert(KvizoviDBOpenHelper.DATABASE_TABLE_KATEGORIJE, null, novaKategorija);
        }

        db.close();

        return;
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
        }
        cursor.close();

        return kategorije;

    }

    public void ubaciPitanjaIOdgovore(ArrayList<Pitanje> pitanja) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJA, null, null);
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_ODGOVORI, null, null);

        for (Pitanje p : pitanja) {
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
            }

        }

        db.close();

        return;
    }

    public void ubaciKvizove(ArrayList<Kviz> kvizovi) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_KVIZOVI, null, null);
        db.delete(KvizoviDBOpenHelper.DATABASE_TABLE_PITANJE_I_KVIZ, null, null);

        for (Kviz k : kvizovi) {
            ContentValues noviKviz = new ContentValues();
            noviKviz.put(KvizoviDBOpenHelper.KVIZ_NAZIV, k.getNaziv());

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


}
