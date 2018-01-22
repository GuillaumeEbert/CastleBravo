package API_Com.Modules.Wifi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import API_Com.BroadcastReceiver.BR_Wifi;
import API_Com.CommunicationManager.CommunicationManager;
import API_Com.DialogAndAlert.Alert.GenericAlert;
import API_Com.DialogAndAlert.Dialog.WifiDialogSelectNetwork;
import API_Com.Modules.AbstractComModule;
import API_Com.Modules.ModuleManager;
import API_Com.Modules.ThreadMessage;
import API_Com.DialogAndAlert.Alert.AlertSc.AlertUserAction;

import API_Com.Modules.Wifi.Threads.WifiReceivedDataThread;
import API_Com.Modules.Wifi.Threads.WifiSendDataThread;
import API_Com.Modules.Wifi.Threads.WifiConnectionThread;
import fr.telecom_physique.castlebravo.R;


/**
 * Created by Guillaumee on 26/03/2016.
 */
public class Wifi extends AbstractComModule {

    private Socket mySocket;
    private BR_Wifi myBR_Wifi;
    private int connectionTimeOut;
    private WifiManager theWifiManager;
    private WifiSocketState mySocketState;
    private SocketAddress theSocketAddress;
    private GenericAlert myAlertWifiDisable;
    private WifiSendDataThread mySendDataThread;
    private WifiConnectionThread myConnectionThread;
    private GenericAlert myAlertDisconnectFromServer;
    private WifiReceivedDataThread myReceivedDataThread;
    private WifiDialogSelectNetwork myDialogWifiScanResult;

    public final static String ALERT_WIFI_DISABLE_DISPLAY_KEY = "Alert_No_Wifi";
    public final static String DIALOG_SCAN_RESULT_DISPLAY_KEY = "Wifi_dialog_scan";
    public final static String DIALOG_CONNECT_TO_NETWORK_DISPLAY_KEY = "Dialog_connect";
    public final static String ALERT_DISCONNECTED_SERVER_DISPLAY_KEY = "Alert_socket_disconnect_server";

    /**
     * Wifi module constructor
     *
     * @param instComManager the module manager instance
     * @param anActivity     the activity on which this module is running
     */
    public Wifi(ModuleManager instComManager, Activity anActivity) {
        super(instComManager, anActivity);

        theWifiManager = (WifiManager) this.anActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        iFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        iFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        iFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        iFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);

        /*Create broadcast receiver and register it*/
        myBR_Wifi = new BR_Wifi(this, theWifiManager);

        myDialogWifiScanResult = WifiDialogSelectNetwork.newInstance();

        myAlertWifiDisable = GenericAlert.newInstance(bundleAlertWifiDisable());
        myAlertWifiDisable.setOnNoticeListener(this);

