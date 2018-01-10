package com.ncodata.votar.sql;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by perez.juan.jose on 05/01/2018.
 */

public class ConnextionBD  extends AsyncTask<Void, Integer, Connection> {
    public static final String LOG_TAG = "ConnextionBDClass";
    public static String ip;
    public static String  nombreBase;
    public static String  userBase;
    public static String passwordBase;
    OnConnectionResult onConnectionResult;
    public void setOnResultListener( OnConnectionResult onAsyncResult) {
            if (onAsyncResult != null) {
            this.onConnectionResult = onAsyncResult;
            }
    }

    public void setDatabase(String ip,String nombreBase,String userBase,String passwordBase){
        this.ip=ip;
        this.nombreBase=nombreBase;
        this.userBase=userBase;
        this.passwordBase=passwordBase;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Connection doInBackground(Void... voids) {
        Log.d(LOG_TAG, "conextionBD():");
        Connection connection = null;
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
//             mi pc 192.168.0.34
//             mi pc 192.168.0.17
//             Bulnes 192.168.0.6


            connection = DriverManager.getConnection("jdbc:jtds:sqlserver://"+ip+";databaseName="+nombreBase+";user="+userBase+";password="+passwordBase);
//            connection = DriverManager.getConnection("jdbc:jtds:sqlserver://192.168.0.6;databaseName=HCD;user=hcd;password=d4k4r");
            Log.d(LOG_TAG, "conextionBD()-connection:" + connection.toString());
            onConnectionResult.onResultSuccess(connection);

        } catch (Exception e) {
            Log.d(LOG_TAG, "conextionBD()-Exception:" + e.getMessage());
            onConnectionResult.onResultFail(e.getMessage());


        }
        return connection;

     }



    public interface OnConnectionResult {
            public abstract void onResultSuccess(Connection connection);
            public abstract void onResultFail(String errorMessage);
      }
}
