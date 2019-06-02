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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class ImportKviza extends IntentService {
    public int STATUS_RUNNING = 0;
    public int STATUS_FINISHED = 1;
    public int STATUS_ERROR = 2;
    private Kviz kviz;

    public ImportKviza() {
        super(null);
    }

    public ImportKviza(String name) {
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
            bundle.putString("poruka", "Kviz sa ovim imenom vec postoji!");
            receiver.send(STATUS_ERROR, bundle);
        } else if (imaLiDuplikataPitanja(token, kviz.getPitanja())) {
            bundle.putString("poruka", "Pitanja sa tim tekstom vec postoje!");
            receiver.send(STATUS_ERROR, bundle);
        } else {
            ArrayList<Pitanje> mogucaPitanja = svaPitanja(token);
            dodajKategorijuAkoTreba(kviz.getKategorija(), token);
            for (Pitanje p : kviz.getPitanja()) {
                dodajPitanjeUBazu(p, token);
            }
            bundle.putSerializable("kviz",kviz);
            bundle.putSerializable("pitanja", mogucaPitanja);
            receiver.send(STATUS_FINISHED, bundle);
        }
    }

    private ArrayList<Pitanje> svaPitanja(String token){
        ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();
        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Pitanja?access_token=" + URLEncoder.encode(token, "UTF-8");
            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rezultat = convertStreamToString(in);
            JSONObject jo = new JSONObject(rezultat);
            try {
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
                    int indeksTacnog = Integer.parseInt(fields.getJSONObject("indexTacnog").getString("integerValue"));

                    mogucaPitanja.add(new Pitanje(naziv, naziv, odgovoriPitanja, odgovoriPitanja.get(indeksTacnog)));
                }
            }
            catch (Exception e){}


            int responseCode = urlConnection.getResponseCode();
            InputStream ist = urlConnection.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mogucaPitanja;
    }

    private void dodajPitanjeUBazu(Pitanje pitanje, String token) {

        String naziv = pitanje.getNaziv();

        int brojac = 0;
        for (String o : pitanje.getOdgovori()) {
            if (o.equals(pitanje.getTacan())) break;
            brojac++;
        }

        int indexTacnog = brojac;
        ArrayList<String> odgovori = new ArrayList<>();
        odgovori.addAll(pitanje.getOdgovori());

        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Pitanja?documentId=PITANJE" + naziv.replaceAll("\\s","");
            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            String novoPitanje = "{ \"fields\": { \"naziv\": {\"stringValue\": \"" + naziv + "\" }, \"indexTacnog\": {" +
                    "\"integerValue\": \"" + indexTacnog + "\" },  \"odgovori\": { \"arrayValue\": { \"values\": [ ";
            brojac = 0;
            for (String odgovor : odgovori) {
                if (brojac != 0) novoPitanje += ", ";
                novoPitanje += "{ \"stringValue\"" + ": \"" + odgovor + "\" }";
                brojac++;
            }
            novoPitanje += " ] } } } }";


            OutputStream os = urlConnection.getOutputStream();
            byte[] input = novoPitanje.getBytes("utf-8");
            os.write(input, 0, input.length);

            int responseCode = urlConnection.getResponseCode();
            InputStream ist = urlConnection.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    private boolean imaLiDuplikataPitanja(String token, ArrayList<Pitanje> pitanja) {
        for (Pitanje p : pitanja) {
            if (jeLiPitanjeDuplikat(p, token)) return true;
        }
        return false;
    }

    private boolean jeLiPitanjeDuplikat(Pitanje p, String token) {
        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Pitanja/PITANJE" + p.getNaziv() + "?";

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
                return false;
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void dodajKategorijuAkoTreba(Kategorija kategorija, String token) {
        if (!imaLiKategorije(kategorija, token)) {
            upisiKategorijuUBazu(kategorija, token);
        }
    }

    private void upisiKategorijuUBazu(Kategorija kategorija, String token) {
        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Kategorije?documentId=KATEGORIJA" + kategorija.getNaziv();
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

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean imaLiKategorije(Kategorija kategorija, String token) {
        if(kategorija.getNaziv().equals("Svi")) return true;
        URL url = null;
        try {
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Kategorije/KATEGORIJA" + kviz.getKategorija().getNaziv() + "?";

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
                return false;
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
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
