package com.ncodata.votar;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ncodata.votar.sql.ConnextionBD;
import com.ncodata.votar.sql.InsertBD;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.ncodata.votar.utils.SporteConf.getMacAddr;

/**
 * A login screen that offers login via email/password.
 */
//public class ConfiguracionActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
public class ConfiguracionActivity extends AppCompatActivity {
    public static final String LOG_TAG = "ConfiguracionActivity";
    Connection conn = null;//Conexion a la base de datos. Al entrar a la actividad nos conectamos y al salir nos desconectamos.
    ConnextionBD connextionBD; //Tarea que se ejecuta en segundo plano para conseguir una conexion a la base
    ConnextionBD.OnConnectionResult onConnectionResult;
    private ImageView mIconoBaseConectada;
    private ImageView mIconoBaseDesonectada;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */


    // UI references.
    private EditText mMac;
    private EditText mImei;
    private EditText mSerie;
    private EditText mDispositivo;
    private EditText mIp;
    private EditText mNombreBase;
    private EditText mUserBase;
    private EditText mPasswordBase;
    private View mProgressView;
    private View mLoginFormView;


    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        setupActionBar();
        // Set up the login form.
        mMac = (EditText) findViewById(R.id.mac);
        mImei = (EditText) findViewById(R.id.imei);
        mSerie = (EditText) findViewById(R.id.serie);
        mDispositivo = (EditText) findViewById(R.id.dispositivo);
        mIp = (EditText) findViewById(R.id.ip);
        mNombreBase = (EditText) findViewById(R.id.nombreBase);
        mUserBase = (EditText) findViewById(R.id.userBase);
        mPasswordBase = (EditText) findViewById(R.id.passwordBase);

//        mPasswordView = (EditText) findViewById(R.id.password);
//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });

        mIconoBaseConectada = (ImageView) findViewById(R.id.inconoBaseConectada);
        mIconoBaseDesonectada = (ImageView) findViewById(R.id.inconoBaseDesonectada);

