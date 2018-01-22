package API_Com.Modules.Wifi.Threads;

import android.util.Log;

import API_Com.Modules.ThreadMessage;
import API_Com.Modules.Wifi.Wifi;
import API_Com.Modules.Wifi.Wifi.WifiSocketState;
import API_Com.Modules.ThreadMessage.ThreadMessageKey;
import API_Com.Modules.ThreadMessage.ThreadMessageState;

/**
 * Created by Guillaumee on 27/04/2016.
 */
public class WifiConnectionThread extends WifiThread {

    public WifiConnectionThread(Wifi aModule) {
        super(aModule);
    }


    @Override
    public void run() {
        super.run();

        Log.d("WIFI_thread_Connection", "START");

        if (myWifiToRun.getMySocketState() == WifiSocketState.DISCONNECTED) {

            try {
                sleep(800);

                myWifiToRun.setMySocketState(WifiSocketState.CONNECTING);
                myWifiToRun.doConnection();

                sendAInterThreadMessage(ThreadMessageKey.CONNECTION_KEY.name(), new ThreadMessage(ThreadMessageState.CONNECTED));

                myWifiToRun.setMySocketState(WifiSocketState.CONNECTED);
                myWifiToRun.startListenForData();
                Run().set(false);


            } catch (Exception e) {
                Sleep(800);

                myWifiToRun.setMySocketState(WifiSocketState.CONNECTION_FAILED);
                sendAInterThreadMessage(ThreadMessageKey.CONNECTION_KEY.name(), new ThreadMessage(ThreadMessageState.CONNECTION_FAILED, e));
                canRun.set(false);

            }

        } else {

            String theMessageForException = "Error. ";

            switch (myWifiToRun.getMySocketState()) {
                case CONNECTED:
                    theMessageForException = theMessageForException + "The application is already connected to a server";
                    break;

                case NO_NETWORK:
                    theMessageForException = theMessageForException + "There is no wifi connection";
                    break;

            }

            sendAInterThreadMessage(ThreadMessageKey.CONNECTION_KEY.name(), new ThreadMessage(ThreadMessageState.CONNECTION_FAILED, new Exception(theMessageForException)));

        }

        Log.d("WIFI_thread_Connection", "OVER");
    }


}

