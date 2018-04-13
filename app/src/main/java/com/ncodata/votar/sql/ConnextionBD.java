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
        Log.d(LOG_TAG, "conextionBD() :setOnResultListener");
            if (onAsyncResult != null) {
            this.onConnectionResult = onAsyncResult;
            }
    }

    public void setDatabase(String ip,String nombreBase,String userBase,String passwordBase){
        Log.d(LOG_TAG, "conextionBD() :setDatabase");
        this.ip=ip;
        this.nombreBase=nombreBase;
        this.userBase=userBase;
        this.passwordBase=passwordBase;
    }

    @Override
    protected void onPreExecute() {
        Log.d(LOG_TAG, "conextionBD(): onPreExecute");
        super.onPreExecute();
    }


    @Override
    protected Connection doInBackground(Void... voids) {
        Log.d(LOG_TAG, "conextionBD(): doInBackground");
        Connection connection = null;
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();


            connection = DriverManager.getConnection("jdbc:jtds:sqlserver://"+ip+";databaseName="+nombreBase+";user="+userBase+";password="+passwordBase);

            Log.d(LOG_TAG, "conextionBD()-connection:" + connection.toString());
            onConnectionResult.onResultSuccess(connection);
            Log.d(LOG_TAG, "conextionBD(): onResultSuccess ejecutado");
        } catch (Exception e) {
            Log.d(LOG_TAG, "conextionBD()-Exception:" + e.getMessage());
            onConnectionResult.onResultFail(e.getMessage());


        }
        Log.d(LOG_TAG, "conextionBD(): return");
        return connection;

     }



    public interface OnConnectionResult {
            public abstract void onResultSuccess(Connection connection);
            public abstract void onResultFail(String errorMessage);
      }
}
