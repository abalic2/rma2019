package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;

public class RangListaFrag extends Fragment {
    private ListView listaIgraca;
    private TextView tekst;
    private ArrayList<Pair<String, Double>> igraci;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pitanje, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listaIgraca = (ListView) getView().findViewById(R.id.odgovoriPitanja);
        tekst = (TextView) getView().findViewById(R.id.tekstPitanja);

        listaIgraca.setFocusable(false);

        if (getArguments().containsKey("lista")) {

            igraci = new ArrayList<>();

            tekst.setText("Rang lista:");

            ArrayList<Pair<String, Double>> lista = (ArrayList<Pair<String, Double>>) getArguments().getSerializable("lista");
            igraci.addAll(lista);

            ArrayList<String> ispisi = new ArrayList<>();
            int pozicija = 1;
            for(Pair<String,Double> par : igraci){
                ispisi.add(pozicija + ". " + par.first + " - procenat " + par.second + "%");
                pozicija++;
            }

            final ArrayAdapter<String> adapter;
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, ispisi);
            listaIgraca.setAdapter(adapter);
            justifyListViewHeightBasedOnChildren(listaIgraca);

        }
    }

    //da namjesti velicinu liste
    public static void justifyListViewHeightBasedOnChildren(ListView listView) {

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }
}
