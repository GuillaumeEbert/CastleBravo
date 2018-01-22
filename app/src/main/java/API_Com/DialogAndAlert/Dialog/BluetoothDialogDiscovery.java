package API_Com.DialogAndAlert.Dialog;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import API_Com.DialogAndAlert.Alert.AlertSc;
import API_Com.Modules.Bluetooth.MyBluetoothDevice;
import fr.telecom_physique.castlebravo.R;
import API_Com.Modules.Bluetooth.MyBluetoothDevice.BluetoothState;
import API_Com.Modules.Bluetooth.MyBluetoothDevice.DeviceState;

/**
 * Dialog which display the discovered and paired bluetooth device.
 */
public class BluetoothDialogDiscovery extends DialogSc implements View.OnClickListener {

    private Button btnCancel;
    private Button btnForceAnalyze;
    private ListView lvPairedDevices;
    private boolean canRedoAnAnalyse;
    private ListView lvDiscoveredDevices;
    private ProgressBar analyzeProgressBar;
    private RelativeLayout pairedDeviceLayout;
    private BluetoothAdapter myBluetoothAdapter;
    private MyListAdapter adapterLvPairedDevices;
    private RelativeLayout discoveredDeviceLayout;
    private MyListAdapter adapterLvDiscoveredDevices;
    private List<MyBluetoothDevice> listPairedDevice;
    private List<MyBluetoothDevice> listDiscoveredDevice;
    private ScheduledExecutorService scheduleTaskExecutorReDoAnalysis;

    private OnDeviceSelectListener onDeviceSelectListener;

    private TextView tvPairedDevice;
    private boolean aDeviceWasSelected = false;

    /**
     * Constructor
     *
     * @return
     */
    public static BluetoothDialogDiscovery newInstance() {

        BluetoothDialogDiscovery fragment = new BluetoothDialogDiscovery();
        fragment.setStyle(STYLE_NORMAL, R.style.CustomDialog);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.api_bluetooth_dialog_discovery, container);
        getDialog().setTitle("BLUETOOTH");

        BluetoothManager theBluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        myBluetoothAdapter = theBluetoothManager.getAdapter();

        scheduleTaskExecutorReDoAnalysis = Executors.newScheduledThreadPool(1);

        listDiscoveredDevice = new ArrayList<>();
        listPairedDevice = new ArrayList<>();

        /*Layout*/
        pairedDeviceLayout = (RelativeLayout) view.findViewById(R.id.layout_paired_devices);
        discoveredDeviceLayout = (RelativeLayout) view.findViewById(R.id.layout_discovered_devices);

