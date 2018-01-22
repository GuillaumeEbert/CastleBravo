package API_Com.Modules.Serial;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Message;

import java.util.HashMap;
import java.util.Iterator;

import API_Com.BroadcastReceiver.BR_Serial;
import API_Com.CommunicationManager.CommunicationManager;
import API_Com.DialogAndAlert.Alert.AlertSc;
import API_Com.DialogAndAlert.Alert.GenericAlert;
import API_Com.Modules.AbstractComModule;
import API_Com.Modules.ModuleManager;
import API_Com.Modules.Serial.Threads.SerialConnectionThread;
import API_Com.Modules.Serial.Threads.SerialReceivedDataThread;
import API_Com.Modules.Serial.Threads.SerialSendDataThread;
import API_Com.Modules.ThreadMessage;
import fr.telecom_physique.castlebravo.R;

/**
 * Created by Guillaumee on 28/04/2016.
 */
public class Serial extends AbstractComModule {

    public final static String ALERT_NO_DEVICE_CONNECTED_DISPLAY_KEY = "Alert_no_serial_device";
    public final static String ALERT_TO_MUCH_DEVICES_CONNECTED_DISPLAY_KEY = "Alert_to_much_devices";
    public final static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private int myBaudRate;
    private int myDataBits;
    private UsbEndpoint myEpIn;
    private UsbEndpoint myEpOut;
    private BR_Serial myBR_Serial;
    private UsbDevice myUsbDevice;
    private UsbManager theUsbManager;
    private PendingIntent myPermissionIntent;
    private UsbDeviceConnection myUsbConnection;
    private GenericAlert myAlertNoDeviceConnected;
    private GenericAlert myAlertToMuchDevice;
    private SerialSendDataThread mySendDataThread;
    private HashMap<String, UsbDevice> myDeviceList;
    private SerialConnectionThread myConnectionThread;
    private SerialReceivedDataThread myReceivedDataThread;
    private SerialState myState;
    private UsbInterface myUsbInterface;

    /**
     * Constructor
     *
     * @param moduleManager instance of the ModuleManager
     * @param anActivity    instance of the activity that has the focus
     */
    public Serial(ModuleManager moduleManager, Activity anActivity) {
        super(moduleManager, anActivity);

        myPermissionIntent = PendingIntent.getBroadcast(anActivity, 0, new Intent(ACTION_USB_PERMISSION), 0);

        myBR_Serial = new BR_Serial(this);
        iFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        iFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        iFilter.addAction(Serial.ACTION_USB_PERMISSION);

        myAlertNoDeviceConnected = GenericAlert.newInstance(bundleAlertSerialNoDevice());
        myAlertNoDeviceConnected.setOnNoticeListener(this);

        myAlertToMuchDevice = GenericAlert.newInstance(bundleAlertSerialToMuchDevice());
        myAlertToMuchDevice.setOnNoticeListener(this);


        theUsbManager = (UsbManager) anActivity.getSystemService(Context.USB_SERVICE);

        myState = SerialState.NO_DEVICE;

    }

    @Override
    public void connect() {

        myBR_Serial.registerMe(anActivity, iFilter);

        myDeviceList = theUsbManager.getDeviceList();

        if (myDeviceList.size() == 0) {
            myAlertNoDeviceConnected.display(ALERT_NO_DEVICE_CONNECTED_DISPLAY_KEY, anActivity);
        } else if (myDeviceList.size() > 1) {
            myAlertToMuchDevice.display(ALERT_TO_MUCH_DEVICES_CONNECTED_DISPLAY_KEY, anActivity);
            myState = SerialState.TO_MUCH_DEVICE;

        } else {
            /* A device is connected*/
            Iterator<UsbDevice> deviceIterator = myDeviceList.values().iterator();

            while (deviceIterator.hasNext()) {
                myUsbDevice = deviceIterator.next();
                theUsbManager.requestPermission(myUsbDevice, myPermissionIntent);

            }
        }

    }

    public void onPermissionDenied() {
        myBR_Serial.unregisterMe(anActivity);
        callAComManagerListener().onConnection(CommunicationManager.ModuleType.SERIAL, myId, CommunicationManager.ModuleState.FAILED, "Permission denied");
    }

    @Override
    public void startConnection() {
        myConnectionThread = new SerialConnectionThread(this);
        myConnectionThread.start();

    }

