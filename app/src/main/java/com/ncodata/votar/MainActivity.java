package com.ncodata.votar;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "MainActivity";
    EditText mPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Intent intent = new Intent(getApplication(), VotacionActivity.class);

                startActivity(intent);

            }
        });

       mPassword =(EditText)findViewById(R.id.password);
        Button  mButtonSignIn =(Button)findViewById(R.id.button_sign_in);

        mButtonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agregarDato();
            }
        });



        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();


        Log.d(LOG_TAG, "wifiManager:" + wifiManager);
        Log.d(LOG_TAG,"wInfo:" + wInfo);
        Log.d(LOG_TAG, "macAddres:" +macAddress);
        Log.d(LOG_TAG, "getMAc:" +getMacAddr());





    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Log.d(LOG_TAG, "id" +id);
            Intent intent = new Intent(getApplication(), ConfiguracionActivity.class);

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public Connection conextionBD() {
        Log.d(LOG_TAG, "conextionBD():" );
        Connection connection=null;
        try{
            StrictMode.ThreadPolicy policy= new StrictMode.ThreadPolicy.Builder().permitAll().build();
            Log.d(LOG_TAG, "conextionBD()-policy:" +policy.toString());
            StrictMode.setThreadPolicy(policy);
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();

            connection= DriverManager.getConnection("jdbc:jtds:sqlserver://192.168.1.37;databaseName=nombreBase;user=juan;password=1234");

            Log.d(LOG_TAG, "conextionBD()-connection:" +connection.toString());
        }
        catch(Exception e){
            Log.d(LOG_TAG, "conextionBD()-Exception:" +e.getMessage().toString());
            Toast.makeText(getApplicationContext(),"Error:" + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
        return connection;
    }

    public void agregarDato (){

        try {

            PreparedStatement pst= conextionBD().prepareStatement("insert into ASIGNACION_TABLETS values (?,?)");
            Log.d(LOG_TAG, " agregarDato-connection:" +conextionBD().toString());
            pst.setString(1,mPassword.getText().toString());
            pst.executeUpdate();
            Toast.makeText(getApplicationContext(),"Dato agregado correctamente",Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            Toast.makeText(getApplicationContext(),"Error:" + e.getMessage().toString(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }
}
