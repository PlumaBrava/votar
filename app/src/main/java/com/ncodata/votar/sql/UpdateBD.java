package com.ncodata.votar.sql;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.PreparedStatement;


/**
 * Created by perez.juan.jose on 05/01/2018.
 */

public class UpdateBD extends AsyncTask<PreparedStatement, Void, Void> {
    public static final String LOG_TAG = "UpdateBDClass";
    OnUpdateResult onUpdateResult;
    public void setOnResultListener( OnUpdateResult onAsyncResult) {
            if (onAsyncResult != null) {
            this.onUpdateResult = onAsyncResult;
            }
    }


    @Override
    protected Void doInBackground(PreparedStatement ... psts) {
        Log.d(LOG_TAG, "UpdateBD:");
        Log.d(LOG_TAG, "UpdateBD): psts "+psts[0]);
        if(psts[0]!=null){
            try {
                int cantidaLineasModificadas = psts[0].executeUpdate();
                Log.d(LOG_TAG, "executeUpdate() exitoso:");
            onUpdateResult.onResultSuccess(cantidaLineasModificadas);

            } catch (Exception e) {
            Log.d(LOG_TAG, "executeUpdate-Exception:" + e.getMessage());
            onUpdateResult.onResultFail(e.getMessage());


            }
        }
        return null;
    }

    public interface OnUpdateResult {
            public abstract void onResultSuccess(int cantidaLineasModificadas);
            public abstract void onResultFail(String errorMessage);
      }
}
