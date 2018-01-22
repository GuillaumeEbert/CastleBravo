package API_Com.Modules.Bluetooth.Threads;

import API_Com.Modules.Bluetooth.Bluetooth;
import API_Com.Modules.Bluetooth.Threads.BluetoothThread;

/**
 * Thread call to send data to a remote bluetooth device
 */
public class BluetoothSendDataThread extends BluetoothThread {

    private String dataToSend;

    public BluetoothSendDataThread(Bluetooth aModule, String dataToSend) {
        super(aModule);
        this.dataToSend = dataToSend;
    }


    @Override
    public void run() {
        super.run();

        myBtModule.doSend(dataToSend);
    }
}
