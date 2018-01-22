package API_Com.Modules;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Vector;


public class ThreadMessage implements Parcelable {

    private Exception anException;
    private ThreadMessageState aState;

    public ThreadMessage(ThreadMessageState aState, Exception e) {
        this.aState = aState;
        this.anException = e;
    }

    public ThreadMessage(ThreadMessageState aState) {
        this.aState = aState;

    }

    protected ThreadMessage(Parcel in) {
    }

    public static final Creator<ThreadMessage> CREATOR = new Creator<ThreadMessage>() {
        @Override
        public ThreadMessage createFromParcel(Parcel in) {
            return new ThreadMessage(in);
        }

        @Override
        public ThreadMessage[] newArray(int size) {
            return new ThreadMessage[size];
        }
    };

    public Exception getException() {
        return anException;
    }

    public void setException(Exception anException) {
        this.anException = anException;
    }

    public ThreadMessageState getState() {
        return aState;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }


    public enum ThreadMessageState {
        CONNECTING, CONNECTED, CONNECTION_FAILED, DISCONNECTED, DISCONNECTED_FROM_SERVER, DATA_LISTEN, DATA_LISTEN_FAILED
    }

    public enum ThreadMessageKey {
        CONNECTION_KEY, LISTEN_DATA_KEY
    }
}

