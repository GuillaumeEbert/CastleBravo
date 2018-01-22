package API_Com.Modules.Bluetooth.Threads;

import android.util.Log;

import API_Com.Modules.Bluetooth.Bluetooth;
import API_Com.Modules.Bluetooth.MyBluetoothDevice;
import API_Com.Modules.Bluetooth.Threads.BluetoothThread;
import API_Com.Modules.ThreadMessage;

/**
 * Thread for the data received by the bluetooth
 */
public class BluetoothReceivedDataThread extends BluetoothThread {

    public BluetoothReceivedDataThread(Bluetooth aModule) {
        super(aModule);
    }

    @Override
    public void run() {
        super.run();

        MyBluetoothDevice myCurrentDevice = myBtModule.getMyDevice();

        Log.d("BT_thread_Data", "START");

        while (canRun.get()) {

            if (myCurrentDevice != null) {

                if (!isPaused.get() && myCurrentDevice.getState().equals(MyBluetoothDevice.BluetoothState.CONNECTED)) {

                    try {

                        myBtModule.listenForData();
                        sendAInterThreadMessage(ThreadMessage.ThreadMessageKey.LISTEN_DATA_KEY.name(), new ThreadMessage(ThreadMessage.ThreadMessageState.DATA_LISTEN));

                    } catch (Exception e) {
                        sendAInterThreadMessage(ThreadMessage.ThreadMessageKey.LISTEN_DATA_KEY.name(), new ThreadMessage(ThreadMessage.ThreadMessageState.DATA_LISTEN_FAILED, e));
                    }

                }
            }

        }

        Log.d("BT_thread_Data", "OVER");

    }
}
