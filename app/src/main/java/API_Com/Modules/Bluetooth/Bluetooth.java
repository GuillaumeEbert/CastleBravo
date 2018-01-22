package API_Com.Modules.Bluetooth;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;


import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import API_Com.CommunicationManager.CommunicationManager;
import API_Com.DialogAndAlert.Alert.AlertSc;
import API_Com.DialogAndAlert.Alert.GenericAlert;
import API_Com.BroadcastReceiver.BR_Bluetooth;
import API_Com.DialogAndAlert.Dialog.BluetoothDialogDiscovery;
import API_Com.Modules.AbstractComModule;
import API_Com.Modules.Bluetooth.Threads.BluetoothConnectionThread;
import API_Com.Modules.Bluetooth.Threads.BluetoothReceivedDataThread;
import API_Com.Modules.Bluetooth.Threads.BluetoothSendDataThread;
import API_Com.Modules.ModuleManager;
import API_Com.Modules.ThreadMessage;
import API_Com.Modules.Bluetooth.MyBluetoothDevice.BluetoothState;
import fr.telecom_physique.castlebravo.R;


/**
 * Bluetooth module
 */
public class Bluetooth extends AbstractComModule implements BluetoothDialogDiscovery.OnDeviceSelectListener {

    private BluetoothSocket mySocket;
    private MyBluetoothDevice myDevice;
    private BR_Bluetooth myBR_Bluetooth;
    private BluetoothAdapter myBluetoothAdapter;
    private GenericAlert myAlertBluetoothDisable;
    private BluetoothSendDataThread mySendDataThread;
    private BluetoothConnectionThread myConnectionThread;
    private BluetoothDialogDiscovery myBtDiscoveryDialog;
    private BluetoothReceivedDataThread myReceivedDataThread;

    public final static String ALERT_BLUETOOTH_DISABLE_DISPLAY_KEY = "Alert_No_Bluetooth";
    public final static String DIALOG_DISCOVERY_DISPLAY_KEY = "Bluetooth_dialog_discovery";

    /**
     * Constructor
     *
     * @param theModuleManager
     * @param anActivity
     */
    public Bluetooth(ModuleManager theModuleManager, Activity anActivity) {
        super(theModuleManager, anActivity);

        BluetoothManager theBluetoothManager = (BluetoothManager) anActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        myBluetoothAdapter = theBluetoothManager.getAdapter();

        myThreadHandler = new Handler(this);

        myReceivedDataThread = new BluetoothReceivedDataThread(this);

        myBtDiscoveryDialog = BluetoothDialogDiscovery.newInstance();
        myBtDiscoveryDialog.setOnNoticeListener(this);
        myBtDiscoveryDialog.setOnDeviceSelectListener(this);

        myAlertBluetoothDisable = GenericAlert.newInstance(bundleAlertBluetoothDisable());
        myAlertBluetoothDisable.setOnNoticeListener(this);


        iFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        iFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        iFilter.addAction(BluetoothDevice.ACTION_FOUND);
        iFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        iFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        myBR_Bluetooth = new BR_Bluetooth(this);


    }

    /**
     * @see AbstractComModule
     */
    @Override
    public void connect() {

        myBR_Bluetooth.registerMe(anActivity, iFilter);

        if (!myBluetoothAdapter.isEnabled()) {
            myAlertBluetoothDisable.display(ALERT_BLUETOOTH_DISABLE_DISPLAY_KEY, anActivity);

        } else {
            myBtDiscoveryDialog.display(DIALOG_DISCOVERY_DISPLAY_KEY, anActivity);

        }

    }

    @Override
    public void startConnection() {
        getMyIncomingDataBuffer().clear(); /*Clean the buffer on a new connection */

        myConnectionThread = new BluetoothConnectionThread(this);
        myConnectionThread.start();

    }

