package API_Com.DialogAndAlert;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;

/**
 * Super class for the alert and the dialog
 */
public abstract class DialogAndAlertSc extends DialogFragment {

    protected boolean isDisplay;
    protected NoticeAlertDialogListener myNoticeListener;

    public DialogAndAlertSc() {
        isDisplay = false;
    }

    /**
     * Call this method to display an alert or dialog
     *
     * @param key      the key for the display
     * @param aContext the activity which has the focus
     */
    public void display(String key, Context aContext) {

        if (!isDisplay) {

            Activity theActivity = (Activity) aContext;

            FragmentTransaction ft = theActivity.getFragmentManager().beginTransaction();
            Fragment prev = theActivity.getFragmentManager().findFragmentByTag(key);

            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            this.show(ft, key);
            isDisplay = true;
        }
    }

    /**
     * Set the listener for the dialog or alert
     * @param listener the listener
     */
    public void setOnNoticeListener(NoticeAlertDialogListener listener) {
        myNoticeListener = listener;
    }

    /**
     * Get the state of the alert or dialog display
     * @return the state
     */
    public boolean isDisplay() {
        return isDisplay;
    }

}
