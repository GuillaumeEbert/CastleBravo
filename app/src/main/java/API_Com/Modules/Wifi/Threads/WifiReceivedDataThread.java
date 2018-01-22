package API_Com.Modules.Wifi.Threads;

import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import API_Com.Modules.ThreadMessage;
import API_Com.Modules.Wifi.Wifi;

/**
 * Created by Guillaumee on 27/04/2016.
 */
public class WifiReceivedDataThread extends WifiThread {

    private ScheduledExecutorService scheduleTaskExecutorDisconnectionFromServer;
    private Boolean isScheduleExecutorStarted;


    public WifiReceivedDataThread(Wifi aModule) {
        super(aModule);
        scheduleTaskExecutorDisconnectionFromServer = Executors.newScheduledThreadPool(1);
        isScheduleExecutorStarted = false;

    }

    @Override
    public void run() {
        super.run();

        Log.d("WIFI_thread_ListenData", "START");
        int valueReceived;


        while (canRun.get()) {

            if (myWifiToRun.getMySocketState().equals(Wifi.WifiSocketState.CONNECTED) && !isPaused.get()) {

                try {

                    valueReceived = myWifiToRun.listenForData();

                    if (valueReceived > 0) {
                        sendAInterThreadMessage(ThreadMessage.ThreadMessageKey.LISTEN_DATA_KEY.name(), new ThreadMessage(ThreadMessage.ThreadMessageState.DATA_LISTEN));

                        if (isScheduleExecutorStarted) {
                            scheduleTaskExecutorDisconnectionFromServer.shutdown();
                            scheduleTaskExecutorDisconnectionFromServer = Executors.newScheduledThreadPool(1);
                        }

                    } else {
                        /*Start a timer*/
                        if (!isScheduleExecutorStarted) {

                            isScheduleExecutorStarted = true;
                            scheduleTaskExecutorDisconnectionFromServer.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    myWifiToRun.setMySocketState(Wifi.WifiSocketState.DISCONNECTED_FROM_SERVER);
                                    sendAInterThreadMessage(ThreadMessage.ThreadMessageKey.CONNECTION_KEY.name(), new ThreadMessage(ThreadMessage.ThreadMessageState.DISCONNECTED_FROM_SERVER));
                                }
                            }, 1, TimeUnit.SECONDS);

                        }

                    }

                } catch (Exception e) {
                    sendAInterThreadMessage(ThreadMessage.ThreadMessageKey.LISTEN_DATA_KEY.name(), new ThreadMessage(ThreadMessage.ThreadMessageState.DATA_LISTEN_FAILED, e));
                }

            } else {
                Pause().set(true);
                Wait();

            }
        }

        Log.d("WIFI_thread_ListenData", "RUN_OVER");
    }


    public ScheduledExecutorService getScheduleTaskExecutorDisconnectionFromServer() {
        return scheduleTaskExecutorDisconnectionFromServer;
    }
}
