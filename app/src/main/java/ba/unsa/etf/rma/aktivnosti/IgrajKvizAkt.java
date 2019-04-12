package ba.unsa.etf.rma.aktivnosti;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.klase.Kviz;

public class IgrajKvizAkt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz);

        final Kviz kviz = (Kviz) getIntent().getSerializableExtra("kviz");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        InformacijeFrag fi = new InformacijeFrag();
        PitanjeFrag fp = new PitanjeFrag();
        Bundle argumenti=new Bundle();
        argumenti.putSerializable("kviz", kviz);
        fi.setArguments(argumenti);
        fp.setArguments(argumenti);

        ft.add(R.id.informacijePlace, fi);
        ft.add(R.id.pitanjePlace, fp);
        ft.commit();

    }
}
