package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class PitanjeFrag extends Fragment {
    private ListView listaOdgovora;
    private TextView tekstPitanja;
    private ArrayList<String> odgovori;
    private OnItemClick oic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pitanje, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listaOdgovora = (ListView) getView().findViewById(R.id.odgovoriPitanja);
        tekstPitanja = (TextView) getView().findViewById(R.id.tekstPitanja);



        if(getArguments().containsKey("pitanje")){

            final Pitanje pitanje = (Pitanje) getArguments().getSerializable("pitanje");
            odgovori = new ArrayList<>();

            if (pitanje == null) {
                tekstPitanja.setText("Kviz je završen!");
            }
            else {
                odgovori.addAll(getArguments().getStringArrayList("odgovori"));
                tekstPitanja.setText(pitanje.getNaziv());
            }

            final ArrayAdapter<String> adapterOdgovora;
            adapterOdgovora = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, odgovori);
            listaOdgovora.setAdapter(adapterOdgovora);
            justifyListViewHeightBasedOnChildren(listaOdgovora);

            try
            {
                oic = (OnItemClick) getActivity();
            }
            catch (ClassCastException e)
            {
                throw new ClassCastException(getActivity().toString() + "Treba implementirati OnItemClick");
            }

            listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    if(odgovori.get(position).equals(pitanje.getTacan())){
                        view.setBackgroundColor(getResources().getColor(R.color.zelena));
                    }
                    else{
                        view.setBackgroundColor(getResources().getColor(R.color.crvena));
                        int indeksTacnog = 0;
                        for(String s : odgovori){
                            if(s.equals(pitanje.getTacan())) {
                                listaOdgovora.getChildAt(indeksTacnog).setBackgroundColor(getResources().getColor(R.color.zelena));
                                break;
                            }
                            indeksTacnog++;
                        }
                    }
                    listaOdgovora.setEnabled(false);
                    oic.onItemClicked(position);
                }
            });
        }
    }

    public interface OnItemClick {
        public void onItemClicked(int pos);
    }

    //da namjesti velicinu liste
    public static void justifyListViewHeightBasedOnChildren (ListView listView) {

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
