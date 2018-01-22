package API_Com.Modules.Serial.Threads;

import API_Com.Modules.Serial.Serial;

/**
 * Thread for sending data
 */
public class SerialSendDataThread extends SerialThread {

    private String sToSend;


    public SerialSendDataThread(Serial aSerialToRun, String aString) {
        super(aSerialToRun);
        sToSend = aString;
    }

    @Override
    public void run() {
        super.run();

        mySerialToRun.doSend(sToSend);
    }
}
