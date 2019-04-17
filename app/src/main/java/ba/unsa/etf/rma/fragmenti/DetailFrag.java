package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.GridAdapter;
import ba.unsa.etf.rma.klase.Kviz;

public class DetailFrag extends Fragment {
    ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        GridView grid = (GridView) v.findViewById(R.id.gridKvizovi);

        if (getArguments() != null && getArguments().containsKey("kvizovi")) {
            odabraniKvizovi.clear();
            odabraniKvizovi.addAll ((ArrayList<Kviz>) getArguments().getSerializable("kvizovi"));

            GridAdapter adapter = new GridAdapter(getActivity(), odabraniKvizovi, getResources());
            grid.setAdapter(adapter);
        }

        if (savedInstanceState != null) {
            odabraniKvizovi.clear();
            odabraniKvizovi.addAll((ArrayList<Kviz>) savedInstanceState.getSerializable("kvizovi"));

        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("kvizovi", odabraniKvizovi);
    }

}
