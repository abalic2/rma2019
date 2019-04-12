package ba.unsa.etf.rma.aktivnosti;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class IgrajKvizAkt extends AppCompatActivity  implements PitanjeFrag.OnItemClick {
    private Kviz kviz;
    private ArrayList<Pitanje> pitanja;
    private ArrayList<String> odgovori;
    private Pitanje pitanje;
    private int brojTacnih = 0;
    private int brojPreostalih;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz);


        kviz = (Kviz) getIntent().getSerializableExtra("kviz");
        brojPreostalih = kviz.getPitanja().size();
        pitanja = new ArrayList<>();
        pitanja.addAll(kviz.getPitanja());
        Collections.shuffle(pitanja);

        InformacijeFrag fi = (InformacijeFrag) getSupportFragmentManager().findFragmentById(R.id.informacijePlace);

        if(fi == null){
            fi = new InformacijeFrag();
            Bundle argumenti = new Bundle();
            argumenti.putSerializable("kviz", kviz);
            argumenti.putInt("tacni",brojTacnih);
            argumenti.putInt("preostali",brojPreostalih);
            fi.setArguments(argumenti);
            getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, fi).commit();
        }

        PitanjeFrag fp = (PitanjeFrag) getSupportFragmentManager().findFragmentById(R.id.pitanjePlace);

        if(fp == null){
            fp = new PitanjeFrag();
            pitanje = pitanja.get(0);
            pitanja.remove(0);
            odgovori = new ArrayList<>();
            odgovori.addAll(pitanje.getOdgovori());

            Bundle argumenti = new Bundle();
            argumenti.putSerializable("pitanje", pitanje);
            fp.setArguments(argumenti);
            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, fp).commit();
        }
//        else{
//            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        }

    }

    @Override
    public void onItemClicked(int pos) {

        String odgovor = odgovori.get(pos);
        if(odgovor.equals(pitanje.getTacan())){
            brojTacnih++;
        }
        brojPreostalih--;

        Bundle argumenti = new Bundle();
        argumenti.putSerializable("kviz", kviz);
        argumenti.putInt("tacni",brojTacnih);
        argumenti.putInt("preostali",brojPreostalih);
        InformacijeFrag fi = new InformacijeFrag();
        fi.setArguments(argumenti);

        getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, fi)
                    .addToBackStack(null).commit();

        //čekanje 2s
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PitanjeFrag fpnovo = new PitanjeFrag();
                odgovori = new ArrayList<>();
                if(brojPreostalih != 0){
                    pitanje = pitanja.get(0);
                    pitanja.remove(0);
                    odgovori.addAll(pitanje.getOdgovori());
                }
                else{
                    pitanje = null;
                }

                Bundle argumenti = new Bundle();
                argumenti.putSerializable("pitanje", pitanje);
                fpnovo.setArguments(argumenti);
                getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, fpnovo).commit();
            }
        }, 2000);
    }

}
