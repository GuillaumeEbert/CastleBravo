package API_Com.Modules.Wifi.Threads;

import API_Com.Modules.ModuleThreadSc;
import API_Com.Modules.Wifi.Wifi;

/**
 * Created by Guillaumee on 27/04/2016.
 */
public abstract class WifiThread extends ModuleThreadSc {

    protected Wifi myWifiToRun;

    public WifiThread(Wifi aModule) {
        super();
        myWifiToRun = aModule;
        aThreadHandler = myWifiToRun.getThreadHandler();
    }
}