        /*Widget*/
        analyzeProgressBar = (ProgressBar) view.findViewById(R.id.pb_bt_discovery);
        btnForceAnalyze = (Button) view.findViewById(R.id.btn_Bt_Analyze);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel_Bt_dialog);
        tvPairedDevice = (TextView) view.findViewById(R.id.tv_paired_devices);

        btnCancel.setOnClickListener(this);
        btnForceAnalyze.setOnClickListener(this);

        /*List view adapter */
        adapterLvDiscoveredDevices = new MyListAdapter(getActivity(), R.layout.api_bluetooth_list_view_paired_discovered_device, listDiscoveredDevice);
        adapterLvPairedDevices = new MyListAdapter(getActivity(), R.layout.api_bluetooth_list_view_paired_discovered_device, listPairedDevice);

        /*List view*/
        lvDiscoveredDevices = (ListView) view.findViewById(R.id.lv_discovered_devices);
        lvPairedDevices = (ListView) view.findViewById(R.id.lv_paired_devices);
        lvDiscoveredDevices.setAdapter(adapterLvDiscoveredDevices);
        lvPairedDevices.setAdapter(adapterLvPairedDevices);

        canRedoAnAnalyse = true;

        /*The view is on display can redraw the layout for paired device*/
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RedrawPairedDeviceLayoutHeight();
            }
        });

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        RelativeLayout.LayoutParams discoveredDeviceLayoutParam = (RelativeLayout.LayoutParams) discoveredDeviceLayout.getLayoutParams();

        if (myBluetoothAdapter.getBondedDevices().size() == 0) { /*Some bluetooth devices are paired to your device
            /*No paired device*/
            if (pairedDeviceLayout.getVisibility() == View.VISIBLE) {
                pairedDeviceLayout.setVisibility(View.GONE);
                discoveredDeviceLayoutParam.setMargins(0, 0, 0, 0); /* put the layout just below the tittle*/

            }

        } else {
            setLvPairedDevices();
            pairedDeviceLayout.setVisibility(View.VISIBLE);

        }

        myBluetoothAdapter.startDiscovery();

        /*Select a paired device to connect to*/
        lvDiscoveredDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!aDeviceWasSelected) {
                    MyBluetoothDevice deviceSelected = listDiscoveredDevice.get(position);
                    onDeviceSelectListener.onSelectDiscoveredDevice(deviceSelected);
                }
                aDeviceWasSelected = true;


            }
        });

        /*Select a un pair device to connect to*/
        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!aDeviceWasSelected) {
                    MyBluetoothDevice deviceSelected = listPairedDevice.get(position);
                    onDeviceSelectListener.onSelectPairDevice(deviceSelected);
                }
                aDeviceWasSelected = true;

            }
        });

    }


    @Override
    public void onStop() {
        super.onStop();
        isDisplay = false;

    }

    /**
     * Set my list of paired device
     */
    public void setLvPairedDevices() {
        Set<BluetoothDevice> setBtDevice = myBluetoothAdapter.getBondedDevices();
        Object arrObj[] = setBtDevice.toArray();


        for (Object o : arrObj) {
            listPairedDevice.add(new MyBluetoothDevice((BluetoothDevice) o, BluetoothState.PAIRED, DeviceState.ALREADY_PAIRED_DEVICE));
            adapterLvPairedDevices.notifyDataSetChanged();
        }


    }

    /**
     * Redraw the pair device layout.
     * When it's done the layout of the discovered device is just below the layout of the pair devices
     */
    private void RedrawPairedDeviceLayoutHeight() {
        int heightLv = getTheLvPairedDeviceHeight();
        int heightTv = tvPairedDevice.getHeight();
        RelativeLayout.LayoutParams pairedDeviceLayoutParam = (RelativeLayout.LayoutParams) pairedDeviceLayout.getLayoutParams();

        Resources r = getResources();
        int Margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics()); /*Convert dp on px*/

        /*New height of the layout*/
        pairedDeviceLayoutParam.height = heightLv + heightTv + Margin + ((lvPairedDevices.getDividerHeight() * (adapterLvPairedDevices.getCount() - 1)));

    }

    /**
     * Get the height of the list view following the number of elements displayed
     *
     * @return the height of the list view
     */
    private int getTheLvPairedDeviceHeight() {
        int totalHeight = 0;

        for (int i = 0; i < adapterLvPairedDevices.getCount(); i++) {
            View aView = adapterLvPairedDevices.getView(i, null, lvPairedDevices);

            aView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += aView.getMeasuredHeight();
        }

        return totalHeight;
    }

    /**
     * Call when the state of the discovery change
     *
     * @param discoveryState represent the state of the discovery
     */
    public void setDiscoveryState(boolean discoveryState) {
        if (discoveryState) {
            analyzeProgressBar.setVisibility(View.VISIBLE);

        } else {
            analyzeProgressBar.setVisibility(View.GONE);

            //Relaunch a discovery after 10 sec
            if (canRedoAnAnalyse) {
                scheduleTaskExecutorReDoAnalysis.schedule(new Runnable() {
                    public void run() {
                        myBluetoothAdapter.startDiscovery();
                    }
                }, 10, TimeUnit.SECONDS);

            }

        }
    }

    /**
     * Call when a new device is found
     * Add it to my list of discovered devices
     *
     * @param theDevice
     */
    public void newDeviceFound(MyBluetoothDevice theDevice) {
        boolean canBeAdd = true;

        if (listDiscoveredDevice.size() > 0) {

            for (int i = 0; i < listDiscoveredDevice.size(); i++) {
                MyBluetoothDevice aDevice = listDiscoveredDevice.get(i);

                if (aDevice.getDevice().getAddress().equals(theDevice.getDevice().getAddress())) {
                    canBeAdd = false;
                    break;
                } else {
                    canBeAdd = true;
                }
            }

            if (canBeAdd) {
                listDiscoveredDevice.add(theDevice);
                adapterLvDiscoveredDevices.notifyDataSetChanged();
            }


        } else {
            listDiscoveredDevice.add(theDevice);
            adapterLvDiscoveredDevices.notifyDataSetChanged();
        }

    }


    /**
     * Update the two list view
     *
     * @param aDeviceConnecting the device in connection
     * @param aState            the connection state
     * @see MyBluetoothDevice.BluetoothState
     */
    public void updateLVs(BluetoothDevice aDeviceConnecting, BluetoothState aState) {

        if (isDeviceAlreadyPaired(aDeviceConnecting)) {
            updateLvPairedDevices(aDeviceConnecting, aState);

        } else {
            updateLvDiscoveredDevices(aDeviceConnecting, aState);

        }

        if (aState.equals(BluetoothState.CONNECTED)) autoDismiss();

    }

    /**
     * Check if the device selected is already paired to your device
     *
     * @param aDevice a pair device
     * @return true if it's already pair
     */
    private boolean isDeviceAlreadyPaired(BluetoothDevice aDevice) {

        for (int i = 0; i < listPairedDevice.size(); i++) {
            MyBluetoothDevice aListDevice = listPairedDevice.get(i);

            if (aDevice.getAddress().equals(aListDevice.getDevice().getAddress())) {

                if (aListDevice.getMyDeviceState().equals(DeviceState.ALREADY_PAIRED_DEVICE)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Update the list view which display the paired devices
     *
     * @param aDevice  a paired device to update
     * @param newState the new state to display
     * @see MyBluetoothDevice.BluetoothState
     */
    private void updateLvPairedDevices(BluetoothDevice aDevice, BluetoothState newState) {

        for (int i = 0; i < listPairedDevice.size(); i++) {
            MyBluetoothDevice aListDevice = listPairedDevice.get(i);

            if (aDevice.getAddress().equals(aListDevice.getDevice().getAddress())) {
                listPairedDevice.remove(i);
                aListDevice.setState(newState);
                listPairedDevice.add(0, aListDevice);
                adapterLvPairedDevices.notifyDataSetChanged();

            }
        }

    }

    /**
     * Update the list view which display the un pair devices
     *
     * @param aDevice  a un pair device to update
     * @param newState the new state to display
     * @see MyBluetoothDevice.BluetoothState
     */
    private void updateLvDiscoveredDevices(BluetoothDevice aDevice, BluetoothState newState) {

        for (int i = 0; i < listDiscoveredDevice.size(); i++) {
            MyBluetoothDevice aListDevice = listDiscoveredDevice.get(i);

            if (aDevice.getAddress().equals(aListDevice.getDevice().getAddress())) {
                listDiscoveredDevice.remove(i);
                aListDevice.setState(newState);
                listDiscoveredDevice.add(0, aListDevice);
                adapterLvDiscoveredDevices.notifyDataSetChanged();

            }
        }

    }


    /**
     * Auto dismiss this dialog when a bluetooth device is connected.
     * When call the dialog will be dismiss 2 seconds after
     */
    private void autoDismiss() {

        ScheduledExecutorService scheduleTaskExecutorDismiss = Executors.newSingleThreadScheduledExecutor();

        scheduleTaskExecutorDismiss.schedule(new Runnable() {
            @Override
            public void run() {
                myNoticeListener.onAlertDialogNotification(getDialog(), AlertSc.AlertUserAction.BUTTON_POSITIVE);
                canRedoAnAnalyse = false;
                myBluetoothAdapter.cancelDiscovery();
                dismiss();
            }
        }, 2, TimeUnit.SECONDS);

    }

    /**
     * Handle the click of the btn Cancel and Analyze
     *
     * @param v the view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_Bt_Analyze:
                if (!myBluetoothAdapter.isDiscovering()) {
                    myBluetoothAdapter.startDiscovery();
                }
                if (!canRedoAnAnalyse) canRedoAnAnalyse = true;
                break;

            case R.id.btn_cancel_Bt_dialog:
                myNoticeListener.onAlertDialogNotification(getDialog(), AlertSc.AlertUserAction.BUTTON_NEGATIVE);
                dismiss();
                break;
        }
    }

    /**
     * Callback
     */
    public interface OnDeviceSelectListener {
        void onSelectDiscoveredDevice(MyBluetoothDevice theDiscoveredDevice);

        void onSelectPairDevice(MyBluetoothDevice thePairDevice);
    }

    public void setOnDeviceSelectListener(OnDeviceSelectListener listener) {
        onDeviceSelectListener = listener;
    }


    /**
     * Setter
     */
    public void setCanRedoAnAnalyse(boolean canRedoAnAnalyse) {
        synchronized (this) {
            this.canRedoAnAnalyse = canRedoAnAnalyse;
            myBluetoothAdapter.cancelDiscovery();
        }

    }

    /**
     * Inner class
     * Adapter for the two list view
     */
    private class MyListAdapter extends ArrayAdapter<MyBluetoothDevice> {

        private int resource;
        private LayoutInflater mLayoutInflater;
        private List<MyBluetoothDevice> theList;

        public MyListAdapter(Context ctx, int resourceId, List<MyBluetoothDevice> listOfBluetoothDevice) {

            super(ctx, resourceId, listOfBluetoothDevice);
            resource = resourceId;
            mLayoutInflater = LayoutInflater.from(ctx);
            theList = listOfBluetoothDevice;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = mLayoutInflater.inflate(resource, null);

            MyBluetoothDevice aBtDevice = getItem(position);

            TextView tvNameBluetoothDevice = (TextView) convertView.findViewById(R.id.tv_bluetooth_device_name);
            TextView tvStateBluetoothDevice = (TextView) convertView.findViewById(R.id.tv_bt_bound_state);

            tvNameBluetoothDevice.setText(aBtDevice.getDevice().getName());

            if (aBtDevice.getMyDeviceState().equals(DeviceState.NEW_DEVICE)) {

                switch (aBtDevice.getState()) {
                    case UN_PAIR:
                        break;

                    case PAIRING:
                        tvStateBluetoothDevice.setText("Associating...");
                        break;

                    case PAIRED:
                        tvStateBluetoothDevice.setText("Associated");
                        break;
                }

            }

            switch (aBtDevice.getState()) {
                case CONNECTING:
                    tvStateBluetoothDevice.setText("Connecting");
                    break;

                case CONNECTED:
                    tvStateBluetoothDevice.setText("Connected");
                    break;

                case CONNECTION_FAILED:
                    tvStateBluetoothDevice.setText("Connected");
                    break;
            }


            return convertView;
        }
    }

}

