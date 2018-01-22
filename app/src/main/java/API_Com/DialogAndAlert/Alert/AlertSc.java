package API_Com.DialogAndAlert.Alert;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import API_Com.DialogAndAlert.DialogAndAlertSc;

/**
 * Super class for the Alert dialog .
 */
public abstract class AlertSc extends DialogAndAlertSc implements DialogInterface.OnClickListener {

    protected AlertDialog theDialog;

    public AlertSc() {
        super();
        theDialog = null;
        myNoticeListener = null;

    }

    /**
     * Handle the click on the button of an alert dialog. This method will raise a more generic event for this API
     *
     * @param dialog the dialog which has trig a click event
     * @param which  the type of button
     * @see API_Com.DialogAndAlert.NoticeAlertDialogListener for more details about these events.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {

        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE:
                myNoticeListener.onAlertDialogNotification(theDialog, AlertUserAction.BUTTON_NEGATIVE);
                break;

            case DialogInterface.BUTTON_POSITIVE:
                myNoticeListener.onAlertDialogNotification(theDialog, AlertUserAction.BUTTON_POSITIVE);
                break;

            case DialogInterface.BUTTON_NEUTRAL:
                myNoticeListener.onAlertDialogNotification(theDialog, AlertUserAction.BUTTON_NEUTRAL);
                break;
        }
    }

    /**
     * Create the alert dialog, following the parameters present in the bundle
     *
     * @param theBundleOfParam the Bundle containing the data to built the alert dialog
     * @return the alert dialog
     */
    protected  AlertDialog builtAlertDialog(Bundle theBundleOfParam){
        return theDialog;
    }

    /**
     * Represent a user action on a alert dialog.
     */
    public enum AlertUserAction {
        BUTTON_POSITIVE, BUTTON_NEGATIVE, BUTTON_NEUTRAL, FINGER_CANCEL
    }

}

