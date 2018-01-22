package API_Com.DialogAndAlert.Dialog;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import API_Com.Modules.Wifi.MyScanResult;
import API_Com.Modules.Wifi.Wifi;
import fr.telecom_physique.castlebravo.R;

/**
 * Dialog to select a wifi network.
 */
public class WifiDialogSelectNetwork extends DialogSc {

    private RelativeLayout myProgressBarLayout;
    private RelativeLayout myListViewLayout;
    private ListView myScanResultListView;
    private List<MyScanResult> arrL_MyScanResult;
    private MyListAdapter myAdapter;
    private WifiManager theWifiManager;
    private ScheduledExecutorService scheduleTaskExecutor;
    private WifiDialogConnectNetwork theConnectionDialog;
    private Handler autoDismiss;


    public static WifiDialogSelectNetwork newInstance() {

        WifiDialogSelectNetwork fragment = new WifiDialogSelectNetwork();
        fragment.setStyle(STYLE_NORMAL, R.style.CustomDialog);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.api_wifi_dialog_scan_result, container, false);
        getDialog().setTitle(R.string.wifi_dialog_alert_tittle);

        autoDismiss = new Handler();

        myProgressBarLayout = (RelativeLayout) view.findViewById(R.id.layout_progress_bar_scan);
        myProgressBarLayout.setVisibility(View.VISIBLE);

        myListViewLayout = (RelativeLayout) view.findViewById(R.id.layout_lv_scan_result);
        myListViewLayout.setVisibility(View.GONE);

        myScanResultListView = (ListView) view.findViewById(R.id.lv_scan_result);
        arrL_MyScanResult = new ArrayList<MyScanResult>();

        myAdapter = new MyListAdapter(getActivity(), R.layout.api_wifi_list_view_scan_result, arrL_MyScanResult);
        myScanResultListView.setAdapter(myAdapter);

        theWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        theWifiManager.startScan();

        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);

        //launch a Scan every 10sec
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                theWifiManager.startScan();

            }
        }, 0, 15, TimeUnit.SECONDS);

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        myScanResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                theConnectionDialog = WifiDialogConnectNetwork.newInstance(arrL_MyScanResult.get(position));
                theConnectionDialog.display(Wifi.DIALOG_CONNECT_TO_NETWORK_DISPLAY_KEY, getActivity());

            }
        });

    }


    @Override
    public void onStop() {
        super.onStop();
        isDisplay = false;
        scheduleTaskExecutor.shutdown();

    }


    public void setScanList(List<ScanResult> theResultList) {

        if (myProgressBarLayout.getVisibility() == View.VISIBLE) {
            myProgressBarLayout.setVisibility(View.GONE);
            myListViewLayout.setVisibility(View.VISIBLE);
        }

        sortListScanResult(theResultList);

    }


    /**
     * Sort the List<ScanResult>, the wifi with the most signal are shown at the top the double are skipped
     */
    private void sortListScanResult(List<ScanResult> aScanResultList) {

        int numLevels = 5;
        int signalLevel;
        ScanResult aScanResult;
        arrL_MyScanResult.clear();

        for (int i = numLevels - 1; i >= 0; i--) {

            for (int y = 0; y < aScanResultList.size(); y++) { /*Get through the Scan list result */

                aScanResult = aScanResultList.get(y);
                signalLevel = WifiManager.calculateSignalLevel(aScanResult.level, numLevels);

                if (signalLevel == i) {
                    if (!containSSID(aScanResult)) {

                        arrL_MyScanResult.add(new MyScanResult(aScanResult, signalLevel));
                        myAdapter.notifyDataSetChanged();
                    }
                }

            }
        }
    }

    /**
     * Check if the SSID is already in the list
     *
     * @param aScanResultToCompare
     * @return
     * @true if the SSID is already present
     * @false if not
     */
    private boolean containSSID(ScanResult aScanResultToCompare) {
        for (int i = 0; i < arrL_MyScanResult.size(); i++) {
            if (arrL_MyScanResult.get(i).getScanData().SSID.equals(aScanResultToCompare.SSID)) {
                return true;
            }
        }

        return false;
    }

    public void updateScanListView(WifiInfo theInformation, NetworkInfo.DetailedState aState) {

        MyScanResult aScanResult;

        /*Find the proper scan result*/
        for (int i = 0; i < arrL_MyScanResult.size(); i++) {

            aScanResult = arrL_MyScanResult.get(i);

            if (aScanResult.getScanData().BSSID.equals(theInformation.getBSSID())) {
                arrL_MyScanResult.remove(i); /*Remove the scan list*/
                aScanResult.setTheConnectionDetail(aState);
                arrL_MyScanResult.add(0, aScanResult); /*put it on the top of the list*/
                myAdapter.notifyDataSetChanged(); /*notified the change*/
            }

        }

        if (aState == NetworkInfo.DetailedState.CONNECTED) {
            autoDismiss.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 1000);
        }


    }


    private class MyListAdapter extends ArrayAdapter<MyScanResult> {

        private int resource;
        private LayoutInflater mLayoutInflater;


        public MyListAdapter(Context ctx, int resourceId, List<MyScanResult> objects) {

            super(ctx, resourceId, objects);
            resource = resourceId;
            mLayoutInflater = LayoutInflater.from(ctx);
            autoDismiss = new Handler();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = mLayoutInflater.inflate(resource, null);

            MyScanResult aScanResult = getItem(position);

            TextView tvSSID = (TextView) convertView.findViewById(R.id.tv_bluetooth_device_name);
            TextView tvConnectionState = (TextView) convertView.findViewById(R.id.tv_bluetooth_device_state);
            ImageView IvIcon = (ImageView) convertView.findViewById(R.id.iv_bluetooth_icon);

            tvSSID.setText(aScanResult.getScanData().SSID);

            if (aScanResult.getTheConnectionDetail() != null) {

                tvConnectionState.setVisibility(View.VISIBLE);
                switch (aScanResult.getTheConnectionDetail()) {
                    case OBTAINING_IPADDR:
                        tvConnectionState.setText("Obtaining address Ip");
                        break;
                    case CONNECTED:
                        tvConnectionState.setText("Connected");
                        break;
                    case FAILED:
                        tvConnectionState.setText("Failed to connect to the network");
                        break;
                }

            } else {
                tvConnectionState.setVisibility(View.GONE);
            }

             /*Network is open*/
            if (!aScanResult.getScanData().capabilities.contains("WEP") && !aScanResult.getScanData().capabilities.contains("WPA")) {

                 /*Display the right icon following the power signal of the scan */
                switch (aScanResult.getSignalLevel()) {
                    case 0:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_open_0_bar);
                        break;
                    case 1:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_open_1_bar);
                        break;
                    case 2:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_open_2_bar);
                        break;
                    case 3:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_open_3_bar);
                        break;
                    case 4:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_open_4_bar);
                        break;
                }

            } else {        /*Network is lock*/


                switch (aScanResult.getSignalLevel()) {
                    case 0:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_lock_1_bar);
                        break;
                    case 1:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_lock_1_bar);
                        break;
                    case 2:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_lock_2_bar);
                        break;
                    case 3:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_lock_3_bar);
                        break;
                    case 4:
                        IvIcon.setImageResource(R.drawable.api_wifi_signal_lock_4_bar);
                        break;
                }
            }

            return convertView;
        }
    }

}