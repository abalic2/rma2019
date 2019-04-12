package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;


public class InformacijeFrag extends Fragment {
    private TextView nazivKviza;
    private TextView brojTacnihPitanja;
    private TextView brojPreostalihPitanja;
    private TextView procenatTacni;
    private int brojTacnih = 0;
    private int brojPreostalih;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View iv= inflater.inflate(R.layout.fragment_informacije, container, false);

        if(getArguments() != null && getArguments().containsKey("kviz")){
            Kviz kviz = (Kviz) getArguments().getSerializable("kviz");
            brojPreostalih = kviz.getPitanja().size();
            nazivKviza = (TextView) iv.findViewById(R.id.infNazivKviza);
            brojTacnihPitanja = (TextView) iv.findViewById(R.id.infBrojTacnihPitanja);
            brojPreostalihPitanja = (TextView) iv.findViewById(R.id.infBrojPreostalihPitanja);
            procenatTacni = (TextView) iv.findViewById(R.id.infProcenatTacni);

            nazivKviza.setText(kviz.getNaziv());
            brojTacnihPitanja.setText(String.valueOf(brojTacnih));
            brojPreostalihPitanja.setText(String.valueOf(brojTacnih));
        }
        return iv;
    }

}
