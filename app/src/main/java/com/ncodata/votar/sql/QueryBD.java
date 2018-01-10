package com.ncodata.votar.sql;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by perez.juan.jose on 05/01/2018.
 */

public class QueryBD extends AsyncTask<QueryBD.QueryData, Void, Void> {
    public static final String LOG_TAG = "UpdateBDClass";
    OnQueryResult onQueryResult;
    public void setOnResultListener( OnQueryResult onAsyncResult) {
            if (onAsyncResult != null) {
            this.onQueryResult = onAsyncResult;
            }
    }


    @Override
    protected Void doInBackground(QueryData ... queryData) {
        Log.d(LOG_TAG, "conextionBD():");
        Log.d(LOG_TAG, "conextionBD(): psts "+queryData[0].getSelect());
        ResultSet rs=null;

        Statement st= null;
        try {
            st = queryData[0].getConnection().createStatement();
            rs = st.executeQuery(queryData[0].getSelect());
            onQueryResult.onResultSuccess(rs);
        } catch (SQLException e) {
            onQueryResult.onResultFail(e.getMessage());
        }
        return null;
    }

    public interface OnQueryResult {
            public abstract void onResultSuccess(ResultSet resultSet);
            public abstract void onResultFail(String errorMessage);
      }

    public static class QueryData{
        public String select = "";
        public Connection connection=null;

        public QueryData(Connection connection,String stsql) {
            this.select = stsql;
            this.connection = connection;
        }

        public String getSelect() {
            return select;
        }

        public Connection getConnection() {
            return connection;
        }
    }
}
