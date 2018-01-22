package API_Com.DialogAndAlert.Alert;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Class which built a generic alert dialog.
 */
public class GenericAlert extends AlertSc {

    private Bundle paramBundle;

    public final static String KEY_ICON = "Icon";
    public final static String KEY_TITTLE = "Tittle";
    public final static String KEY_MESSAGE = "Message";
    public final static String KEY_POSITIVE_BTN = "PositiveButton";
    public final static String KEY_NEGATIVE_BTN = "NegativeButton";


    public GenericAlert() {
        super();
    }

    /**
     * Constructor
     *
     * @param aBundle A bundle containing all the parameters to display on the alert
     * @return the fragment of the alert
     */
    public static GenericAlert newInstance(Bundle aBundle) {
        GenericAlert frag = new GenericAlert();
        frag.setArguments(aBundle);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        paramBundle = getArguments();
        theDialog = builtAlertDialog(paramBundle);

        return theDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        myNoticeListener.onAlertDialogNotification(theDialog, AlertUserAction.FINGER_CANCEL);

    }

    @Override
    public void onStop() {
        super.onStop();
        super.isDisplay = false;
    }

    /**
     * Create the alert
     * @param theBundleOfParam the Bundle containing the data to built the alert dialog
     * @return
     */
    @Override
    protected AlertDialog builtAlertDialog(Bundle theBundleOfParam) {

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        if (theBundleOfParam.getInt(KEY_TITTLE) > 0) {
            alert.setTitle(theBundleOfParam.getInt(KEY_TITTLE));
        }

        if (theBundleOfParam.getInt(KEY_ICON) > 0) {
            alert.setIcon(theBundleOfParam.getInt(KEY_ICON));
        }

        if (theBundleOfParam.getInt(KEY_MESSAGE) > 0) {
            alert.setMessage(theBundleOfParam.getInt(KEY_MESSAGE));
        }

        if (theBundleOfParam.getInt(KEY_POSITIVE_BTN) > 0) {
            alert.setPositiveButton(theBundleOfParam.getInt(KEY_POSITIVE_BTN), this);
        }

        if (theBundleOfParam.getInt(KEY_NEGATIVE_BTN) > 0) {
            alert.setNegativeButton(theBundleOfParam.getInt(KEY_NEGATIVE_BTN), this);
        }

        return alert.create();

    }


    /**
     * Get the alert dialog
     *
     * @return the Alert dialog
     */
    public AlertDialog getTheDialog() {
        return theDialog;
    }
}
