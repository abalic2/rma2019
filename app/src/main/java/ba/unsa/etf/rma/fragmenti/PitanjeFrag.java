package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class PitanjeFrag extends Fragment {
    private ListView listaOdgovora;
    private ArrayList<String> odgovori = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pitanje, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getArguments().containsKey("pitanje")){
            Pitanje pitanje = (Pitanje) getArguments().getSerializable("pitanje");
            odgovori.addAll(pitanje.getOdgovori());
            listaOdgovora = (ListView) getView().findViewById(R.id.odgovoriPitanja);

            final ArrayAdapter<String> adapterOdgovora;
            adapterOdgovora = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, odgovori);
            listaOdgovora.setAdapter(adapterOdgovora);
        }
    }

}
