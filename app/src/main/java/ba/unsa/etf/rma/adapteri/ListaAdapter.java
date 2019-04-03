package ba.unsa.etf.rma.adapteri;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;

public class ListaAdapter extends BaseAdapter{

    private Activity activity;
    private ArrayList<Kviz> data;
    private static LayoutInflater inflater = null;
    public Resources res;
    int i = 0;
    Kviz kviz = null;

    public ListaAdapter(Activity a, ArrayList d, Resources resLocal) {
        activity = a;
        data = d;
        res = resLocal;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount(){
        if (data.size() <= 0)
            return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        ImageView slika;
        TextView naziv;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;

        if (convertView == null) {

            vi = inflater.inflate(R.layout.element_liste, null);

            holder = new ViewHolder();
            holder.naziv = (TextView) vi.findViewById(R.id.Itemname);
            holder.slika = (ImageView) vi.findViewById(R.id.icon);

            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();

        if (data.size() <= 0) {
            holder.naziv.setText("No Data");
        } else {
            kviz = null;
            kviz = (Kviz) data.get(position);

            holder.naziv.setText(kviz.getNaziv());
            holder.slika.setImageResource(res.getIdentifier("ba.unsa.etf.rma:drawable/circle", null, null));
        }
        return vi;
    }
}