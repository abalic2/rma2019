package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;


public class InformacijeFrag extends Fragment {
    private TextView nazivKviza;
    private TextView brojTacnihPitanja;
    private TextView brojPreostalihPitanja;
    private TextView procenatTacni;
    private Button dugme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View iv = inflater.inflate(R.layout.fragment_informacije, container, false);
        nazivKviza = (TextView) iv.findViewById(R.id.infNazivKviza);
        brojTacnihPitanja = (TextView) iv.findViewById(R.id.infBrojTacnihPitanja);
        brojPreostalihPitanja = (TextView) iv.findViewById(R.id.infBrojPreostalihPitanja);
        procenatTacni = (TextView) iv.findViewById(R.id.infProcenatTacni);
        dugme = (Button) iv.findViewById(R.id.btnKraj);

        dugme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        if (getArguments() != null && getArguments().containsKey("kviz")) {
            Kviz kviz = (Kviz) getArguments().getSerializable("kviz");
            nazivKviza.setText(kviz.getNaziv());
            int tacni = getArguments().getInt("tacni");
            int preostali = getArguments().getInt("preostali");
            int ukupno = getArguments().getInt("ukupanBroj");
            int proslo = ukupno - preostali - 1;
            brojTacnihPitanja.setText(String.valueOf(tacni));
            if(preostali != -1)
                brojPreostalihPitanja.setText(String.valueOf(preostali));
            else
                brojPreostalihPitanja.setText("0");
            double procenat = 0;
            if (proslo != 0) {
                procenat = (double) tacni / proslo * 100;
            }
            procenatTacni.setText(String.format("%.1f", procenat) + "%");

        }

        return iv;
    }

}
