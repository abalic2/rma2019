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
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;

public class DetailFrag extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        GridView grid = (GridView) getActivity().findViewById(R.id.gridKvizovi);

        Kategorija kategorija = (Kategorija) getArguments().getSerializable("kategorija");
        ArrayList<Kviz> kvizovi = (ArrayList<Kviz>) getArguments().getSerializable("kvizovi");

        ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();
        for(Kviz k : kvizovi){
            if(k.getKategorija().getNaziv().equals(kategorija.getNaziv())){
                odabraniKvizovi.add(k);
            }
        }

        GridAdapter adapter = new GridAdapter(getActivity(), odabraniKvizovi, getResources());
        grid.setAdapter(adapter);




        return v;
    }

}
