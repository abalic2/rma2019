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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;

public class DodajURangListuVise extends IntentService {
    public int STATUS_RUNNING = 0;
    public int STATUS_FINISHED = 1;

    private String ime;
    private Double procenat;
    private String idKviza;
    private String naziv;

    public DodajURangListuVise() {
        super(null);
    }

    public DodajURangListuVise(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("caooo");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        ArrayList<Kviz> kk = (ArrayList<Kviz>) intent.getSerializableExtra("kvizovi");
        ArrayList<String> imena = intent.getStringArrayListExtra("imena");
        ArrayList<Double> procenti = (ArrayList<Double>) intent.getSerializableExtra("procenti");

        Bundle bundle = new Bundle();

        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        for (int i = 0; i < kk.size(); i++) {
            ime = imena.get(i);
            procenat = procenti.get(i);
            idKviza = kk.get(i).getId();
            naziv = kk.get(i).getNaziv();

            InputStream is = getResources().openRawResource(R.raw.secret);
            GoogleCredential credentials = null;
            try {
                credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String token = credentials.getAccessToken();

            ArrayList<Pair<String, Double>> lista = dajSveIzRangListe(token);
            lista.add(new Pair<String, Double>(ime, procenat));
            sortirajListu(lista);
            upisiSveOpet(lista, token);

        }
        receiver.send(STATUS_FINISHED, bundle);

    }

    private void upisiSveOpet(ArrayList<Pair<String, Double>> lista, String token) {

        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Rangliste/RANG" + idKviza + "?";
            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("PATCH");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            String rangLista = "{\n" +
                    " \"fields\": {\n" +
                    "  \"nazivKviza\": {\n" +
                    "   \"stringValue\": \"" + naziv + "\"\n" +
                    "  },\n" +
                    "  \"lista\": {\n" +
                    "   \"mapValue\": {\n" +
                    "    \"fields\": {\n";

            int brojac = 0;
            for (Pair<String, Double> p : lista) {
                if (brojac != 0) rangLista += ", ";
                rangLista += "     \"" + (brojac + 1) + "\": {\n" +
                        "      \"mapValue\": {\n" +
                        "       \"fields\": {\n" +
                        "        \"" + p.first + "\": {\n" +
                        "         \"stringValue\": \"" + p.second + "\"\n" +
                        "        }\n" +
                        "       }\n" +
                        "      }\n" +
                        "     }\n";
                brojac++;
            }

            rangLista += "    }\n" +
                    "   }\n" +
                    "  }\n" +
                    " }\n" +
                    "}";

            OutputStream os = urlConnection.getOutputStream();
            byte[] input = rangLista.getBytes("utf-8");
            os.write(input, 0, input.length);

            int responseCode = urlConnection.getResponseCode();
            InputStream ist = urlConnection.getInputStream();

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sortirajListu(ArrayList<Pair<String, Double>> lista) {
        Collections.sort(lista, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                if (o1.second > o2.second) {
                    return -1;
                } else if (o1.second.equals(o2.second)) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
    }

    private ArrayList<Pair<String, Double>> dajSveIzRangListe(String token) {
        ArrayList<Pair<String, Double>> listaRezultata = new ArrayList<>();

        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Rangliste/RANG" + idKviza + "?";

            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            InputStream in;
            try {
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (FileNotFoundException e) {
                return listaRezultata;
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

                        listaRezultata.add(new Pair<String, Double>(igrac, procenat));
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
        return listaRezultata;
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
}