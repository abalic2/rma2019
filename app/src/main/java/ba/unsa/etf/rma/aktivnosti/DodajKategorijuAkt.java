package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback{
    private EditText nazivKategorije;
    private EditText ikona;
    private Button dodajIkonu;
    private Button dodajKategoriju;
    private ArrayList<Kategorija> kategorije;
    private Icon[] selectedIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju);

        final Kategorija novaKategorija = new Kategorija();

        kategorije = (ArrayList<Kategorija>) getIntent().getSerializableExtra("kategorije");
        nazivKategorije = (EditText) findViewById(R.id.etNaziv);
        ikona = (EditText) findViewById(R.id.etIkona);
        dodajIkonu = (Button) findViewById(R.id.btnDodajIkonu);
        ikona.setEnabled(false);
        dodajKategoriju = (Button) findViewById(R.id.btnDodajKategoriju);

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
                novaKategorija.setNaziv(nazivKategorije.getText().toString());
                novaKategorija.setId(ikona.getText().toString());
                kategorije.add(kategorije.size()-1, novaKategorija);
                Intent myIntent = new Intent();
                myIntent.putExtra("kategorije",kategorije);
                setResult(Activity.RESULT_OK, myIntent);
                finish();
            }
        });



    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        ikona.setText(String.valueOf(selectedIcons[0].getId()));
    }
}