    @Override
    public void doConnection() throws Exception {

        setMyUsbConnection(theUsbManager.openDevice(myUsbDevice));
        myUsbInterface = myUsbDevice.getInterface(0);

        getMyUsbConnection().claimInterface(myUsbDevice.getInterface(0), true);

        setControlTransfer();


        for (int i = 0; i < myUsbInterface.getEndpointCount(); i++) {
            if (myUsbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {

                if (myUsbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
                    myEpIn = myUsbInterface.getEndpoint(i);

                else
                    myEpOut = myUsbInterface.getEndpoint(i);
            }
        }
    }

    @Override
    protected void onConnectionFailed(String theReason) {
        myBR_Serial.unregisterMe(anActivity);
        callAComManagerListener().onConnection(CommunicationManager.ModuleType.SERIAL, myId, CommunicationManager.ModuleState.FAILED, theReason);

    }

    @Override
    public void disconnect() {
        myBR_Serial.unregisterMe(anActivity);
        myReceivedDataThread.Run().set(false);

        myUsbConnection.releaseInterface(myUsbInterface);
        myUsbConnection.close();

        callAComManagerListener().onDisconnection(CommunicationManager.ModuleType.SERIAL, myId, CommunicationManager.ModuleState.DISCONNECTED, null);

    }

    @Override
    public void send(String sToSend) {
        mySendDataThread = new SerialSendDataThread(this, sToSend);
        mySendDataThread.start();


    }

    @Override
    public void doSend(String dataToSend) {
        byte[] bytesToTransmit = dataToSend.getBytes();

        getMyUsbConnection().bulkTransfer(myEpOut, bytesToTransmit, bytesToTransmit.length, 500);

    }

    public void startListen() {
        myReceivedDataThread = new SerialReceivedDataThread(this);
        myReceivedDataThread.start();

    }

    @Override
    public int listenForData() throws Exception {
        byte[] buffer = new byte[4096];
        int data;
        int state = 0;

        if (getMyUsbConnection().bulkTransfer(myEpIn, buffer, 4096, 500) >= 0) {

            for (int i = 0; i < 4096; i++) {
                if (buffer[i] != 0) {
                    data = buffer[i];

                    send(new String(buffer, "UTF-8"));
                    getMyIncomingDataBuffer().add(data);

                }
                state = 1;

            }

        }

        return state;
    }

    @Override
    public void parametersChecker(String sToAnalyze) throws Exception {
        syntaxAnalyzer(sToAnalyze);
        splitParam(sToAnalyze);

    }

    @Override
    protected void syntaxAnalyzer(String sToAnalyze) throws Exception {
        if (!sToAnalyze.contains("baudRate="))
            throw new Exception("Serial configuration : sub parameter 'baudRate=' missing");


    }

    @Override
    protected void splitParam(String sToSplit) throws Exception {
        int posEqual;

        String theBaudRate;

        posEqual = sToSplit.indexOf("=", 0);

        /*Fetch the ipAddress */
        theBaudRate = sToSplit.substring(posEqual + 1, sToSplit.length());

        setMyBaudRate(theBaudRate);

    }

    @Override
    public boolean handleMessage(Message msg) {
        ThreadMessage aInterThreadMessage;

        if (msg.getData().containsKey(ThreadMessage.ThreadMessageKey.CONNECTION_KEY.name())) {

            aInterThreadMessage = msg.getData().getParcelable(ThreadMessage.ThreadMessageKey.CONNECTION_KEY.name());

            switch (aInterThreadMessage.getState()) {

                case CONNECTED:
                    callAComManagerListener().onConnection(CommunicationManager.ModuleType.SERIAL, myId, CommunicationManager.ModuleState.CONNECTED, null);
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
                    callAComManagerListener().onDataReceived(CommunicationManager.ModuleType.SERIAL, myId, CommunicationManager.ModuleState.LISTEN_FOR_DATA, getMyIncomingDataBuffer(), null);
                    getMyIncomingDataBuffer().clear();
                    break;

                case DATA_LISTEN_FAILED:
                    callAComManagerListener().onDataReceived(CommunicationManager.ModuleType.SERIAL, myId, CommunicationManager.ModuleState.FAILED, null, aInterThreadMessage.getException());
            }
        }
        return true;
    }

    /**
     * Create the bundle for the alert dialog which
     * will be in charge of indicate to the user that
     * the bluetooth is disable
     *
     * @return the bundle containing all the information for building an alert
     */
    private Bundle bundleAlertSerialNoDevice() {
        Bundle theBundle = new Bundle();

        theBundle.putInt(GenericAlert.KEY_ICON, R.drawable.api_warning);
        theBundle.putInt(GenericAlert.KEY_TITTLE, R.string.api_serial_dialog_alert_tittle);
        theBundle.putInt(GenericAlert.KEY_NEGATIVE_BTN, R.string.dialog_generic_button_cancel);
        theBundle.putInt(GenericAlert.KEY_MESSAGE, R.string.api_serial_no_device_alert_message);

        return theBundle;
    }


    /**
     * Create the bundle for the alert dialog which
     * will be in charge of indicate to the user that
     * the bluetooth is disable
     *
     * @return the bundle containing all the information for building an alert
     */
    private Bundle bundleAlertSerialToMuchDevice() {
        Bundle theBundle = new Bundle();

        theBundle.putInt(GenericAlert.KEY_ICON, R.drawable.api_warning);
        theBundle.putInt(GenericAlert.KEY_TITTLE, R.string.api_serial_dialog_alert_tittle);
        theBundle.putInt(GenericAlert.KEY_NEGATIVE_BTN, R.string.dialog_generic_button_cancel);
        theBundle.putInt(GenericAlert.KEY_MESSAGE, R.string.api_serial_too_much_device_alert_message);

        return theBundle;
    }

    @Override
    public void onAlertDialogNotification(Dialog dialog, AlertSc.AlertUserAction which) {
        switch (which) {

            case BUTTON_NEGATIVE:
            case FINGER_CANCEL:
                onConnectionFailed("No USB device connected");
                break;
        }
    }

    private void setControlTransfer() {

        getMyUsbConnection().controlTransfer(UsbConstants.USB_TYPE_VENDOR, 0, 0, 0, null, 0, 0);
        //myUsbConnection.controlTransfer(UsbConstants.USB_TYPE_VENDOR, 0, 1, 0, null, 0, 0);
        getMyUsbConnection().controlTransfer(UsbConstants.USB_TYPE_VENDOR, 0, 2, 0, null, 0, 0);
        getMyUsbConnection().controlTransfer(UsbConstants.USB_TYPE_VENDOR, 0x02, 0x0000, 0, null, 0, 0);
        getMyUsbConnection().controlTransfer(UsbConstants.USB_TYPE_VENDOR, 0x03, myBaudRate, 0, null, 0, 0);
        getMyUsbConnection().controlTransfer(UsbConstants.USB_TYPE_VENDOR, 0x04, 0x0008, 0, null, 0, 0);



    }

    private void setMyBaudRate(String aS_BaudRate) throws Exception {

        int aBaudRate = Integer.parseInt(aS_BaudRate);

        switch (aBaudRate) {
            case 9600:
                myBaudRate = 0x4138;
                break;

            case 57600:
                myBaudRate = 0x0034;
                break;

            case 115200:
                myBaudRate = 0x001A;
                break;

            case 460800:
                myBaudRate = 0x4006;
                break;

            default:
                throw new Exception("baud rate not supported");

        }

    }


    public UsbDevice getMyUsbDevice() {
        return myUsbDevice;
    }

    public void setMyUsbDevice(UsbDevice myUsbDevice) {
        this.myUsbDevice = myUsbDevice;
    }

    public PendingIntent getMyPermissionIntent() {
        return myPermissionIntent;
    }

    public UsbManager getTheUsbManager() {
        return theUsbManager;
    }


    public UsbDeviceConnection getMyUsbConnection() {
        synchronized (this) {
            return myUsbConnection;

        }
    }

    public void setMyUsbConnection(UsbDeviceConnection myUsbConnection) {
        synchronized (this) {
            this.myUsbConnection = myUsbConnection;

        }

    }

    public SerialState getMyState() {
        synchronized (this) {
            return myState;
        }

    }

    public void setMyState(SerialState myState) {
        synchronized (this) {
            this.myState = myState;
        }
    }

    public GenericAlert getMyAlertNoDeviceConnected() {
        return myAlertNoDeviceConnected;
    }

    public enum SerialState {
        NO_DEVICE, ATTACHED, DETACHED, CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, TO_MUCH_DEVICE,
    }
}
