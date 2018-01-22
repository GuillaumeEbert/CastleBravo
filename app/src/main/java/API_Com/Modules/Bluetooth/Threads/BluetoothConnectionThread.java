package API_Com.Modules.Bluetooth.Threads;


import android.util.Log;

import API_Com.Modules.Bluetooth.Bluetooth;
import API_Com.Modules.Bluetooth.MyBluetoothDevice;
import API_Com.Modules.ThreadMessage;
import API_Com.Modules.Bluetooth.MyBluetoothDevice.BluetoothState;
import API_Com.Modules.ThreadMessage.ThreadMessageKey;
import API_Com.Modules.ThreadMessage.ThreadMessageState;

/**
 * Thread for the connection to a remote bluetooth device
 */
public class BluetoothConnectionThread extends BluetoothThread {

    public BluetoothConnectionThread(Bluetooth aModule) {
        super(aModule);

    }

    @Override
    public void run() {
        super.run();

        Log.d("BT_thread_Connection", "START");

        MyBluetoothDevice myCurrentDevice = myBtModule.getMyDevice();

        if (myCurrentDevice != null) {

            if (myCurrentDevice.getState().equals(BluetoothState.PAIRED) && !isPaused.get()) {

                try {

                    myBtModule.getMyBtDiscoveryDialog().setCanRedoAnAnalyse(false); /*Stop the analyse to improve the connection performance*/

                    sendAInterThreadMessage(ThreadMessageKey.CONNECTION_KEY.name(), new ThreadMessage(ThreadMessageState.CONNECTING));

                    myBtModule.doConnection();

                    sendAInterThreadMessage(ThreadMessageKey.CONNECTION_KEY.name(), new ThreadMessage(ThreadMessageState.CONNECTED));

                    myBtModule.getMyReceivedDataThread().start();

                    Sleep(1000);
                    canRun.set(false);

                } catch (Exception e) {
                    sendAInterThreadMessage(ThreadMessageKey.CONNECTION_KEY.name(), new ThreadMessage(ThreadMessageState.CONNECTION_FAILED, e));

                    canRun.set(false);
                }
            } else {

                sendAInterThreadMessage(ThreadMessageKey.CONNECTION_KEY.name(), new ThreadMessage(ThreadMessageState.CONNECTION_FAILED, new Exception("Device not paired")));
            }

        }

        Log.d("BT_thread_Connection", "OVER");

    }
}
