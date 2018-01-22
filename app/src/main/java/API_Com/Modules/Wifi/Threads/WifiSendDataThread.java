package API_Com.Modules.Wifi.Threads;

import android.util.Log;

import API_Com.Modules.Wifi.Wifi;

/**
 * Created by Guillaumee on 27/04/2016.
 */
public class WifiSendDataThread extends WifiThread {

    String theDataToSend;

    public WifiSendDataThread(Wifi aModule, String theDataToSend) {
        super(aModule);
        this.theDataToSend = theDataToSend;
    }


    @Override
    public void run() {
        super.run();

        myWifiToRun.doSend(theDataToSend);

    }
}