        myAlertDisconnectFromServer = GenericAlert.newInstance(bundleAlertDisconnectFromServer());
        myAlertDisconnectFromServer.setOnNoticeListener(this);

    }

    /**
     * @see AbstractComModule
     */
    @Override
    public void connect() {

        myBR_Wifi.registerMe(this.anActivity, iFilter);

        if (!theWifiManager.isWifiEnabled()) {
            myAlertWifiDisable.display(ALERT_WIFI_DISABLE_DISPLAY_KEY, this.anActivity);
            mySocketState = WifiSocketState.NO_NETWORK;

        } else if (theWifiManager.getConnectionInfo().getSupplicantState() != SupplicantState.COMPLETED) { /*wifi isn't connected to a networ*/
            myDialogWifiScanResult.display(DIALOG_SCAN_RESULT_DISPLAY_KEY, this.anActivity);
            mySocketState = WifiSocketState.NO_NETWORK;

        } else {    /*Wifi connected to a network*/
            mySocketState = WifiSocketState.DISCONNECTED;

        }

    }

    /**
     * @see AbstractComModule
     */
    @Override
    public void startConnection() {

        getMyIncomingDataBuffer().clear(); /* No old data on a new connection */
        myConnectionThread = new WifiConnectionThread(this);
        myConnectionThread.start();


    }


    /**
     * @see AbstractComModule
     */
    @Override
    public void doConnection() throws Exception {
        mySocket = new Socket();
        mySocket.connect(theSocketAddress, connectionTimeOut);

        /*Create input reader and output buffer*/
        myPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mySocket.getOutputStream())));
        myInputStream = mySocket.getInputStream();

    }

    /**
     * @see AbstractComModule
     */
    @Override
    protected void onConnectionFailed(String theReason) {
        Log.d("Connection_Failed", theReason);
        myBR_Wifi.unregisterMe(anActivity);

        callAComManagerListener().onConnection(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.FAILED, theReason);

        theModuleManager.getOpenComList().remove(myId);

    }


    /**
     * @see AbstractComModule
     */
    @Override
    public void disconnect() {
        try {
            if (mySocketState.equals(WifiSocketState.CONNECTED)) {
                if (myReceivedDataThread != null) {
                    myReceivedDataThread.getScheduleTaskExecutorDisconnectionFromServer().shutdownNow();
                }
                mySocket.shutdownOutput();
                mySocket.shutdownInput();


                callAComManagerListener().onDisconnection(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.DISCONNECTED, null);

            }

            myReceivedDataThread.Run().set(false);
            myBR_Wifi.unregisterMe(anActivity);

        } catch (Exception e) {
            callAComManagerListener().onDisconnection(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.FAILED, e);

        }

    }


    private void onDisconnectedFromServer() {
        try {

            if (mySocketState.equals(WifiSocketState.DISCONNECTED_FROM_SERVER)) {
                myReceivedDataThread.Pause().set(true);
                mySocket.shutdownOutput();
                mySocket.shutdownInput();
                mySocketState = WifiSocketState.DISCONNECTED;
                myReceivedDataThread.Run().set(false);
                myAlertDisconnectFromServer.display(ALERT_DISCONNECTED_SERVER_DISPLAY_KEY, anActivity);
            }

        } catch (Exception e) {
            callAComManagerListener().onDisconnection(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.FAILED, e);
            myReceivedDataThread.Run().set(false);
        }
    }


    /**
     * @see AbstractComModule
     */
    @Override
    public void send(String sToSend) {
        mySendDataThread = new WifiSendDataThread(this, sToSend);
        mySendDataThread.start();

    }

    @Override
    public void doSend(String dataToSend) {
        if (mySocketState == WifiSocketState.CONNECTED) {

            myPrintWriter.print(dataToSend);
            myPrintWriter.flush();
        }

    }

    public void startListenForData() {
        myReceivedDataThread = new WifiReceivedDataThread(this);
        myReceivedDataThread.start();
    }

    /**
     * @see AbstractComModule
     */
    @Override
    public int listenForData() throws IOException {
        int data;
        int value = myInputStream.read(myBuffer);
        //
        for (int i = 0; i < value; i++) {
            data = myBuffer[i];
            getMyIncomingDataBuffer().add(data);
        }

        System.out.println(value);
        return value;

    }

    /**
     * @see AbstractComModule
     */
    @Override
    public void parametersChecker(String sToAnalyze) throws Exception {
        syntaxAnalyzer(sToAnalyze);
        splitParam(sToAnalyze);
    }


    /**
     * @see AbstractComModule
     */
    @Override
    protected void syntaxAnalyzer(String sToAnalyze) throws Exception {
        if (!sToAnalyze.contains("serverIp="))
            throw new Exception("WIFI configuration : sub parameter 'serverIp=' missing");
        else if (!sToAnalyze.contains(",serverPort="))
            throw new Exception("WIFI configuration : sub parameter ',serverPort=' missing");
        else if (!sToAnalyze.contains(",timeOut="))
            throw new Exception("WIFI configuration : sub parameter ',timeOut=' missing");

    }


    /**
     * @see AbstractComModule
     */
    @Override
    protected void splitParam(String sToSplit) {
        int posEqual;
        int posComma;
        int theServerPort;
        String theServerAddress;

        posEqual = sToSplit.indexOf("=", 0);
        posComma = sToSplit.indexOf(",", 0);

        /*Fetch the ipAddress */
        theServerAddress = sToSplit.substring(posEqual + 1, posComma);

        posEqual = sToSplit.indexOf("=", posComma);
        posComma = sToSplit.indexOf(",", posEqual);

         /*Fetch the port id */
        theServerPort = Integer.parseInt(sToSplit.substring(posEqual + 1, posComma));

        posEqual = sToSplit.indexOf("=", posComma);

        /*Fetch the time out*/
        connectionTimeOut = Integer.parseInt(sToSplit.substring(posEqual + 1, sToSplit.length()));

         /*Build the socket address */
        theSocketAddress = new InetSocketAddress(theServerAddress, theServerPort);

    }

    /**
     * Message receiver from the wifiThread
     *
     * @param msg Message received from the wifi thread
     * @return A boolean
     */
    @Override
    public boolean handleMessage(Message msg) {
        ThreadMessage aInterThreadMessage;

        /*Message about the connection*/
        if (msg.getData().containsKey(ThreadMessage.ThreadMessageKey.CONNECTION_KEY.name())) {

            aInterThreadMessage = msg.getData().getParcelable(ThreadMessage.ThreadMessageKey.CONNECTION_KEY.name());

            switch (aInterThreadMessage.getState()) {
                case CONNECTED:
                    callAComManagerListener().onConnection(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.CONNECTED, null);
                    break;

                case CONNECTION_FAILED:
                    onConnectionFailed(aInterThreadMessage.getException().getMessage());
                    break;

                case DISCONNECTED_FROM_SERVER:
                    onDisconnectedFromServer();
                    break;

                case DISCONNECTED:
                    this.disconnect();
                    break;
            }

        }

        /*Message about the incoming data*/
        if (msg.getData().containsKey(ThreadMessage.ThreadMessageKey.LISTEN_DATA_KEY.name())) {
            aInterThreadMessage = msg.getData().getParcelable(ThreadMessage.ThreadMessageKey.LISTEN_DATA_KEY.name());


            switch (aInterThreadMessage.getState()) {
                case DATA_LISTEN:
                    callAComManagerListener().onDataReceived(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.LISTEN_FOR_DATA, getMyIncomingDataBuffer(), null);
                    getMyIncomingDataBuffer().clear();
                    break;

                case DATA_LISTEN_FAILED:
                    callAComManagerListener().onDataReceived(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.FAILED, null, aInterThreadMessage.getException());
            }
        }
        return true;
    }


    /**
     * Create the bundle for the alert dialog which will indicate that the wifi is disable
     *
     * @return the bundle containing all the information for building an alert
     */
    private Bundle bundleAlertWifiDisable() {
        Bundle theBundle = new Bundle();

        theBundle.putInt(GenericAlert.KEY_ICON, R.drawable.api_wifi_no_signal);
        theBundle.putInt(GenericAlert.KEY_TITTLE, R.string.wifi_dialog_alert_tittle);
        theBundle.putInt(GenericAlert.KEY_POSITIVE_BTN, R.string.dialog_generic_button_yes);
        theBundle.putInt(GenericAlert.KEY_NEGATIVE_BTN, R.string.dialog_generic_button_no);
        theBundle.putInt(GenericAlert.KEY_MESSAGE, R.string.alert_no_wifi_message);

        return theBundle;
    }

    /**
     * Create the bundle for the alert dialog which will indicate when the socket client is disconnect from the server
     *
     * @return the bundle containing all the information for building an alert
     */
    private Bundle bundleAlertDisconnectFromServer() {
        Bundle theBundle = new Bundle();

        theBundle.putInt(GenericAlert.KEY_TITTLE, R.string.wifi_dialog_alert_tittle);
        theBundle.putInt(GenericAlert.KEY_MESSAGE, R.string.alert_wifi_disconnect_server_message);
        theBundle.putInt(GenericAlert.KEY_POSITIVE_BTN, R.string.dialog_generic_button_yes);
        theBundle.putInt(GenericAlert.KEY_NEGATIVE_BTN, R.string.dialog_generic_button_no);

        return theBundle;
    }

    /**
     * Handle pressed button from the alerts dialogs     *
     *
     * @param dialog the dialog which is display
     * @param which  Type of button pressed
     */
    @Override
    public void onAlertDialogNotification(Dialog dialog, AlertUserAction which) {

        switch (which) {
            case BUTTON_POSITIVE:
                if (dialog.equals(myAlertWifiDisable.getTheDialog())) {
                    if (!theWifiManager.isWifiEnabled()) {
                        theWifiManager.setWifiEnabled(true);
                    }

                } else if (dialog.equals(myAlertDisconnectFromServer.getTheDialog())) {
                    startConnection();
                }
                break;

            case BUTTON_NEGATIVE:
            case FINGER_CANCEL:
                if (dialog.equals(myAlertWifiDisable.getTheDialog())) {
                    onConnectionFailed("Wifi is off");
                } else if (dialog.equals(myAlertDisconnectFromServer.getTheDialog())) {
                    if (mySocketState.equals(WifiSocketState.DISCONNECTED)) {
                        callAComManagerListener().onDisconnection(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.DISCONNECTED, null);
                        myReceivedDataThread.Run().set(false);
                    }
                }

                break;
        }

    }


    /**
     * Get the alert in charge of indicating to the user that the
     * wifi is disable
     *
     * @return an alert dialog
     */
    public GenericAlert AlertWifiDisable() {
        return myAlertWifiDisable;
    }

    /**
     * Get the dialog in charge of indicating the result of a wifi scan
     *
     * @return a dialog
     */
    public WifiDialogSelectNetwork getDialogScanResultWifi() {
        return myDialogWifiScanResult;
    }

    /**
     * Get a state of the wifi module
     *
     * @return a WifiSocketState
     */
    public WifiSocketState getMySocketState() {
        synchronized (this) {
            return mySocketState;
        }

    }

    /*
    Setter
     */

    public void setMySocketState(WifiSocketState myWifiSocketState) {
        synchronized (this) {
            this.mySocketState = myWifiSocketState;
        }

    }

    /*
    Enum
     */

    public enum WifiSocketState {
        NO_NETWORK, DISCONNECTED_FROM_SERVER, DISCONNECTED, CONNECTING, CONNECTED, CONNECTION_FAILED
    }


}
