package com.ncodata.votar.sql;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.PreparedStatement;


/**
 * Created by perez.juan.jose on 05/01/2018.
 */

public class InsertBD extends AsyncTask<PreparedStatement, Void, Void> {
    public static final String LOG_TAG = "UpdateBDClass";
    OnInsertResult onInsertResult;
    public void setOnResultListener( OnInsertResult onAsyncResult) {
            if (onAsyncResult != null) {
            this.onInsertResult = onAsyncResult;
            }
    }


    @Override
    protected Void doInBackground(PreparedStatement ... psts) {
        Log.d(LOG_TAG, "conextionBD():");
        Log.d(LOG_TAG, "conextionBD(): psts "+psts[0]);
        if(psts[0]!=null){
            try {
                psts[0].execute();
                onInsertResult.onResultSuccess("exitoso");

            } catch (Exception e) {
            Log.d(LOG_TAG, "conextionBD()-Exception:" + e.getMessage());
            onInsertResult.onResultFail(e.getMessage());


            }
        }
        return null;
    }

    public interface OnInsertResult {
            public abstract void onResultSuccess(String message);
            public abstract void onResultFail(String errorMessage);
      }
}
