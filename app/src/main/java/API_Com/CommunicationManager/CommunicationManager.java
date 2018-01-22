package API_Com.CommunicationManager;

import android.app.Activity;
import API_Com.CommunicationManager.CommunicationListener.ComManagerListener;
import API_Com.Modules.ModuleManager;

/**
 *Wrapper for the modules. Instantiated this class for taking control of one of the modules.
 * This class is a singleton it exist only one in the whole app. For accessed to it call the getInstance method
 *
 *
 */
public class CommunicationManager {

    private static ModuleManager theModuleManager;
    private static CommunicationManager ourInstance = new CommunicationManager();

    private CommunicationManager() {
        theModuleManager = new ModuleManager();
    }

    /**
     * Return the instance of the communication manager
     *
     * @param anActivity the activity which has the focus
     * @return the instance of the communication manager
     */
    public static CommunicationManager getInstance(Activity anActivity) {
        theModuleManager.setTheCurrentActivity(anActivity);
        return ourInstance;
    }

    /**
     * This method must be used to open a communication in Wifi, bluetooth or Serial.
     * @param param the parameter to pass for open a com module
     *              for WifiModule param must be  set like this : "WIFI:serverIp='Address ip of the remote server',serverPort='port of the server',timeOut='Your connection timeout'"
     *              for Bluetooth param must be set like this : "BLUETOOTH:"
     *              for Serial param must be set like this : "SERIAL:baudRate='9600,57600,115200,460800'"
     *
     * @return the id of the opened module
     * @throws Exception if the string isn't in the right format.
     */
    public int openCom(String param) throws Exception{
        return theModuleManager.openCom(param);
    }

    /**
     * This method must be used to send data to the remote device connected.
     * @param id The id given by the openCom method.
     * @param sToSend Data to send
     * @throws Exception if an errors occurs
     */
    public void send(int id, String sToSend) throws Exception{
        theModuleManager.send(id,sToSend);
    }

    /**
     * This method must be call when you want to close one of thh three module( Wifi,Bluetooth,Serial)
     * @param idToClose The id given by the openCom method. Close the module associate to this id
     */
    public void closeCom(int idToClose){
        theModuleManager.closeCom(idToClose);
    }

    /**
     * This method must be implemented into your code to received the event of the communication manager
     * @see CommunicationListener for more details of the event
     * @param listener the listener
     */
    public void setOnComManagerListener(ComManagerListener listener){
        theModuleManager.setOnComManagerListener(listener);
    }

    /**
     * Enum that represent one of the three modules
     */
    public enum ModuleType {
        WIFI, BLUETOOTH, SERIAL
    }

    /**
     * Enum that represent the state of an module.
     */
    public enum ModuleState {
        CONNECTED, FAILED, DISCONNECTED, LISTEN_FOR_DATA,
    }
}
