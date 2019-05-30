package ba.unsa.etf.rma.receiveri;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class DajSveKvizoveRec extends ResultReceiver {
    private DajSveKvizoveRec.Receiver mReceiver;

    public DajSveKvizoveRec(Handler handler) {
        super(handler);
    }

    public void setReceiver(DajSveKvizoveRec.Receiver receiver) {
        mReceiver = receiver;
    }
    /* Deklaracija interfejsa koji će se trebati implementirati */
    public interface Receiver {
        public void onReceiveResultKvizovi(int resultCode, Bundle resultData);
    }
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResultKvizovi(resultCode, resultData);
        }
    }
}