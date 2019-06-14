package ba.unsa.etf.rma;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KvizoviDBOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "mojaBaza.db";
    public static final String DATABASE_TABLE_KVIZOVI = "Kvizovi";
    public static final String DATABASE_TABLE_KATEGORIJE = "Kategorije";
    public static final String DATABASE_TABLE_PITANJA = "Pitanja";
    public static final String DATABASE_TABLE_RANG = "RangListe";
    public static final String DATABASE_TABLE_ODGOVORI = "Odgovori";
    public static final String DATABASE_TABLE_PITANJE_I_KVIZ = "PitanjeKviz";

    public static final int DATABASE_VERSION = 1;

    public static final String KATEGORIJA_ID ="_id";
    public static final String KATEGORIJA_NAZIV ="naziv";
    public static final String KATEGORIJA_IDIKONICE ="idIkonice";

    public static final String PITANJE_ID ="_id";
    public static final String PITANJE_NAZIV ="naziv";
    public static final String PITANJE_TACAN_ODG="tacanOdgovor";

    public static final String ODGVOOR_ID ="_id";
    public static final String ODGOVOR_TEKST ="odgovor";
    public static final String ODGOVOR_PITANJE_FK ="pitanjeId";

    public static final String KVIZ_ID ="_id";
    public static final String KVIZ_NAZIV ="naziv";
    public static final String KVIZ_ID_DOKUMENTA = "idDokumenta";
    public static final String KVIZ_KATEGORIJA_FK ="kategorijaId";

    public static final String PIK_ID ="_id";
    public static final String PIK_PITANJE_FK ="pitanjeId";
    public static final String PIK_KVIZ_FK ="kvizId";






// SQL upit za kreiranje tabele
    private static final String CREATE_TABLE_KATEGORIJE = "create table " +
            DATABASE_TABLE_KATEGORIJE + " (" +
            KATEGORIJA_ID + " integer primary key autoincrement, " +
            KATEGORIJA_NAZIV + " text not null, " +
            KATEGORIJA_IDIKONICE  + " integer not null);";

    private static final String CREATE_TABLE_PITANJA = "create table " +
            DATABASE_TABLE_PITANJA + " (" +
            PITANJE_ID + " integer primary key autoincrement, " +
            PITANJE_NAZIV + " text not null, " +
            PITANJE_TACAN_ODG  + " text not null);";

    private static final String CREATE_TABLE_ODGOVORI = "create table " +
            DATABASE_TABLE_ODGOVORI + " (" +
            ODGVOOR_ID + " integer primary key autoincrement, " +
            ODGOVOR_TEKST + " text not null, " +
            ODGOVOR_PITANJE_FK  + " integer not null," +
            " FOREIGN KEY (" + ODGOVOR_PITANJE_FK + ") REFERENCES " + DATABASE_TABLE_PITANJA + "(" + PITANJE_ID + "));";

    private static final String CREATE_TABLE_KVIZOVI = "create table " +
            DATABASE_TABLE_KVIZOVI + " (" +
            KVIZ_ID + " integer primary key autoincrement, " +
            KVIZ_NAZIV + " text not null, " +
            KVIZ_ID_DOKUMENTA + " text not null, " +
            KVIZ_KATEGORIJA_FK  + " integer not null," +
            " FOREIGN KEY (" + KVIZ_KATEGORIJA_FK + ") REFERENCES " + DATABASE_TABLE_KATEGORIJE + "(" + KATEGORIJA_ID + "));";

    private static final String CREATE_TABLE_PITANJE_I_KVIZ = "create table " +
            DATABASE_TABLE_PITANJE_I_KVIZ + " (" +
            PIK_ID + " integer primary key autoincrement, " +
            PIK_KVIZ_FK  + " integer not null," +
            PIK_PITANJE_FK  + " integer not null, " +
            " FOREIGN KEY (" + PIK_KVIZ_FK + ") REFERENCES " + DATABASE_TABLE_KVIZOVI + "(" + KVIZ_ID + "), " +
            " FOREIGN KEY (" + PIK_PITANJE_FK + ") REFERENCES " + DATABASE_TABLE_PITANJA + "(" + PITANJE_ID + "));";


    public KvizoviDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //Poziva se kada ne postoji baza
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_KATEGORIJE);
        db.execSQL(CREATE_TABLE_PITANJA);
        db.execSQL(CREATE_TABLE_ODGOVORI);
        db.execSQL(CREATE_TABLE_KVIZOVI);
        db.execSQL(CREATE_TABLE_PITANJE_I_KVIZ);
    }

    // Poziva se kada se ne poklapaju verzije baze na disku i trenutne baze
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Brisanje stare verzije
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_KATEGORIJE);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PITANJA);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ODGOVORI);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_KVIZOVI);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PITANJE_I_KVIZ);
// Kreiranje nove
        onCreate(db);
    }

}
