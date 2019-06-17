package ba.unsa.etf.rma.receiveri;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class DodajURangListuViseRec extends ResultReceiver {
    private Receiver mReceiver;

    public DodajURangListuViseRec(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }
    /* Deklaracija interfejsa koji će se trebati implementirati */
    public interface Receiver {
        public void onReceiveResultRangListaVise(int resultCode, Bundle resultData);
    }
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResultRangListaVise(resultCode, resultData);
        }
    }
}