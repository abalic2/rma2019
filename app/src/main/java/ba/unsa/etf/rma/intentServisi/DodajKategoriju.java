package ba.unsa.etf.rma.intentServisi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

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
import ba.unsa.etf.rma.klase.Kategorija;

public class DodajKategoriju extends IntentService {
    public int STATUS_RUNNING = 0;
    public int STATUS_FINISHED = 1;
    public int STATUS_ERROR = 2;
    private Kategorija kategorija;

    public DodajKategoriju() {
        super(null);
    }

    public DodajKategoriju(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        kategorija = (Kategorija) intent.getSerializableExtra("kategorija");
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
                String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Kategorije?documentId=" + kategorija.getIdDokumenta();
                url = new URL(u);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                String noviKviz = "{\n" +
                        " \"fields\": {\n" +
                        "  \"naziv\": {\n" +
                        "   \"stringValue\": \"" + kategorija.getNaziv() + "\"\n" +
                        "  },\n" +
                        "  \"idIkonice\": {\n" +
                        "   \"integerValue\": \"" + kategorija.getId() + "\"\n" +
                        "  }\n" +
                        "   }\n" +
                        "  }";


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
                    "     \"stringValue\": \"" + kategorija.getNaziv() + "\"\n" +
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
                    "    \"collectionId\": \"Kategorije\"\n" +
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
            if (rezultat.contains("naziv")) return true;

        } catch (IOException e) {
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