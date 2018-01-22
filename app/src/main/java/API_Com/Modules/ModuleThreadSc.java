package API_Com.Modules;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Guillaume.Ebert on 14/04/2016.
 */
public abstract class ModuleThreadSc extends Thread {

    protected AtomicBoolean canRun;
    protected AtomicBoolean isPaused;
    protected Handler aThreadHandler;

    public ModuleThreadSc() {
        canRun = new AtomicBoolean(true);
        isPaused = new AtomicBoolean(false);
    }

    /**
     * Send a message to another thread
     *
     * @param key      Key for id the message
     * @param aMessage The message to transmit
     */
    protected void sendAInterThreadMessage(String key, ThreadMessage aMessage) {
        Bundle messageBundle = new Bundle();
        Message myMessage;
        Handler theThreadHandler;

        theThreadHandler = aThreadHandler;
        myMessage = theThreadHandler.obtainMessage();

        messageBundle.putParcelable(key, aMessage);
        myMessage.setData(messageBundle);
        theThreadHandler.sendMessage(myMessage);

    }

    /**
     * Put the Thread in a wait state
     *
     * @throws Exception when the thread wait has been interrupted
     */
    protected void Wait() {

        try {

            while (canRun.get() && isPaused.get()) {

                sleep(250);
            }
        } catch (Exception e) {

            isPaused.set(false);
        }
    }


    public void Sleep(int duration) {
        try {
            sleep(duration);
        } catch (Exception e) {

        }
    }


    /**
     * Get and set if the thread can run or not
     *
     * @return The  current run state
     */
    public AtomicBoolean Run() {
        return canRun;
    }

    /**
     * Get and set if the thread must enter a wait state
     *
     * @return the current wait state
     */
    public AtomicBoolean Pause() {
        return isPaused;
    }
}
