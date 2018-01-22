package API_Com.Modules;

import android.app.Activity;

import API_Com.CommunicationManager.CommunicationListener.ComManagerListener;

import java.util.ArrayList;

import API_Com.CommunicationManager.CommunicationManager;
import API_Com.Modules.Bluetooth.Bluetooth;
import API_Com.Modules.Serial.Serial;
import API_Com.Modules.Wifi.Wifi;


/**
 * Created by Guillaumee on 26/03/2016.
 */
public class ModuleManager {

    private Activity theCurrentActivity;
    private ArrayList<Object> openComList;
    private ComManagerListener comManagerListener;

    /**
     * Constructor
     */
    public ModuleManager() {
        openComList = new ArrayList<>();
    }


    /**
     * Open a communication
     *
     * @param sParam the string parameter which contain the module to open and it's configuration
     * @return the id of the communication
     * @throws Exception if the string parameter is wrong
     */
    public int openCom(String sParam) throws Exception {
        try {
            return rootToModule(sParam);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Close a communication
     *
     * @param idToClose id of the communication to close
     */
    public void closeCom(int idToClose) {

        Object aObject;

        if (openComList.size() > 0) {
            aObject = openComList.get(idToClose);
            if (aObject instanceof Wifi) ((Wifi) aObject).disconnect();
            if (aObject instanceof Bluetooth) ((Bluetooth) aObject).disconnect();
            if (aObject instanceof Serial) ((Serial) aObject).disconnect();

            openComList.remove(idToClose);
        }

    }

    /**
     * Send data using the right communication
     *
     * @param id      communication to use for the sending
     * @param sToSend data to send
     * @throws Exception if an error during the sending
     */
    public void send(int id, String sToSend) throws Exception {

        Object aObject;
        if (openComList.size() > 0) {
            aObject = openComList.get(id);
            if (aObject instanceof Wifi) ((Wifi) aObject).send(sToSend);
            if (aObject instanceof Bluetooth) ((Bluetooth) aObject).send(sToSend);
            if (aObject instanceof Serial) ((Serial) aObject).send(sToSend);

        }
    }



    /**
     * Take the first characters of the sParam string and root to the right module
     *
     * @param sParam the parameters
     * @return id of the open communication
     * @throws Exception if the module is unknowns or the parameters are wrong
     */
    private int rootToModule(String sParam) throws Exception {


        if (sParam.startsWith("WIFI:")) return handleWifiRoot(sParam);
        else if (sParam.startsWith("BLUETOOTH")) return handleBluetoothRoot(sParam);
        else if (sParam.startsWith("SERIAL")) return handleSerialRoot(sParam);
        else throw new Exception("Root argument invalid");


    }


    /**
     * Open the com for a Wifi module. Allow just one communication for this module
     *
     * @param sParam the string parameter
     * @return the id of the wifi communication
     * @throws Exception if the parameters are wrong
     */
    private int handleWifiRoot(String sParam) throws Exception {

        Wifi aWifiModule = new Wifi(this, theCurrentActivity);
        int id;


        if (listContainModule(CommunicationManager.ModuleType.WIFI))
            throw new Exception("WIFI communication already open. Close it");

        else {
            aWifiModule.parametersChecker(sParam);
            aWifiModule.connect();

            openComList.add(aWifiModule);
            id = openComList.indexOf(aWifiModule);

            aWifiModule.setMyId(id);

            return id;
        }

    }


    private int handleBluetoothRoot(String sParam) throws Exception {

        int id;
        Bluetooth aBluetoothModule = new Bluetooth(this, theCurrentActivity);

        if (listContainModule(CommunicationManager.ModuleType.BLUETOOTH))
            throw new Exception("Bluetooth communication already open. Close it");

        else {
            aBluetoothModule.parametersChecker(sParam);
            aBluetoothModule.connect();

            openComList.add(aBluetoothModule);
            id = openComList.indexOf(aBluetoothModule);

            aBluetoothModule.setMyId(id);
        }

        return id;
    }

    private int handleSerialRoot(String sParam) throws Exception {

        int id;
        Serial aSerialModule = new Serial(this, theCurrentActivity);

        if (listContainModule(CommunicationManager.ModuleType.SERIAL))
            throw new Exception("Serial communication already open. Close it");

        else {
            aSerialModule.parametersChecker(sParam);
            aSerialModule.connect();

            openComList.add(aSerialModule);
            id = openComList.indexOf(aSerialModule);

            aSerialModule.setMyId(id);
        }

        return id;
    }

    /**
     * Check if the module list contain a specified module
     *
     * @param sModuleToCheck enum that indicate which type module to test
     * @return true if the list contain the module false the otherwise
     */
    private boolean listContainModule(CommunicationManager.ModuleType sModuleToCheck) {

        Object aModule;

        for (int i = 0; i < openComList.size(); i++) {
            aModule = openComList.get(i);

            switch (sModuleToCheck) {
                case WIFI:
                    if (aModule instanceof Wifi) return true;
                    break;

                case BLUETOOTH:
                    break;

                case SERIAL:
                    break;
            }
        }
        return false;
    }


    public void setTheCurrentActivity(Activity theCurrentActivity) {
        this.theCurrentActivity = theCurrentActivity;
    }


    public ComManagerListener getComManagerListener() {
        return comManagerListener;
    }


    public ArrayList<Object> getOpenComList() {
        return openComList;
    }


    public void setOnComManagerListener(ComManagerListener listener) {
        comManagerListener = listener;
    }


}
