package API_Com.BroadcastReceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Super class for the modules broadcast receiver
 */
public abstract class BroadCastReceiverSc extends BroadcastReceiver {

    private boolean isRegister;

    protected BroadCastReceiverSc() {
        isRegister = false;
    }

    /**
     * Register the broadcast receiver
     *
     * @param runningActivity The activity to register the broadcast receiver
     * @param intentFilter    The intent filter for the broadcast receiver
     */
    public void registerMe(Activity runningActivity, IntentFilter intentFilter){
        runningActivity.registerReceiver(this, intentFilter);
        isRegister = true;
    }

    /**
     * Unregister the broadcastReceiver
     * @param runningActivity The activity to unregister the broadcast receiver. Must be the same as the @see registerMe
     *                        method.
     */
    public void unregisterMe(Activity runningActivity){
        if(isRegister){
            runningActivity.unregisterReceiver(this);
            isRegister = false;
        }
    }

}
