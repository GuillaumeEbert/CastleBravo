package API_Com.BroadcastReceiver;

/**
 * Created by Guillaumee on 11/04/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import API_Com.Modules.Wifi.Wifi;

/**
 * Broadcast receiver for the Wifi module
 * Extends BroadCastReceiverSc
 */
public class BR_Wifi extends BroadCastReceiverSc {

    private Wifi myWifiModule;
    private WifiManager theWifiManager;

    public BR_Wifi(Wifi aModule, WifiManager theWifiManager) {
        super();
        myWifiModule = aModule;
        this.theWifiManager = theWifiManager;
    }

    /**
     * @param context
     * @param intent  The intent filter which has trig
     */
    @Override
    public void onReceive(Context context, Intent intent) {


        /*State of the Wifi */
        if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            ScheduledExecutorService scheduleTaskExecutor;
            final Context cont = context;

            if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                myWifiModule.AlertWifiDisable().display(Wifi.ALERT_WIFI_DISABLE_DISPLAY_KEY, context);

                myWifiModule.setMySocketState(Wifi.WifiSocketState.NO_NETWORK);
            }

            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                myWifiModule.setMySocketState(Wifi.WifiSocketState.NO_NETWORK);

                scheduleTaskExecutor = Executors.newScheduledThreadPool(1);

                if (theWifiManager.getConnectionInfo().getSupplicantState() != SupplicantState.COMPLETED) {

                    //launch a Scan every 10sec
                    scheduleTaskExecutor.schedule(new Runnable() {
                        public void run() {
                            theWifiManager.startScan();
                            myWifiModule.getDialogScanResultWifi().display(Wifi.DIALOG_SCAN_RESULT_DISPLAY_KEY, cont);
                        }
                    }, 7, TimeUnit.MILLISECONDS);

                }

            }

        }

        /*State of the wifi connection*/
        if ((intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))) {
            NetworkInfo aNetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (aNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                //  Log.d("DETAILED_STATE", aNetworkInfo.getDetailedState().toString());

                switch (aNetworkInfo.getDetailedState()) {

                    case FAILED:
                        if (myWifiModule.getDialogScanResultWifi().isDisplay()) {
                            myWifiModule.getDialogScanResultWifi().updateScanListView(theWifiManager.getConnectionInfo(), aNetworkInfo.getDetailedState());
                        }
                        break;

                    case OBTAINING_IPADDR:
                        if (myWifiModule.getDialogScanResultWifi().isDisplay()) {
                            myWifiModule.getDialogScanResultWifi().updateScanListView(theWifiManager.getConnectionInfo(), aNetworkInfo.getDetailedState());
                        }
                        break;

                    case DISCONNECTING:
                        myWifiModule.setMySocketState(Wifi.WifiSocketState.NO_NETWORK);
                        break;

                    case DISCONNECTED:
                        myWifiModule.setMySocketState(Wifi.WifiSocketState.NO_NETWORK);
                        break;
                }

            }

        }

        /*Scan result available*/
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            if (myWifiModule.getDialogScanResultWifi().isDisplay()) {
                myWifiModule.getDialogScanResultWifi().setScanList(theWifiManager.getScanResults());

            }

        }

        if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
            SupplicantState aSupplicant = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);


            Log.d("SUPPLICANT_STATE", aSupplicant.toString());

            if (aSupplicant.equals(SupplicantState.COMPLETED)) {

                if (myWifiModule.getDialogScanResultWifi().isDisplay()) {
                    myWifiModule.getDialogScanResultWifi().updateScanListView(theWifiManager.getConnectionInfo(), NetworkInfo.DetailedState.CONNECTED);
                }

                myWifiModule.setMySocketState(Wifi.WifiSocketState.DISCONNECTED);
                myWifiModule.startConnection();

            }


        }


    }
}
