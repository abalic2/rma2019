package ba.unsa.etf.rma.intentServisi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Pair;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;

public class DajSveIzRangListe extends IntentService {
    public int STATUS_RUNNING = 0;
    public int STATUS_FINISHED = 1;
    ArrayList<Pair<Kviz, Pair<String,Double>>> rezultati;

    private ArrayList<Kviz> kvizovi;

    public DajSveIzRangListe() {
        super(null);
    }

    public DajSveIzRangListe(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        kvizovi = (ArrayList<Kviz>) intent.getSerializableExtra("kvizovi");


        Bundle bundle = new Bundle();

        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        rezultati = new ArrayList<>();

        InputStream is = getResources().openRawResource(R.raw.secret);
        GoogleCredential credentials = null;
        try {
            credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
            credentials.refreshToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String token = credentials.getAccessToken();

        for(Kviz k : kvizovi){
            dajRangListuJednogKviza(k, token);
        }

        bundle.putSerializable("rangLista", rezultati);
        receiver.send(STATUS_FINISHED, bundle);

    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }

    private void dajRangListuJednogKviza(Kviz kviz, String token) {
        String idKviza = kviz.getId();
        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Rangliste/RANG" + idKviza + "?";

            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            InputStream in = null;
            try {
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (FileNotFoundException e) {
                return;
            }
            String rezultat = convertStreamToString(in);
            JSONObject jo = new JSONObject(rezultat);

            try {
                JSONObject sve = jo.getJSONObject("fields");
                String naziv = sve.getJSONObject("nazivKviza").getString("stringValue");
                JSONObject lista = sve.getJSONObject("lista").getJSONObject("mapValue").getJSONObject("fields");
                try {
                    int brojac = 1;
                    for (; ; ) {
                        JSONObject jedanZapis = lista.getJSONObject(String.valueOf(brojac)).getJSONObject("mapValue").getJSONObject("fields");
                        String zapis = jedanZapis.toString();

                        String[] elementi = zapis.split("\"");
                        String igrac = elementi[1];
                        Double procenat = Double.valueOf(elementi[5]);
                        Pair<String,Double> par = new Pair<>(igrac, procenat);

                        rezultati.add(new Pair<Kviz, Pair<String, Double>>(kviz, par));
                        brojac++;
                    }
                } catch (JSONException e) {

                }

                int responseCode = urlConnection.getResponseCode();
                InputStream ist = urlConnection.getInputStream();


            } catch (IOException e) {
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}