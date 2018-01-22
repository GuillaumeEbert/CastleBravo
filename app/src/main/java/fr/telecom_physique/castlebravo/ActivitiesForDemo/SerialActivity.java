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

public class SerialActivity extends AppCompatActivity {

    private int myId;
    private CustomAdapter _adapter;
    private ArrayList<String> _lElementForList;
    private CommunicationManager theComManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial);
        Toolbar _toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(_toolbar);
        setTitle("Serial");
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

            myId = theComManager.openCom("SERIAL:baudRate=9600");

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
            public void onConnection(CommunicationManager.ModuleType moduleType, int id, CommunicationManager.ModuleState moduleState, String reason) {
                if (moduleType.equals(CommunicationManager.ModuleType.SERIAL) && id == myId) {
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
            public void onDisconnection(CommunicationManager.ModuleType moduleType, int id, CommunicationManager.ModuleState moduleState, Exception e) {
                if (moduleType.equals(CommunicationManager.ModuleType.SERIAL) && id == myId) {
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
            public void onDataReceived(CommunicationManager.ModuleType moduleType, int id, CommunicationManager.ModuleState moduleState, Vector<Integer> dataBuffer, Exception e) {

                if (moduleType.equals(CommunicationManager.ModuleType.SERIAL) && id == myId) {
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
                        Log.e("TCP", "S: Error", e);
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


}
