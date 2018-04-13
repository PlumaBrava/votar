package com.ncodata.votar;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ncodata.votar.sql.ConnextionBD;
import com.ncodata.votar.sql.InsertBD;
import com.ncodata.votar.sql.QueryBD;
import com.ncodata.votar.sql.UpdateBD;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
//    private PowerManager mPowerManager;
//    private PowerManager.WakeLock mWakeLock;
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
    private Integer mDispositivosIndice;
    private Integer mDispositivosIncremento;


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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
                buscarMacEnDispositivos();

            }
        });

        final Button apagar=(Button)findViewById(R.id.apagar);
        apagar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, " apagar on Click" );
                apagar();
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
                            leerSecuenciaDispositivos();
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
        Log.i("TAG","serie android.os.Build.SERIAL: " + Build.SERIAL);
        String androidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.i("TAG","serie Secure.ANDROID_ID: " + androidDeviceId);
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
        mDispositivo.setText((sharedPref.getString(getString(R.string.preference_nombreDispositivo), getString(R.string.preference_nombreDispositivo_default))));
    }


    @Override
    protected void onResume() {
        Log.d(LOG_TAG, " onResume ");
        if (conn == null) {
            Log.d(LOG_TAG, " onResume conn null");
            connextionBD = new ConnextionBD();
            connextionBD.setOnResultListener(onConnectionResult);
            SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
            String ip = (sharedPref.getString(getString(R.string.preference_ip), getString(R.string.preference_ip_default)));
            String nombreBase = (sharedPref.getString(getString(R.string.preference_NombreBase), getString(R.string.preference_NombreBase_default)));
            String userBase = (sharedPref.getString(getString(R.string.preference_userBase), getString(R.string.preference_userBase_default)));
            String passwordBase = (sharedPref.getString(getString(R.string.preference_PasswordBase), getString(R.string.preference_PasswordBase_default)));
            connextionBD.setDatabase(ip, nombreBase, userBase, passwordBase);
            if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                connextionBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                connextionBD.execute();
            }
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

    public void leerSecuenciaDispositivos() {
        Log.d(LOG_TAG, " leerSecuenciaDispositivos ");
        if (conn != null) {
            Log.d(LOG_TAG, " leerSecuenciaDispositivos  conn != null");

//            /En el stsql se puede agregar cualquier consulta SQL deseada.
            String stsql = "Select * FROM gen_Secuencias where Secuencia= 'age_Dispositivos'";
            QueryBD.QueryData queryData = new QueryBD.QueryData(conn, stsql);
            QueryBD queryBD = new QueryBD();
            QueryBD.OnQueryResult onQueryResult;
            onQueryResult = new QueryBD.OnQueryResult() {
                @Override
                public void onResultSuccess(final ResultSet rs) {
                    Log.d(LOG_TAG, " leerSecuenciaDispositivos query success: " + rs);
                    int row = 0;
                    String concejal = "";
                    try {
                        while (rs.next()) {
                            row++;

                            Log.d(LOG_TAG, "Valor: " + rs.getInt("Valor"));
                            Log.d(LOG_TAG, "Incremento: " + rs.getInt("Incremento"));
                            mDispositivosIndice = rs.getInt("Valor");
                            mDispositivosIncremento = rs.getInt("Incremento");
//                            Log.d(LOG_TAG, "NumDispositivo: " + rs.getString("NumDispositivo") + "- serie: " + rs.getString("Serie") + " - Macaddresses:  " + rs.getString("Macaddresses"));

                        }
                        final int finalRow = row;
                        final String finalConcejal = concejal;
                        runOnUiThread(new Runnable() {
                            public void run() {


                            }
                        });

                    } catch (SQLException e) {

                        Log.d(LOG_TAG, " leerSecuenciaDispositivos catch : " + e.getMessage());
                        e.printStackTrace();
                    }


                }

                @Override
                public void onResultFail(final String errorMessage) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), " leerSecuenciaDispositivos : " + errorMessage, Toast.LENGTH_LONG).show();
                            Log.d(LOG_TAG, " leerSecuenciaDispositivos onResultFail: " + errorMessage);
                        }
                    });
                }
            };
            queryBD.setOnResultListener(onQueryResult);

            if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                queryBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,queryData);
            } else {
                queryBD.execute(queryData);
            }
            Log.d(LOG_TAG, " leerSecuenciaDispositivos: qwuery");


        }else{
        Log.d(LOG_TAG, " leerSecuenciaDispositivos  conn == null");
        Toast.makeText(getApplicationContext(), "Error:" + "Sin conexion", Toast.LENGTH_LONG).show();
        }
    }

    public void registar() {


        if (conn != null) {
            try {
                PreparedStatement pst = conn.prepareStatement("insert into age_Dispositivos "
                        + "(NumDispositivo,Dispositivo, Serie, Imei, Macaddresses, Estado)"
                        + "values (?,?,?,?,?,?)");


                Log.d(LOG_TAG, " agregarDato-connection:" + conn.toString());
                Log.d(LOG_TAG, " agregarDato-imei:" + mImei.getText().toString());
                pst.setInt(1, mDispositivosIndice + mDispositivosIncremento);
                pst.setString(2, mDispositivo.getText().toString());

                pst.setString(3, mSerie.getText().toString());
                pst.setString(4, mImei.getText().toString());
                pst.setString(5, getMacAddr());
                pst.setBoolean(6, true);
                Log.d(LOG_TAG, " pst.executeUpdate:");
                InsertBD insertBD = new InsertBD();
                InsertBD.OnInsertResult onInsertResult;
                onInsertResult = new InsertBD.OnInsertResult() {
                    @Override
                    public void onResultSuccess(String msg) {
                        Log.d(LOG_TAG, " executeUpdate success: " + msg);
                        IncrementarSecuenciaDispositivos();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.preference_nombreDispositivo),mDispositivo.getText().toString());
                                editor.commit();

                                Toast.makeText(getApplicationContext(), "registar correcto", Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onResultFail(final String errorMessage) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), " registar : " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });

                        Log.d(LOG_TAG, " executeUpdate onResultFail: " + errorMessage);
                    }
                };
                insertBD.setOnResultListener(onInsertResult);
