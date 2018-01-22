package API_Com.Modules.Serial.Threads;

import API_Com.Modules.Serial.Serial;
import API_Com.Modules.ThreadMessage;

/**
 * Thread for the  serial received data
 */
public class SerialReceivedDataThread extends SerialThread {

    public SerialReceivedDataThread(Serial aSerialToRun) {
        super(aSerialToRun);
    }

    @Override
    public void run() {
        super.run();

        while (canRun.get()) {

            try {
                if (mySerialToRun.listenForData() == 1) {
                    sendAInterThreadMessage(ThreadMessage.ThreadMessageKey.LISTEN_DATA_KEY.name(), new ThreadMessage(ThreadMessage.ThreadMessageState.DATA_LISTEN));
                    Sleep(100);
                }


            } catch (Exception e) {

                e.printStackTrace();
            }

        }
    }
}
