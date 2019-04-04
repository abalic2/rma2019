package ba.unsa.etf.rma.adapteri;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class PitanjaAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Pitanje> data;
    private static LayoutInflater inflater = null;
    public Resources res;
    int i = 0;
    Pitanje pitanje = null;

    public PitanjaAdapter(Activity a, ArrayList d, Resources resLocal) {
        activity = a;
        data = d;
        res = resLocal;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount(){
        if (data.size() <= 0)
            return 0;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView naziv;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        PitanjaAdapter.ViewHolder holder;

        if (convertView == null) {

            vi = inflater.inflate(R.layout.element_pitanja, null);

            holder = new PitanjaAdapter.ViewHolder();
            holder.naziv = (TextView) vi.findViewById(R.id.Itemname);

            vi.setTag(holder);
        } else
            holder = (PitanjaAdapter.ViewHolder) vi.getTag();

        if (data.size() <= 0) {
            holder.naziv.setText("No Data");
        } else {
            pitanje = null;
            pitanje = (Pitanje) data.get(position);

            holder.naziv.setText(pitanje.getNaziv());
        }
        return vi;
    }
}
