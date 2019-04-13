package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;

public class ListaFrag extends Fragment {
    ArrayList<Kategorija> kategorije = new ArrayList<>();
    private OnItemClick oic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getArguments().containsKey("kategorije")){
            kategorije = (ArrayList<Kategorija>) getArguments().getSerializable("kategorije");
            ListView lv = (ListView )getView().findViewById(R.id.listaKategorija);
            final ArrayAdapter<Kategorija> adapter;
            adapter = new ArrayAdapter<Kategorija> (getActivity(), android.R.layout.simple_list_item_1, kategorije);
            lv.setAdapter(adapter);


            try {
                oic = (OnItemClick)getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString() + "Treba implementirati OnItemClick");
            }
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    oic.onItemClicked(position);
                }
            });

        }
    }

    public interface OnItemClick {
        public void onItemClicked(int pos);
    }

}