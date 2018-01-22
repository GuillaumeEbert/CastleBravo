package API_Com.DialogAndAlert;

import android.app.Dialog;
import android.os.Bundle;

import API_Com.DialogAndAlert.Alert.AlertSc.AlertUserAction;

/**
 * Generic event for the user action on the alert or dialog.
 */
public interface NoticeAlertDialogListener {
    void onAlertDialogNotification(Dialog dialog, AlertUserAction which);
}
