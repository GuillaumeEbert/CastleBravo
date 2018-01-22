package API_Com.DialogAndAlert.Dialog;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import API_Com.Modules.Wifi.MyScanResult;
import fr.telecom_physique.castlebravo.R;

/**
 * The dialog to enter the password of an selected wifi network.
 */
public class WifiDialogConnectNetwork extends DialogSc {

    private Bundle myBundle;
    private Button btnCancel;
    private Button btnConnected;
    private EditText etPassword;
    private CheckBox cbShowPassword;
    private WifiManager theWifiManager;
    private String NetworkCapabilities;
    private MyScanResult theScanSelected;
    private RelativeLayout myPasswordLayout;


    public static WifiDialogConnectNetwork newInstance(MyScanResult theScanSelected) {

        WifiDialogConnectNetwork fragment = new WifiDialogConnectNetwork();
        fragment.setStyle(STYLE_NORMAL, R.style.CustomDialog);

        Bundle aBundle = new Bundle();
        aBundle.putParcelable("ScanSelected", theScanSelected);

        fragment.setArguments(aBundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.api_wifi_dialog_connect_wifi, container, false);

        myBundle = getArguments();
        theScanSelected = myBundle.getParcelable("ScanSelected");

        getDialog().setTitle(theScanSelected.getScanData().SSID);


        theWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        NetworkCapabilities = theScanSelected.getScanData().capabilities;
        Log.d("Scan Capalities", NetworkCapabilities);

        btnConnected = (Button) view.findViewById(R.id.btn_connect);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel_connect_dialog);
        cbShowPassword = (CheckBox) view.findViewById(R.id.cb_show_password);
        etPassword = (EditText) view.findViewById(R.id.et_password);

        myPasswordLayout = (RelativeLayout) view.findViewById(R.id.layout_connect_wifi_password);

        if (NetworkCapabilities.startsWith("[ESS]")) {
            myPasswordLayout.setVisibility(View.GONE);
        }

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnConnected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myPasswordLayout.getVisibility() == View.VISIBLE) {

                    if (etPassword.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "Password empty", Toast.LENGTH_LONG).show();
                    } else {
                        handleConnection();
                    }

                } else {
                    handleConnection();
                }
            }
        });

        cbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etPassword.setInputType(EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    etPassword.setSelection(etPassword.getText().length());
                } else {
                    /*TODO hidden password when uncheck*/
                }

            }
        });


    }

    @Override
    public void onStop() {
        super.onStop();
        isDisplay = false;

    }

    /**
     * handle the connection to the wifi network
     */
    private void handleConnection() {

        WifiConfiguration config = new WifiConfiguration();

        if (NetworkCapabilities.contains("WPA2")) { /*WPA2-PSK*/
            doTheConnection(networkWPA2_PSK_Security(config));

        } else if (NetworkCapabilities.startsWith("[ESS]")) { /*No security*/
            doTheConnection(networkNoSecurity(config));

        }

    }

    /**
     * Do the connection to a network id
     *
     * @param networkId the id of the network
     */
    private void doTheConnection(int networkId) {

        if (networkId != -1) {

            if (theWifiManager.enableNetwork(networkId, true)) {
                theWifiManager.saveConfiguration();
                dismiss();
            } else {
                Toast.makeText(getActivity(), "Error enable network", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(getActivity(), "Error config network", Toast.LENGTH_LONG).show();
        }


    }

    /**
     * Do the connection when the security is WAP2_PSK
     * @param config the configuration of the wifi
     * @return the id of the network
     */
    private int networkWPA2_PSK_Security(WifiConfiguration config) {
        config.SSID = "\"" + theScanSelected.getScanData().SSID + "\"";
        config.preSharedKey = "\"" + etPassword.getText().toString() + "\"";

        return theWifiManager.addNetwork(config);
    }

    /**
     * Handle the connection to an open wifi network
     * @param config the configuration of the wifi
     * @return the if of the network
     */
    private int networkNoSecurity(WifiConfiguration config) {
        config.SSID = "\"" + theScanSelected.getScanData().SSID + "\"";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        return theWifiManager.addNetwork(config);
    }

}