//                insertBD.execute(pst);
                if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                    insertBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pst);
                } else {
                    insertBD.execute(pst);
                }


            } catch (SQLException e) {
                Log.d(LOG_TAG, " agregarDato error " + e.toString());
                Toast.makeText(getApplicationContext(), "Error:" + e.getMessage().toString(), Toast.LENGTH_LONG).show();

            }

        }
    }

    public void updateRegistro() {

        if (conn != null) {
            Log.d(LOG_TAG, " updateRegistro conn != null");

            PreparedStatement pst = null;
            try {


                pst = conn.prepareStatement("UPDATE age_Dispositivos "
                        + " SET Dispositivo= ? ,"
                        + "  Serie= ? ,"
                        + "  Imei= ? ,"
                        + "  Macaddresses= ?, "
                        + "  Estado= ? "
                        + " WHERE  NumDispositivo= ? ");


                pst.setString(1, mDispositivo.getText().toString());

                pst.setString(2, mSerie.getText().toString());
                pst.setString(3, mImei.getText().toString());
                pst.setString(4, getMacAddr());
                pst.setBoolean(5, true);
                pst.setInt(6, mDispositivosIndice);


                UpdateBD updateBD = new UpdateBD();
                UpdateBD.OnUpdateResult onUpDateResult;
                onUpDateResult = new UpdateBD.OnUpdateResult() {
                    @Override
                    public void onResultSuccess(int cantidadLineasModificadas) {
                        Log.d(LOG_TAG, "  updateRegistro: success: " + cantidadLineasModificadas);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.preference_nombreDispositivo),mDispositivo.getText().toString());
                                editor.commit();
                                Toast.makeText(getApplicationContext(), " updateRegistro: con Éxito", Toast.LENGTH_LONG).show();

                            }
                        });

                    }

                    @Override
                    public void onResultFail(final String errorMessage) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), " updateRegistro: " + errorMessage, Toast.LENGTH_LONG).show();
                                Log.d(LOG_TAG, "  updateRegistro: onResultFail: " + errorMessage);
                            }
                        });
                    }

                };
                updateBD.setOnResultListener(onUpDateResult);
                ;

