package fr.telecom_physique.castlebravo.ActivitiesForDemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Vector;

import API_Com.CommunicationManager.CommunicationListener;
import API_Com.CommunicationManager.CommunicationManager;
import fr.telecom_physique.castlebravo.R;
import API_Com.CommunicationManager.CommunicationManager.ModuleType;
import API_Com.CommunicationManager.CommunicationManager.ModuleState;

public class WifiActivity extends AppCompatActivity {

    private int myId;
    private CustomAdapter _adapter;
    private ArrayList<String> _lElementForList;
    private CommunicationManager theComManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi);
        Toolbar _toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(_toolbar);
        setTitle("Wifi");
        _toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        _toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        _lElementForList = new ArrayList<String>();
        ListView _listView = (ListView) findViewById(R.id.list);

        _adapter = new CustomAdapter(this, _lElementForList);
        _listView.setAdapter(_adapter);

        theComManager = CommunicationManager.getInstance(this);

        try {

            myId = theComManager.openCom("WIFI:serverIp=192.168.10.254,serverPort=8080,timeOut=1000");

        } catch (Exception e) {

            Log.e("OpenCom", "exception:", e);
            finish();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        theComManager.setOnComManagerListener(new CommunicationListener.ComManagerListener() {
            @Override
            public void onConnection(ModuleType moduleType, int id, ModuleState moduleState, String reason) {
                if (moduleType.equals(CommunicationManager.ModuleType.WIFI) && id == myId) {
                    switch (moduleState) {
                        case CONNECTED:
                            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                            break;

                        case FAILED:
                            Toast.makeText(getApplicationContext(), "Connection failed : " + reason, Toast.LENGTH_LONG).show();
                            finish();
                            break;
                    }
                }

            }

            @Override
            public void onDisconnection(ModuleType moduleType, int id, ModuleState moduleState, Exception e) {
                if (moduleType.equals(CommunicationManager.ModuleType.WIFI) && id == myId) {
                    switch (moduleState) {
                        case DISCONNECTED:
                            Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();
                            finish();
                            break;

                        case FAILED:
                            Toast.makeText(getApplicationContext(), "Disconnected failed : " + e.getMessage(), Toast.LENGTH_LONG).show();
                            finish();
                            break;
                    }
                }

            }

            @Override
            public void onDataReceived(ModuleType moduleType, int id, ModuleState moduleState, Vector<Integer> dataBuffer, Exception e) {

                if (moduleType.equals(CommunicationManager.ModuleType.WIFI) && id == myId) {
                    switch (moduleState) {
                        case LISTEN_FOR_DATA:
                            if (dataBuffer != null) {

                                dataBuffer.toString();
                                _lElementForList.add("Server : " + dataBuffer);
                                _adapter.notifyDataSetChanged();

                            }
                            break;

                        case FAILED:
                            break;
                    }
                }
            }


        });

        Button Btn_send = (Button) findViewById(R.id.send_button);

        if (Btn_send != null) {

            Btn_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    EditText editText = (EditText) findViewById(R.id.editText);
                    String dataToSend = editText.getText().toString();

                    try {
                        theComManager.send(myId, dataToSend);

                        _lElementForList.add("Client : " + dataToSend);
                        _adapter.notifyDataSetChanged();
                        if (editText != null) editText.setText(""); //clear the edit text

                    } catch (Exception e) {
                        _lElementForList.add("Exception: " + e.getMessage());
                        if (editText != null) editText.setText("");

                    }
                }
            });
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {

            theComManager.closeCom(myId);

        } catch (Exception e) {

            Log.e("onDestroy", "EXCEPTION:", e);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}

