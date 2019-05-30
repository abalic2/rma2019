package ba.unsa.etf.rma.intentServisi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;


public class DodajPitanje extends IntentService {
    public int STATUS_RUNNING = 0;
    public int STATUS_FINISHED = 1;
    public int STATUS_ERROR = 2;

    public DodajPitanje() {
        super(null);
    }
    public DodajPitanje(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Pitanje pitanje = (Pitanje) intent.getSerializableExtra("pitanje");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();

        String naziv = pitanje.getNaziv();

        int brojac = 0;
        for(String o : pitanje.getOdgovori()){
            if(o.equals(pitanje.getTacan())) break;
            brojac++;
        }

        int indexTacnog = brojac;
        ArrayList<String> odgovori = new ArrayList<>();
        odgovori.addAll(pitanje.getOdgovori());

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
            String u = "https://firestore.googleapis.com/v1/projects/rmaspirala/databases/(default)/documents/Pitanja?documentId=PITANJE"+naziv;
            url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            String novoPitanje = "{ \"fields\": { \"naziv\": {\"stringValue\": \"" + naziv +"\" }, \"indexTacnog\": {" +
                    "\"integerValue\": \"" + indexTacnog + "\" },  \"odgovori\": { \"arrayValue\": { \"values\": [ ";
            brojac = 0;
            for(String odgovor : odgovori){
                if(brojac != 0) novoPitanje += ", ";
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


        //bundle.putStringArray("result", bundle);
        receiver.send(STATUS_FINISHED, bundle);

    }


}
