package API_Com.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import API_Com.Modules.ModuleManager;
import API_Com.Modules.Serial.Serial;

/**
 * Created by Guillaumee on 28/04/2016.
 */
public class BR_Serial extends BroadCastReceiverSc {

    private Serial mySerialModule;

    public BR_Serial(Serial aSerialModule) {
        super();
        mySerialModule = aSerialModule;

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        /*Action a device is attached*/
        if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            Log.d("BR_USB", "A USB device is attached");
            UsbDevice aDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            mySerialModule.getMyAlertNoDeviceConnected().dismiss();
            mySerialModule.getTheUsbManager().requestPermission(aDevice, mySerialModule.getMyPermissionIntent());
            mySerialModule.setMyUsbDevice(aDevice);

        } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
            Log.d("BR_USB", "A USB device is detached");
            mySerialModule.disconnect();

            /*Handle permission*/
        } else if (intent.getAction().equals(Serial.ACTION_USB_PERMISSION)) {

            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            Boolean isPermissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);

            if (isPermissionGranted) {

                if (device != null) {
                    mySerialModule.startConnection();
                }

            } else {
                mySerialModule.onPermissionDenied();

            }

        }


    }
}
