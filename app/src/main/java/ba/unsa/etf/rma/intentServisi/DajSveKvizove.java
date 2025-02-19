package ba.unsa.etf.rma.intentServisi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class DajSveKvizove extends IntentService {
    public int STATUS_RUNNING = 0;
    public int STATUS_FINISHED = 1;
    ArrayList<Kviz> rezultati;

    public DajSveKvizove() {
        super(null);
    }

    public DajSveKvizove(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        boolean dodaj = intent.getExtras().getBoolean("dodaj");
        Bundle bundle = new Bundle();
        rezultati = new ArrayList<>();

        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        InputStream is = getResources().openRawResource(R.raw.secret);
        GoogleCredential credentials = null;
        try {
            credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
            credentials.refreshToken();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String token = credentials.getAccessToken();

        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Kvizovi?&access_token=" + URLEncoder.encode(token, "UTF-8");
            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rezultat = convertStreamToString(in);
            JSONObject jo = new JSONObject(rezultat);

            try {
                JSONArray kvizovi = jo.getJSONArray("documents");
                for (int i = 0; i < kvizovi.length(); i++) {
                    JSONObject p = kvizovi.getJSONObject(i);

                    String[] name = p.getString("name").split("/");
                    String idKviza = name[name.length - 1];

                    JSONObject fields = p.getJSONObject("fields");
                    String naziv = fields.getJSONObject("naziv").getString("stringValue");
                    String idKategorije = fields.getJSONObject("idKategorije").getString("stringValue");

                    Kategorija k = dajKategoriju(idKategorije, token);

                    ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();

                    JSONObject pitanjaValues = fields.getJSONObject("pitanja").getJSONObject("arrayValue");
                    try {
                        JSONArray pitanja = pitanjaValues.getJSONArray("values");
                        for (int j = 0; j < pitanja.length(); j++) {
                            String id = pitanja.getJSONObject(j).getString("stringValue");
                            Pitanje pk = dajPitanje(id, token);
                            pitanjaKviza.add(pk);
                        }
                    } catch (Exception e) {

                    }

                    rezultati.add(new Kviz(naziv, pitanjaKviza, k, idKviza));
                }
            } catch (Exception e) {
            }


            int responseCode = urlConnection.getResponseCode();
            InputStream ist = urlConnection.getInputStream();


        } catch (
                IOException e) {
            e.printStackTrace();

        } catch (
                JSONException e) {
            e.printStackTrace();
        }

        bundle.putSerializable("kvizovi", rezultati);
        bundle.putBoolean("dodaj", dodaj);
        receiver.send(STATUS_FINISHED, bundle);

    }

    private Pitanje dajPitanje(String id, String token) {
        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Pitanja/" + id + "?access_token=" + URLEncoder.encode(token, "UTF-8");
            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rezultat = convertStreamToString(in);
            JSONObject jo = new JSONObject(rezultat);
            JSONObject fields = jo.getJSONObject("fields");

            String naziv = fields.getJSONObject("naziv").getString("stringValue");
            ArrayList<String> odgovoriPitanja = new ArrayList<>();
            JSONArray odgovori = fields.getJSONObject("odgovori").getJSONObject("arrayValue").getJSONArray("values");
            for (int j = 0; j < odgovori.length(); j++) {
                String odgovor = odgovori.getJSONObject(j).getString("stringValue");
                odgovoriPitanja.add(odgovor);
            }
            int indeksTacnog = Integer.parseInt(fields.getJSONObject("indexTacnog").getString("integerValue"));

            int responseCode = urlConnection.getResponseCode();
            InputStream ist = urlConnection.getInputStream();

            return new Pitanje(naziv, naziv, odgovoriPitanja, odgovoriPitanja.get(indeksTacnog));

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    private Kategorija dajKategoriju(String idKategorije, String token) {
        if (idKategorije.equals("KATEGORIJASvi")) {
            return new Kategorija("Svi", "0");
        }
        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Kategorije/" + idKategorije + "?access_token=" + URLEncoder.encode(token, "UTF-8");
            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rezultat = convertStreamToString(in);
            JSONObject jo = new JSONObject(rezultat);

            JSONObject fields = jo.getJSONObject("fields");

            String naziv = fields.getJSONObject("naziv").getString("stringValue");

            String idIkonice = fields.getJSONObject("idIkonice").getString("integerValue");

            int responseCode = urlConnection.getResponseCode();
            InputStream ist = urlConnection.getInputStream();

            return new Kategorija(naziv, idIkonice);

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

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

