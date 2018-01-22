package API_Com.Modules;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Handler;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import API_Com.CommunicationManager.CommunicationListener;
import API_Com.DialogAndAlert.NoticeAlertDialogListener;

/**
 * Created by Guillaumee on 26/03/2016.
 */
public abstract class AbstractComModule implements Handler.Callback, NoticeAlertDialogListener {

    protected int myId;
    protected Activity anActivity;
    protected Handler myThreadHandler;
    protected ModuleManager theModuleManager;
    /*The vector is used on multiple threads. Use only the getter and setter method to access it */
    protected Vector<Integer> myIncomingDataBuffer;
    protected byte[] myBuffer;
    protected InputStream myInputStream;
    protected PrintWriter myPrintWriter;
    protected IntentFilter iFilter;


    /**
     * Constructor
     *
     * @param moduleManager instance of the ModuleManager
     * @param anActivity instance of the activity that has the focus
     */
    protected AbstractComModule(ModuleManager moduleManager, Activity anActivity) {
        this.anActivity = anActivity;
        myThreadHandler = new Handler(this);
        myIncomingDataBuffer = new Vector<>();
        this.theModuleManager = moduleManager;
        myBuffer = new byte[1024];
        iFilter = new IntentFilter();

    }

    /**
     * Run on the UI thread do the pre-work for the background thread connection
     */
    public abstract void connect();

    /**
     * Create and start the thread in charge of the connection.
     * Must be call for each new connection
     */
    public abstract void startConnection();

    /**
     * Run on a background thread. Do the connection to a remote receiver
     *
     * @throws Exception
     */
    public abstract void doConnection() throws Exception;

    /**
     * Close properly when a connection error happen
     *
     * @param theReason give an indication of the failure
     */
    protected abstract void onConnectionFailed(String theReason);

    /**
     * Run on the UI thread to do the disconnection from the connected remote receiver
     */
    public abstract void disconnect();

    /**
     * Run on the UI thread.
     * Create and launch a unique background thread responsible to send the data passed in parameter
     *
     * @param sToSend data to pass to the thread
     */
    public abstract void send(String sToSend);

    /**
     * Run on a background thread, Send the data to the remote server
     *
     * @param dataToSend data to send to the remote server
     */
    public abstract void doSend(String dataToSend);

    /**
     * Run on a background thread. Receive the data from the connected server
     * <p/>
     * * @throws Exception
     */
    public abstract int listenForData() throws Exception;

    /**
     * Call by the comManager to check if the param string is ok
     *
     * @param sToAnalyze
     * @throws Exception if an errors occur
     */
    public abstract void parametersChecker(String sToAnalyze) throws Exception;

    /**
     * Check if the syntax of the string is ok
     *
     * @param sToAnalyze
     * @throws Exception
     */
    protected abstract void syntaxAnalyzer(String sToAnalyze) throws Exception;

    /**
     * Split a string to extra the right information needed for the module
     *
     * @param sToSplit
     */
    protected abstract void splitParam(String sToSplit) throws Exception;

    /**
     * Get the comManager listener
     *
     * @return the comManager listener
     */
    protected CommunicationListener.ComManagerListener callAComManagerListener() {
        return theModuleManager.getComManagerListener();
    }


    /**
     * Getter
     */

    public Vector<Integer> getMyIncomingDataBuffer() {
        synchronized (this) {
            return myIncomingDataBuffer;
        }
    }

    /**
     * Get the threads handler
     *
     * @return a Handler
     */
    public Handler getThreadHandler() {
        return myThreadHandler;
    }


    /**
     * Setter
     */

    public void setMyId(int myId) {
        this.myId = myId;
    }

}
