package API_Com.Modules.Bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * A Bluetooth device
 */
public class MyBluetoothDevice {

    private BluetoothDevice aBluetoothDevice;
    private BluetoothState myState;
    private DeviceState myDeviceState;


    public MyBluetoothDevice(BluetoothDevice aDevice, BluetoothState aState, DeviceState boundState) {
        aBluetoothDevice = aDevice;
        myState = aState;
        myDeviceState = boundState;
    }


    /**
     * Getter
     */
    public BluetoothState getState() {
        synchronized (this) {
            return myState;
        }

    }

    public BluetoothDevice getDevice() {
        return aBluetoothDevice;
    }

    public DeviceState getMyDeviceState() {
        return myDeviceState;
    }

    /**
     * Setter
     */
    public void setState(BluetoothState aNewState) {
        synchronized (this) {
            this.myState = aNewState;
        }

    }



    public enum BluetoothState {
        UN_PAIR, PAIRED, PAIRING, CONNECTING, CONNECTED, CONNECTION_FAILED, DISCONNECTING, DISCONNECTED
    }

    public enum DeviceState {
        NEW_DEVICE, ALREADY_PAIRED_DEVICE
    }


}