//                    updateBD.execute(pst);
                    if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                        updateBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pst);
                    } else {
                        updateBD.execute(pst);
                    }

                Log.d(LOG_TAG, " updateRegistro  pst.executeUpdate");


            } catch (SQLException e) {
                Log.d(LOG_TAG, " updateRegistro  catch " + e.getMessage());
                e.printStackTrace();
            }

        }

        Log.d(LOG_TAG, " updateRegistro  catch  conn==null");
    }


    public void IncrementarSecuenciaDispositivos() {

        if (conn != null) {
            Log.d(LOG_TAG, " IncrementarSecuenciaDispositivos conn == null");

            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("UPDATE gen_Secuencias "
                        + " SET Valor= ?"
                        + "WHERE  Secuencia= ?");

                pst.setInt(1, mDispositivosIndice + mDispositivosIncremento);
                pst.setString(2, "age_Dispositivos");

                UpdateBD updateBD = new UpdateBD();
                UpdateBD.OnUpdateResult onUpDateResult;
                onUpDateResult = new UpdateBD.OnUpdateResult() {
                    @Override
                    public void onResultSuccess(int cantidadLineasModificadas) {
                        Log.d(LOG_TAG, "  IncrementarSecuenciaDispositivos: success: " + cantidadLineasModificadas);
                        registar();
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(getApplicationContext(), " IncrementarSecuenciaDispositivos: con Éxito", Toast.LENGTH_LONG).show();

                            }
                        });

                    }

                    @Override
                    public void onResultFail(final String errorMessage) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), " IncrementarSecuenciaDispositivos: " + errorMessage, Toast.LENGTH_LONG).show();
                                Log.d(LOG_TAG, "  IncrementarSecuenciaDispositivos: onResultFail: " + errorMessage);
                            }
                        });

                    }

                };
                updateBD.setOnResultListener(onUpDateResult);
                ;
                if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                    updateBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pst);
                } else {
                    updateBD.execute(pst);
                }
                Log.d(LOG_TAG, " IncrementarSecuenciaDispositivos  pst.executeUpdate");


            } catch (SQLException e) {
                Log.d(LOG_TAG, " IncrementarSecuenciaDispositivos  catch " + e.getMessage());
                e.printStackTrace();
            }

        }

        Log.d(LOG_TAG, " IncrementarSecuenciaDispositivos  catch  conn==null");
    }


    public void buscarMacEnDispositivos() {
        Log.d(LOG_TAG, " buscarMacEnDispositivos ");
        if (conn != null) {
            Log.d(LOG_TAG, " buscarMacEnDispositivos  conn != null");

//            /En el stsql se puede agregar cualquier consulta SQL deseada.
            String mac = getMacAddr();
            String stsql = "Select * FROM age_Dispositivos where Macaddresses= '" + mac + "'";
            QueryBD.QueryData queryData = new QueryBD.QueryData(conn, stsql);
            QueryBD queryBD = new QueryBD();
            QueryBD.OnQueryResult onQueryResult;
            onQueryResult = new QueryBD.OnQueryResult() {
                @Override
                public void onResultSuccess(final ResultSet rs) {
                    Log.d(LOG_TAG, " buscarMacEnDispositivos query success: " + rs);
                    int row = 0;
                    String concejal = "";
                    try {
                        while (rs.next()) {
                            row++;

                            Log.d(LOG_TAG, "buscarMacEnDispositivos NumDispositivo: " + rs.getInt("NumDispositivo"));
                            Log.d(LOG_TAG, "buscarMacEnDispositivos Macaddresses: " + rs.getString("Macaddresses"));
                            mDispositivosIndice=rs.getInt("NumDispositivo");
//                            mDispositivosIndice = rs.getInt("Valor");
//                            mDispositivosIncremento = rs.getInt("Incremento");
//                            Log.d(LOG_TAG, "NumDispositivo: " + rs.getString("NumDispositivo") + "- serie: " + rs.getString("Serie") + " - Macaddresses:  " + rs.getString("Macaddresses"));

                        }
                        final int finalRow = row;
                        final String finalConcejal = concejal;

                        if (finalRow == 1) {
                            Log.d(LOG_TAG, "buscarMacEnDispositivos Row=1: ");
                            updateRegistro();
                        } else if (finalRow == 0) {
                            Log.d(LOG_TAG, "buscarMacEnDispositivos Row=0: ");
                            IncrementarSecuenciaDispositivos();

                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Log.d(LOG_TAG, "buscarMacEnDispositivos Row>1 errror: ");
                                    Toast.makeText(getApplicationContext(), " buscarMacEnDispositivos : " + " Mac Repetida", Toast.LENGTH_LONG).show();
//                                if (finalRow == 1) {
//                                    mConcejalAsignado.setText(finalConcejal);
//                                } else if (finalRow > 1) {
//                                    mConcejalAsignado.setText(getString(R.string.asignacionConcejalMultiple));
//                                } else if (finalRow == 0) {
//                                    mConcejalAsignado.setText(getString(R.string.asignacionConcejalSinAsignar));
//                                }
                                }
                            });
                        }
                    } catch (SQLException e) {

                        Log.d(LOG_TAG, " buscarMacEnDispositivos catch : " + e.getMessage());
                        e.printStackTrace();
                    }


                }

                @Override
                public void onResultFail(final String errorMessage) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), " buscarMacEnDispositivos : " + errorMessage, Toast.LENGTH_LONG).show();
                            Log.d(LOG_TAG, " buscarMacEnDispositivos onResultFail: " + errorMessage);
                        }
                    });
                }
            };
            queryBD.setOnResultListener(onQueryResult);
