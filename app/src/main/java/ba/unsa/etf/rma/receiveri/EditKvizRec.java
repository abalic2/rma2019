package ba.unsa.etf.rma.receiveri;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class EditKvizRec extends ResultReceiver {
    private Receiver mReceiver;

    public EditKvizRec(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }
    /* Deklaracija interfejsa koji će se trebati implementirati */
    public interface Receiver {
        public void onReceiveResultEditKviz(int resultCode, Bundle resultData);
    }
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResultEditKviz(resultCode, resultData);
        }
    }
}