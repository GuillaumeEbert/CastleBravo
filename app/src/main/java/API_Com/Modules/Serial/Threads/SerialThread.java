package API_Com.Modules.Serial.Threads;

import API_Com.Modules.ModuleThreadSc;
import API_Com.Modules.Serial.Serial;

/**
 * Super class for the Serial thread.
 */
public class SerialThread extends ModuleThreadSc {

    protected Serial mySerialToRun;

    public SerialThread(Serial aSerialToRun) {
        super();
        mySerialToRun = aSerialToRun;
        aThreadHandler = mySerialToRun.getThreadHandler();

    }

}
