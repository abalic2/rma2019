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
import ba.unsa.etf.rma.klase.Pitanje;

public class DajSvaPitanja extends IntentService {
    public int STATUS_RUNNING = 0;
    public int STATUS_FINISHED = 1;
    public int STATUS_ERROR = 2;
    ArrayList<Pitanje> rezultati;

    public DajSvaPitanja() {
        super(null);
    }
    public DajSvaPitanja(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();


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
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Pitanja?access_token=" + URLEncoder.encode(token, "UTF-8");
            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rezultat = convertStreamToString(in);
            JSONObject jo = new JSONObject(rezultat);
            JSONArray pitanja = jo.getJSONArray("documents");
            for (int i = 0; i < pitanja.length(); i++) {
                JSONObject p = pitanja.getJSONObject(i);
                JSONObject fields = p.getJSONObject("fields");

                String naziv = fields.getJSONObject("naziv").getString("stringValue");
                ArrayList<String> odgovoriPitanja = new ArrayList<>();
                JSONArray odgovori = fields.getJSONObject("odgovori").getJSONObject("arrayValue").getJSONArray("values");
                for (int j = 0; j < odgovori.length(); j++) {
                    String odgovor = odgovori.getJSONObject(j).getString("stringValue");
                    odgovoriPitanja.add(odgovor);
                }
                int indeksTacnog = Integer.parseInt(fields.getJSONObject("indexTacnog").getString("integetValue"));

                rezultati.add(new Pitanje(naziv,naziv,odgovoriPitanja,odgovoriPitanja.get(indeksTacnog)));
            }


            int responseCode = urlConnection.getResponseCode();
            InputStream ist = urlConnection.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        bundle.putSerializable("pitanja", rezultati);
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


}

