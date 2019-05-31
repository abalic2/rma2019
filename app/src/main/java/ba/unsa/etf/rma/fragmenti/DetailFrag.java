package ba.unsa.etf.rma.fragmenti;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.GridAdapter;
import ba.unsa.etf.rma.klase.Kviz;

public class DetailFrag extends Fragment {
    ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();


    private OnItemClick oic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GridView grid = (GridView) getView().findViewById(R.id.gridKvizovi);

        int sirina = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.65 / 300);
        grid.setNumColumns(sirina);

        if (getArguments() != null && getArguments().containsKey("kvizovi")) {
            odabraniKvizovi.clear();
            odabraniKvizovi.addAll ((ArrayList<Kviz>) getArguments().getSerializable("kvizovi"));
            //jer nijedna dodana kategorija ne moze imati naziv ""
            //odabraniKvizovi.add(new Kviz("Dodaj kviz",null,new Kategorija("", "0")));

            GridAdapter adapter = new GridAdapter(getView().getContext(), odabraniKvizovi, getResources());
            grid.setAdapter(adapter);

            try {
                oic = (OnItemClick)getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString() + "Treba implementirati OnItemClick");
            }

            grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if(position == odabraniKvizovi.size()-1){
                        oic.dodajKvizGrid();
                    }
                    else {
                        oic.onItemLongClickedGrid(position);
                    }
                    return true;
                }
            });

            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(position != odabraniKvizovi.size()-1){
                        oic.onItemClickedGrid(position);
                    }
                }
            });


        }

        if (savedInstanceState != null) {
            odabraniKvizovi.clear();
            odabraniKvizovi.addAll((ArrayList<Kviz>) savedInstanceState.getSerializable("kvizovi"));

        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("kvizovi", odabraniKvizovi);
    }

    public interface OnItemClick {
        public void onItemClickedGrid(int pos);
        public void onItemLongClickedGrid(int pos);
        public void dodajKvizGrid();
    }

}