        Button mBotonRegistrarTablet = (Button) findViewById(R.id.botonRegistarTablet);
        mBotonRegistrarTablet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                registar();
            }
        });

        Button mBotonRegistrarBase = (Button) findViewById(R.id.botonRegistarBase);
        mBotonRegistrarBase.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.preference_ip), mIp.getText().toString());
                editor.putString(getString(R.string.preference_NombreBase), mNombreBase.getText().toString());
                editor.putString(getString(R.string.preference_userBase), mUserBase.getText().toString());
                editor.putString(getString(R.string.preference_PasswordBase), mPasswordBase.getText().toString());
                editor.commit();
            }
        });

        mLoginFormView = findViewById(R.id.registo_form);
        mProgressView = findViewById(R.id.registro_progress);


        SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.password_default_key), getString(R.string.password_default));
        editor.commit();



        onConnectionResult = new ConnextionBD.OnConnectionResult() {
            @Override
            public void onResultSuccess(final Connection connection) {
                Log.d(LOG_TAG, " onResultSuccess( " + connection.toString());
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (connection != null) {
                            conn = connection;
                            mIconoBaseConectada.setVisibility(View.VISIBLE);
                            mIconoBaseDesonectada.setVisibility(View.GONE);
                        }

                    }
                });


            }

            @Override
            public void onResultFail(final String errorMessage) {
                Log.d(LOG_TAG, " onResultFail " + errorMessage);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(LOG_TAG, " onResultFail runable" + errorMessage);
                        Toast.makeText(getApplicationContext(), "Error:" + errorMessage, Toast.LENGTH_LONG).show();
                        mIconoBaseConectada.setVisibility(View.GONE);
                        mIconoBaseDesonectada.setVisibility(View.VISIBLE);
                    }
                });


            }
        };

        populateAutoComplete();
        loadIMEI();


    }

    private void populateAutoComplete() {
        mMac.setText(getMacAddr());
        String serial_no = null;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial_no = (String) get.invoke(c, "ro.serialno");
            System.out.println("Device serial ID : " + serial_no);
            mSerie.setText(serial_no);
        } catch (Exception e) {
            System.out.println("Some error occured : " + e.getMessage());
            mSerie.setText("error");
        }

        SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
       mIp.setText(sharedPref.getString(getString(R.string.preference_ip), getString(R.string.preference_ip_default)));
       mNombreBase.setText(sharedPref.getString(getString(R.string.preference_NombreBase), getString(R.string.preference_NombreBase_default)));
       mUserBase.setText(sharedPref.getString(getString(R.string.preference_userBase), getString(R.string.preference_userBase_default)));
       mPasswordBase.setText(sharedPref.getString(getString(R.string.preference_PasswordBase), getString(R.string.preference_PasswordBase_default)));

    }



    @Override
    protected void onResume() {
        Log.d(LOG_TAG, " onResume ");
        if (conn == null) {
            Log.d(LOG_TAG, " onResume conn null");
            connextionBD = new ConnextionBD();
            connextionBD.setOnResultListener(onConnectionResult);
            SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
            String ip=(sharedPref.getString(getString(R.string.preference_ip), getString(R.string.preference_ip_default)));
            String nombreBase=(sharedPref.getString(getString(R.string.preference_NombreBase), getString(R.string.preference_NombreBase_default)));
            String userBase=(sharedPref.getString(getString(R.string.preference_userBase), getString(R.string.preference_userBase_default)));
            String passwordBase=(sharedPref.getString(getString(R.string.preference_PasswordBase), getString(R.string.preference_PasswordBase_default)));
            connextionBD.setDatabase(ip,nombreBase,userBase,passwordBase);
            connextionBD.execute();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, " onpause() ");
        if (conn != null) {
            try {
                conn.close();
                conn = null;
                Log.d(LOG_TAG, " onpause() Close connection");
            } catch (SQLException e) {
                Log.d(LOG_TAG, " onpause() Close exception" + e.getMessage());

            }
        }
        super.onPause();
    }



    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }



    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }





    public void registar() {


        if(conn!=null){
            try {
                PreparedStatement pst = conn.prepareStatement("insert into age_Dispositivos "
                        + "(NumDispositivo,Dispositivo, Serie, Imei, Macaddresses, Estado)"
                        + "values (?,?,?,?,?,?)");


                Log.d(LOG_TAG, " agregarDato-connection:" + conn.toString());
                Log.d(LOG_TAG, " agregarDato-imei:" + mImei.getText().toString());
                pst.setString(1, "2");
                pst.setString(2, mDispositivo.getText().toString());

                pst.setString(3, mSerie.getText().toString());
                pst.setString(4, mImei.getText().toString());
                pst.setString(5, getMacAddr());
                pst.setBoolean(6, true);
                Log.d(LOG_TAG, " pst.executeUpdate:");
                InsertBD insertBD=new InsertBD();
                InsertBD.OnInsertResult onInsertResult;
                onInsertResult= new InsertBD.OnInsertResult() {
                    @Override
                    public void onResultSuccess(String msg) {
                        Log.d(LOG_TAG, " executeUpdate success: "+msg);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "agregarDato correctamente", Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onResultFail(String errorMessage) {
                        Log.d(LOG_TAG, " executeUpdate onResultFail: "+errorMessage);
                    }
                };
                insertBD.setOnResultListener(onInsertResult);
                insertBD.execute(pst);




            } catch (SQLException e) {
                Log.d(LOG_TAG, " agregarDato error " + e.toString());
                Toast.makeText(getApplicationContext(), "Error:" + e.getMessage().toString(), Toast.LENGTH_LONG).show();

            }

        }
    }

    public void loadIMEI() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
//                get_imei_data();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } else {

            TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mImei.setText(mngr.getDeviceId());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mImei.setText(mngr.getDeviceId());


            } else {
//                Toast.makeText(this, "ehgehfg", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

