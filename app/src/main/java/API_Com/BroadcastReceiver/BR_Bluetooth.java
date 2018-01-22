package API_Com.BroadcastReceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import API_Com.Modules.Bluetooth.Bluetooth;
import API_Com.Modules.Bluetooth.MyBluetoothDevice;
import API_Com.Modules.Bluetooth.MyBluetoothDevice.BluetoothState;
import API_Com.Modules.Bluetooth.MyBluetoothDevice.DeviceState;


/**
 * Broadcast receiver for the bluetooth module
 * Extends BroadCastReceiverSc
 */
public class BR_Bluetooth extends BroadCastReceiverSc {

    private Bluetooth myBluetoothModule;

    public BR_Bluetooth(Bluetooth aBluetoothModule) {
        super();
        myBluetoothModule = aBluetoothModule;

    }


    @Override
    public void onReceive(Context context, Intent intent) {

        /*State of the Bluetooth */
        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);

            switch (bluetoothState) {
                case BluetoothAdapter.STATE_ON:
                    Log.d("BR_BLUETOOTH", "STATE_ON");
                    myBluetoothModule.getMyBtDiscoveryDialog().display(Bluetooth.DIALOG_DISCOVERY_DISPLAY_KEY, context);

                    break;

                case BluetoothAdapter.STATE_OFF:
                    Log.d("BR_BLUETOOTH", "STATE_OFF");
                    myBluetoothModule.getMyAlertBluetoothDisable().display(Bluetooth.ALERT_BLUETOOTH_DISABLE_DISPLAY_KEY, context);
                    break;
            }
        }

        /*Start of an discovery*/
        else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
            myBluetoothModule.getMyBtDiscoveryDialog().setDiscoveryState(true);

        }

        /* a device was discovered*/
        else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            BluetoothDevice aDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (aDevice != null) {
                myBluetoothModule.getMyBtDiscoveryDialog().newDeviceFound(new MyBluetoothDevice(aDevice, BluetoothState.UN_PAIR, DeviceState.NEW_DEVICE));
            }

        }

        /*end of the discovery*/
        else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            myBluetoothModule.getMyBtDiscoveryDialog().setDiscoveryState(false);

        }

        /*Bounding is pending*/
        else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            BluetoothDevice aDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int btParingState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);

            switch (btParingState) {
                case BluetoothDevice.BOND_BONDING:
                    myBluetoothModule.getMyDevice().setState(BluetoothState.PAIRING);

                    myBluetoothModule.getMyBtDiscoveryDialog().updateLVs(aDevice, BluetoothState.PAIRING);
                    break;

                case BluetoothDevice.BOND_BONDED:
                    myBluetoothModule.getMyDevice().setState(BluetoothState.PAIRING);
                    myBluetoothModule.getMyBtDiscoveryDialog().updateLVs(aDevice, BluetoothState.PAIRED);
                    myBluetoothModule.startConnection();
                    Log.d("BT", "BOUND");
                    break;

            }

        }

    }
}
