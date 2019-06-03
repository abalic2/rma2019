package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.intentServisi.DodajKategoriju;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.receiveri.DodajKategorijuRec;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback, DodajKategorijuRec.Receiver {
    private EditText nazivKategorije;
    private EditText ikona;
    private Button dodajIkonu;
    private Button dodajKategoriju;

    private Icon[] selectedIcons;

    private Kategorija novaKategorija;

    private DodajKategorijuRec mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju);

        mReceiver = new DodajKategorijuRec(new Handler());
        mReceiver.setReceiver(this);

        novaKategorija = new Kategorija();

        nazivKategorije = (EditText) findViewById(R.id.etNaziv);
        ikona = (EditText) findViewById(R.id.etIkona);
        dodajIkonu = (Button) findViewById(R.id.btnDodajIkonu);
        ikona.setText("");
        ikona.setEnabled(false);
        dodajKategoriju = (Button) findViewById(R.id.btnDodajKategoriju);

        nazivKategorije.setText("");

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
                if (jeLiSveValidno()) {
                    novaKategorija.setNaziv(nazivKategorije.getText().toString());
                    novaKategorija.setId(ikona.getText().toString());
                    dodajKategorijuUBazu(novaKategorija);
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
