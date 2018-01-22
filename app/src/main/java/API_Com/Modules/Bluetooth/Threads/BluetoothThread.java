package API_Com.Modules.Bluetooth.Threads;

import API_Com.Modules.Bluetooth.Bluetooth;
import API_Com.Modules.ModuleThreadSc;

/**
 * Super class for the bluetooth thread
 */
public abstract class BluetoothThread extends ModuleThreadSc {

    protected Bluetooth myBtModule;

    public BluetoothThread(Bluetooth aModule) {
        super();
        myBtModule = aModule;
        aThreadHandler = myBtModule.getThreadHandler();
    }

}