//            queryBD.execute(queryData);
            if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                queryBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,queryData);
            } else {
                queryBD.execute(queryData);
            }

            Log.d(LOG_TAG, " buscarMacEnDispositivos: qwuery");


        }else {
            Log.d(LOG_TAG, " buscarMacEnDispositivos  conn == null");
            Toast.makeText(getApplicationContext(), "Error:" + "Sin conexion", Toast.LENGTH_LONG).show();
        }
    }
    @TargetApi(21) //Suppress lint error for PROXIMITY_SCREEN_OFF_WAKE_LOCK
    public  void apagar(){
        Log.v("apagar", "OFF!");
//        Intent i = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
//        i.putExtra("android.intent.extra.KEY_CONFIRM", false);
//        i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(i);
//        Intent shutdown = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
////        shutdown.putExtra("android.intent.extra.KEY_CONFIRM", true);
////        shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        shutdown.putExtra("android.intent.extra.KEY_CONFIRM", false);
//        shutdown.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//        shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getApplicationContext(). startActivity(shutdown);

//        try {
//            Process proc = Runtime.getRuntime()
//                    .exec(new String[]{ "su", "-c", "reboot -p" });
//            proc.waitFor();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        WindowManager.LayoutParams params = getWindow().getAttributes();
        Log.v("apagar params",params.toString());
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.screenBrightness = 0.1f;
        getWindow().setAttributes(params);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "tag");
//        mWakeLock.acquire();

//            // turn off screen
            Log.v("apagar", "OFF!");
//        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
////            mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "tag");
////            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");
////            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "tag");
//            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");
//            mWakeLock.acquire();
//



    }
}

