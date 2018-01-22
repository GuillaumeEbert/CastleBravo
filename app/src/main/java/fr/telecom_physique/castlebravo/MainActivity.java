package fr.telecom_physique.castlebravo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import fr.telecom_physique.castlebravo.ActivitiesForDemo.BluetoothActivity;
import fr.telecom_physique.castlebravo.ActivitiesForDemo.SerialActivity;
import fr.telecom_physique.castlebravo.ActivitiesForDemo.WifiActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton imgBtn_Wifi = (ImageButton) findViewById(R.id.ImgBtn_Wifi);
        ImageButton imgBtn_Bluetooth = (ImageButton) findViewById(R.id.ImgBtn_Bluetooth);
        ImageButton imgBtn_Serial = (ImageButton) findViewById(R.id.ImgBtn_Serial);

        imgBtn_Wifi.setOnClickListener(this);
        imgBtn_Bluetooth.setOnClickListener(this);
        imgBtn_Serial.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
    /* react on a click */
        switch (v.getId()) {

            case R.id.ImgBtn_Wifi:
                startActivity(new Intent(MainActivity.this, WifiActivity.class));
                break;

            case R.id.ImgBtn_Bluetooth:
                startActivity(new Intent(MainActivity.this, BluetoothActivity.class));
                break;

            case R.id.ImgBtn_Serial:
                startActivity(new Intent(MainActivity.this, SerialActivity.class));
                break;
        }
    }


}
