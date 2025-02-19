package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maltaisn.icondialog.IconHelper;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;

public class GridAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Kviz> data;
    private static LayoutInflater inflater = null;
    public Resources res;
    int i = 0;
    Kviz kviz = null;

    public GridAdapter(Context a, ArrayList d, Resources resLocal) {
        context = a;
        data = d;
        res = resLocal;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
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
        ImageView slika;
        TextView naziv;
        TextView brojPitanja;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;

        if (convertView == null) {

            vi = inflater.inflate(R.layout.element_grida, null);

            holder = new ViewHolder();
            holder.naziv = (TextView) vi.findViewById(R.id.imeKviza);
            holder.slika = (ImageView) vi.findViewById(R.id.icon);
            holder.brojPitanja = (TextView) vi.findViewById(R.id.brojPitanja);

            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();

        if (data.size() <= 0) {
            holder.naziv.setText("No Data");
        } else {
            kviz = null;
            kviz = (Kviz) data.get(position);

            holder.naziv.setText(kviz.getNaziv());
            if( position != getCount()-1)
                holder.brojPitanja.setText(String.valueOf(kviz.getPitanja().size()));
            else {
                holder.brojPitanja.setText("");
            }

            final IconHelper iconHelper = IconHelper.getInstance(context);
            iconHelper.addLoadCallback(new IconHelper.LoadCallback() {
                @Override
                public void onDataLoaded() {
                    // This happens on UI thread, and is guaranteed to be called.
                    if (holder.brojPitanja.getText().equals("")){
                        holder.slika.setImageResource(R.drawable.plus);
                    }
                    else if (kviz.getKategorija().getNaziv().equalsIgnoreCase("Svi")) {
                        holder.slika.setImageDrawable(iconHelper.getIcon(232).getDrawable(inflater.getContext()));
                    }
                    else{
                        holder.slika.setImageDrawable(iconHelper.getIcon(Integer.parseInt(kviz.getKategorija().getId())).getDrawable(inflater.getContext()));
                    }
                }

            });

        }
        return vi;
    }
}