    /**
     * @see AbstractComModule
     */
    @Override
    public void doConnection() throws Exception {
        if (myDevice != null) {
            ParcelUuid arrParcelUuid[] = myDevice.getDevice().getUuids();

            mySocket = myDevice.getDevice().createRfcommSocketToServiceRecord(arrParcelUuid[0].getUuid()); /*Get the socket*/

            mySocket.connect();

            /*Create input reader and output buffer*/
            myPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mySocket.getOutputStream())), true);
            myInputStream = mySocket.getInputStream();

        } else {
            throw new Exception("Device bounded is null");
        }

    }

    /**
     * @see AbstractComModule
     */
    @Override
    protected void onConnectionFailed(String theReason) {
        myBR_Bluetooth.unregisterMe(anActivity);
        callAComManagerListener().onConnection(CommunicationManager.ModuleType.BLUETOOTH, myId, CommunicationManager.ModuleState.FAILED, theReason);

        theModuleManager.getOpenComList().remove(myId);

    }

    /**
     * @see AbstractComModule
     */
    @Override
    public void disconnect() {
        try {
            if (myDevice.getState().equals(BluetoothState.CONNECTED)) {
                mySocket.close();
                callAComManagerListener().onDisconnection(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.DISCONNECTED, null);

            }

            myReceivedDataThread.Run().set(false);
            myBR_Bluetooth.unregisterMe(anActivity);

        } catch (Exception e) {
            callAComManagerListener().onDisconnection(CommunicationManager.ModuleType.WIFI, myId, CommunicationManager.ModuleState.FAILED, e);

        }

    }

    /**
     * @see AbstractComModule
     */
    @Override
    public void send(String sToSend) {
        mySendDataThread = new BluetoothSendDataThread(this, sToSend);
        mySendDataThread.start();

    }

    /**
     * @param dataToSend data to send to the remote server
     */
    @Override
    public void doSend(String dataToSend) {
        if (myDevice.getState().equals(BluetoothState.CONNECTED)) {
            myPrintWriter.println(dataToSend);
            myPrintWriter.flush();
        }
    }


    /**
     * @see AbstractComModule
     */
    @Override
    public int listenForData() throws Exception {
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
        /*No needed*/

    }

    /**
     * @see AbstractComModule
     */
    @Override
    protected void syntaxAnalyzer(String sToAnalyze) throws Exception {
       /*Not needed*/

    }

    /**
     * @see AbstractComModule
     */
    @Override
    protected void splitParam(String sToSplit) {
        /*No needed*/

    }

    /**
     * Handle the message coming from the threads
     *
     * @param msg message transmit from the thread
     * @return true when completed
     */
    @Override
    public boolean handleMessage(Message msg) {
        ThreadMessage aInterThreadMessage;

        /*Message about the connection*/
        if (msg.getData().containsKey(ThreadMessage.ThreadMessageKey.CONNECTION_KEY.name())) {

            aInterThreadMessage = msg.getData().getParcelable(ThreadMessage.ThreadMessageKey.CONNECTION_KEY.name());

            switch (aInterThreadMessage.getState()) {
                case CONNECTING:
                    myBtDiscoveryDialog.updateLVs(myDevice.getDevice(), BluetoothState.CONNECTING);
                    break;

                case CONNECTED:
                    myBtDiscoveryDialog.updateLVs(myDevice.getDevice(), BluetoothState.CONNECTED);
                    callAComManagerListener().onConnection(CommunicationManager.ModuleType.BLUETOOTH, myId, CommunicationManager.ModuleState.CONNECTED, null);
                    break;

                case CONNECTION_FAILED:
                    myBtDiscoveryDialog.updateLVs(myDevice.getDevice(), BluetoothState.CONNECTED);
                    onConnectionFailed(aInterThreadMessage.getException().getMessage());
                    break;

                case DISCONNECTED:
                    break;

                case DISCONNECTED_FROM_SERVER:
                    break;
            }

        }

        /*Message about the incoming data*/
        if (msg.getData().containsKey(ThreadMessage.ThreadMessageKey.LISTEN_DATA_KEY.name())) {
            aInterThreadMessage = msg.getData().getParcelable(ThreadMessage.ThreadMessageKey.LISTEN_DATA_KEY.name());

            switch (aInterThreadMessage.getState()) {
                case DATA_LISTEN:
                    callAComManagerListener().onDataReceived(CommunicationManager.ModuleType.BLUETOOTH, myId, CommunicationManager.ModuleState.LISTEN_FOR_DATA, getMyIncomingDataBuffer(), null);
                    getMyIncomingDataBuffer().clear();
                    break;

                case DATA_LISTEN_FAILED:
                    callAComManagerListener().onDataReceived(CommunicationManager.ModuleType.BLUETOOTH, myId, CommunicationManager.ModuleState.FAILED, null, aInterThreadMessage.getException());
            }
        }
        return true;
    }

    @Override
    public void onAlertDialogNotification(Dialog dialog, AlertSc.AlertUserAction which) {

        switch (which) {
            case BUTTON_POSITIVE:
                if (dialog.equals(myAlertBluetoothDisable.getDialog())) {
                    myBluetoothAdapter.enable();

                }
                break;

            case BUTTON_NEGATIVE:
                if (dialog.equals(myAlertBluetoothDisable.getTheDialog())) {
                    onConnectionFailed("Bluetooth is not active");

                } else if (dialog.equals(myBtDiscoveryDialog.getDialog())) {
                    onConnectionFailed("Bluetooth is not connected");

                }
                break;
        }

    }

    /**
     * Create the bundle for the alert dialog which
     * will be in charge of indicate to the user that
     * the bluetooth is disable
     *
     * @return the bundle containing all the information for building an alert
     */
    private Bundle bundleAlertBluetoothDisable() {
        Bundle theBundle = new Bundle();

        theBundle.putInt(GenericAlert.KEY_ICON, R.drawable.api_bluetooth_disable);
        theBundle.putInt(GenericAlert.KEY_TITTLE, R.string.api_bluetooth_dialog_alert_tittle);
        theBundle.putInt(GenericAlert.KEY_POSITIVE_BTN, R.string.dialog_generic_button_yes);
        theBundle.putInt(GenericAlert.KEY_NEGATIVE_BTN, R.string.dialog_generic_button_no);
        theBundle.putInt(GenericAlert.KEY_MESSAGE, R.string.api_bluetooth_alert_message);

        return theBundle;
    }

    /**
     * Get the thread in charge of the reception of the data
     *
     * @return a thread
     */
    public BluetoothReceivedDataThread getMyReceivedDataThread() {
        return myReceivedDataThread;
    }

    /**
     * Get the alert displayed when the bluetooth is not active
     *
     * @return the alert
     */
    public GenericAlert getMyAlertBluetoothDisable() {
        return myAlertBluetoothDisable;
    }


    /**
     * Get the dialog in charge of the selection of a bluetooth device
     *
     * @return a dialog
     */
    public BluetoothDialogDiscovery getMyBtDiscoveryDialog() {
        return myBtDiscoveryDialog;
    }

    /**
     * Get a bluetooth device
     *
     * @return a device
     */
    public MyBluetoothDevice getMyDevice() {
        return myDevice;
    }

    /**
     * Call back from the Bluetooth dialog discovery
     * Indicate that a new device was select by the user
     * and need to be bound.
     *
     * @param theDiscoveredDevice
     */
    @Override
    public void onSelectDiscoveredDevice(MyBluetoothDevice theDiscoveredDevice) {
        myDevice = theDiscoveredDevice;
        myDevice.getDevice().createBond();
        Log.d("Device", "In Connection");


    }

    /**
     * Call back from the Bluetooth dialog discovery
     * Indicate that a paired device was select by the user
     * The connection to the master bluetooth can be initiated
     *
     * @param thePairDevice
     */
    @Override
    public void onSelectPairDevice(MyBluetoothDevice thePairDevice) {

        myDevice = thePairDevice;
        startConnection();


    }

}
