package API_Com.Modules.Serial.Threads;

import API_Com.Modules.Serial.Serial;
import API_Com.Modules.ThreadMessage;

/**
 * Thread for the Serial connection
 */
public class SerialConnectionThread extends SerialThread {

    public SerialConnectionThread(Serial aSerialToRun) {
        super(aSerialToRun);
    }


    @Override
    public void run() {
        super.run();

        try {

            mySerialToRun.setMyState(Serial.SerialState.CONNECTING);

            mySerialToRun.doConnection();

            mySerialToRun.setMyState(Serial.SerialState.CONNECTED);

            sendAInterThreadMessage(ThreadMessage.ThreadMessageKey.CONNECTION_KEY.name(), new ThreadMessage(ThreadMessage.ThreadMessageState.CONNECTED));

            mySerialToRun.startListen();

        } catch (Exception e) {

        }

    }
}
