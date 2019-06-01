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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class EditKviz extends IntentService {
    public int STATUS_RUNNING = 0;
    public int STATUS_FINISHED = 1;
    public int STATUS_ERROR = 2;
    private Kviz kviz;

    public EditKviz() {
        super(null);
    }

    public EditKviz(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        kviz = (Kviz) intent.getSerializableExtra("kviz");
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

        if (jeLiDuplikat(token)) {
            receiver.send(STATUS_ERROR, bundle);
        } else {

            URL url = null;
            try {
                String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Kvizovi/"+kviz.getId()+"?currentDocument.exists=true";
                url = new URL(u);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("PATCH");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                String noviKviz = "{\n" +
                        " \"fields\": {\n" +
                        "  \"naziv\": {\n" +
                        "   \"stringValue\": \"" + kviz.getNaziv() + "\"\n" +
                        "  },\n" +
                        "  \"idKategorije\": {\n" +
                        "   \"stringValue\": \"KATEGORIJA" + kviz.getKategorija().getNaziv().replaceAll("\\s", "") + "\"\n" +
                        "  },\n" +
                        "  \"pitanja\": {\n" +
                        "   \"arrayValue\": {\n";

                if (kviz.getPitanja().size() != 0) {
                    noviKviz += "    \"values\": [\n";

                    int brojac = 0;
                    for (Pitanje p : kviz.getPitanja()) {
                        if (brojac != 0) noviKviz += ", ";
                        noviKviz += "{ \"stringValue\"" + ": \"PITANJE" + p.getNaziv().replaceAll("\\s", "") + "\" }";
                        brojac++;
                    }

                    noviKviz += "    ]\n";
                }
                noviKviz += "   }\n" +
                        "  }\n" +
                        " },\n" +
                        "}";


                OutputStream os = urlConnection.getOutputStream();
                byte[] input = noviKviz.getBytes("utf-8");
                os.write(input, 0, input.length);

                int responseCode = urlConnection.getResponseCode();
                InputStream ist = urlConnection.getInputStream();


            } catch (IOException e) {
                e.printStackTrace();

            }
            receiver.send(STATUS_FINISHED, bundle);
        }
    }

    private boolean jeLiDuplikat(String token) {
        URL url = null;
        try {
            String query = "{\n" +
                    " \"structuredQuery\": {\n" +
                    "  \"where\": {\n" +
                    "   \"fieldFilter\": {\n" +
                    "    \"field\": {\n" +
                    "     \"fieldPath\": \"naziv\"\n" +
                    "    },\n" +
                    "    \"op\": \"EQUAL\",\n" +
                    "    \"value\": {\n" +
                    "     \"stringValue\": \"" + kviz.getNaziv() + "\"\n" +
                    "    }\n" +
                    "   }\n" +
                    "  },\n" +
                    "  \"select\": {\n" +
                    "   \"fields\": [\n" +
                    "    {\n" +
                    "     \"fieldPath\": \"naziv\"\n" +
                    "    }" +
                    "   ]\n" +
                    "  },\n" +
                    "  \"from\": [\n" +
                    "   {\n" +
                    "    \"collectionId\": \"Kvizovi\"\n" +
                    "   }\n" +
                    "  ]\n" +
                    " }\n" +
                    "}";
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents:runQuery?access_token=" + URLEncoder.encode(token, "UTF-8");
            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = query.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rezultat = convertStreamToString(in);
            rezultat = "{ \"documents\": " + rezultat + "}";
            if (!rezultat.contains("naziv")) return false;
            JSONObject jo = new JSONObject(rezultat);
            JSONArray kvizovi = jo.getJSONArray("documents");
            for (int i = 0; i < kvizovi.length(); i++) {
                JSONObject p = kvizovi.getJSONObject(i);

                String[] name = p.getJSONObject("document").getString("name").split("/");
                String idKviza = name[name.length - 1];
                if(idKviza.equals(kviz.getId())) continue;

                JSONObject fields = p.getJSONObject("document").getJSONObject("fields");

                String naziv = fields.getJSONObject("naziv").getString("stringValue");

                if(naziv.equals(kviz.getNaziv())) return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
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
