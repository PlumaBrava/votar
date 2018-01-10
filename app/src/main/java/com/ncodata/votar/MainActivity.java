package com.ncodata.votar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ncodata.votar.sql.ConnextionBD;
import com.ncodata.votar.sql.InsertBD;
import com.ncodata.votar.sql.QueryBD;
import com.ncodata.votar.sql.UpdateBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.ncodata.votar.utils.Cifrado.encriptadoJJ;
import static com.ncodata.votar.utils.SporteConf.getMacAddr;


public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "MainActivity";

    String IMEI_Number_Holder;
    String device_unique_id;
    String IMEI = null;
    TelephonyManager telephonyManager;

    Connection conn = null;//Conexion a la base de datos. Al entrar a la actividad nos conectamos y al salir nos desconectamos.
    ConnextionBD connextionBD; //Tarea que se ejecuta en segundo plano para conseguir una conexion a la base
    ConnextionBD.OnConnectionResult onConnectionResult;

    private TextView mConcejalAsignado;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private TextInputLayout mConfirmarPasswordLY;
    private View mProgressView;
    private View mLoginFormView;
    private int mIntentosLogin = 0;
    private ImageView mIconoBaseConectada;
    private ImageView mIconoBaseDesonectada;
    private Button mButtonIngresar;
    private Button mButtonSaveNewPassword;
    private int mNumeroConcejal;
    private int mNumeroDispositivo;
    private boolean mFfuncion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, " onCreate ");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressView = findViewById(R.id.registro_progress);
        mLoginFormView = findViewById(R.id.layout_password);

        mConcejalAsignado = (TextView) findViewById(R.id.concejalAsignado);
        mPassword = (EditText) findViewById(R.id.password);
        mConfirmPassword = (EditText) findViewById(R.id.confirmarPassword);
        mConfirmarPasswordLY = (TextInputLayout) findViewById(R.id.confirmarPasswordLY);
        mIconoBaseConectada = (ImageView) findViewById(R.id.inconoBaseConectada);
        mIconoBaseDesonectada = (ImageView) findViewById(R.id.inconoBaseDesonectada);


        mButtonIngresar = (Button) findViewById(R.id.button_ingresar);

        mButtonIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "encriptado " + encriptadoJJ(mPassword.getText().toString()));

                if (mPassword.getText().toString().equals(getApplicationContext().getText(R.string.passwor_configuracion))) {
                    Log.d(LOG_TAG, "mPassword.getText: configuracion");
                    Intent intent = new Intent(getApplication(), ConfiguracionActivity.class);
                    startActivity(intent);
                } else {


                    attemptLogin();
                }


            }
        });

        mButtonSaveNewPassword = (Button) findViewById(R.id.button_saveNewPassWord);
        mButtonSaveNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSavePassword();
            }
        });


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
                            leerConsejalAsignado();
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


    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, " onResume ");
        if (conn == null) {
            Log.d(LOG_TAG, " onResume conn null");
            connextionBD = new ConnextionBD();
            SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
            String ip = (sharedPref.getString(getString(R.string.preference_ip), getString(R.string.preference_ip_default)));
            String nombreBase = (sharedPref.getString(getString(R.string.preference_NombreBase), getString(R.string.preference_NombreBase_default)));
            String userBase = (sharedPref.getString(getString(R.string.preference_userBase), getString(R.string.preference_userBase_default)));
            String passwordBase = (sharedPref.getString(getString(R.string.preference_PasswordBase), getString(R.string.preference_PasswordBase_default)));
            connextionBD.setDatabase(ip, nombreBase, userBase, passwordBase);
            connextionBD.setOnResultListener(onConnectionResult);
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

            Log.d(LOG_TAG, "id" + id);
            Intent intent = new Intent(getApplication(), ConfiguracionActivity.class);

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void ingresaConcejalEnSesion(int numeroConcejal, int numeroDispositivo, boolean funcion, String mac) {

        Log.d(LOG_TAG, " ingresaConcejalEnSesion:" + conn.toString());
        if (conn != null) {
            try {
                PreparedStatement pst = conn.prepareStatement("insert age_Sesiones"
                        + "(NumConcejal, NumDispositivo, Funcion, Macaddresses,Habilitado, IniciaTexto, Titulo , Texto, InicioVoto , ResultadoVoto , TiempoVotacion , TiempoInicio , TiempoFin)"
                        + "values (?,?,?,?,?,?,?,?,?,?,?,?,?)");


                Log.d(LOG_TAG, " ingresaConcejalEnSesion-connection:" + conn.toString());

                pst.setInt(1, numeroConcejal);
                pst.setInt(2, numeroDispositivo);

                pst.setBoolean(3, funcion);
                pst.setString(4, mac);
                pst.setBoolean(5, false);//habilitado
                pst.setBoolean(6, false);// IniciaTexto
                pst.setString(7, "Titulo");//Titulo
                pst.setString(8, "Texto");// Texto
                pst.setBoolean(9, false);//iniciaVoto
                pst.setInt(10, 0);//ResultadoVoto
                pst.setInt(11, 30);// TiempoVotacion
                pst.setString(12, "InicioTiempo");//tiempoInicio
                pst.setString(13, "tiempoFin");//Tiempo fin
                Log.d(LOG_TAG, " pst.executeUpdate:");
                InsertBD insertBD = new InsertBD();
                InsertBD.OnInsertResult onInsertResult;
                onInsertResult = new InsertBD.OnInsertResult() {
                    @Override
                    public void onResultSuccess(String msg) {
                        Log.d(LOG_TAG, " ingresaConcejalEnSesion success: " + msg);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "agregarDato correctamente", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplication(), VotacionActivity.class);

                                startActivity(intent);
                            }
                        });

                    }

                    @Override
                    public void onResultFail(String errorMessage) {
                        Log.d(LOG_TAG, " ingresaConcejalEnSesion onResultFail: " + errorMessage);
                    }
                };
                insertBD.setOnResultListener(onInsertResult);
                insertBD.execute(pst);


            } catch (SQLException e) {
                Log.d(LOG_TAG, " ingresaConcejalEnSesion error " + e.toString());
                Toast.makeText(getApplicationContext(), "Error:" + e.getMessage().toString(), Toast.LENGTH_LONG).show();

            }

        } else {
            Toast.makeText(getApplicationContext(), "Error: No Hay conextion a la base", Toast.LENGTH_SHORT).show();
        }
    }


    public void modificarDato() {
        Log.d(LOG_TAG, " modificarDato");


        if (conn != null) {
            try {

                PreparedStatement pst = conn.prepareStatement("UPDATE age_Dispositivos "
                        + " SET Dispositivo= ?, Serie=?, Imei=?, Macaddresses=?, Estado=?"
                        + "WHERE NumDispositivo = 5"
                );


                pst.setString(1, "Motorola ");
                pst.setString(2, "serie");
                pst.setString(3, "Imei");
                pst.setString(4, getMacAddr());
                pst.setBoolean(5, true);


                UpdateBD updateBD = new UpdateBD();
                UpdateBD.OnUpdateResult onUpDateResult;
                onUpDateResult = new UpdateBD.OnUpdateResult() {
                    @Override
                    public void onResultSuccess(int cantidadLineasModificadas) {
                        Log.d(LOG_TAG, " executeUpdate success: " + cantidadLineasModificadas);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Modificar dato correctamente", Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onResultFail(String errorMessage) {
                        Log.d(LOG_TAG, " Update onResultFail: " + errorMessage);
                    }
                };
                updateBD.setOnResultListener(onUpDateResult);
                updateBD.execute(pst);

            } catch (SQLException e) {
                Log.d(LOG_TAG, " modificarDato error " + e.toString());
                Toast.makeText(getApplicationContext(), "Error:" + e.getMessage().toString(), Toast.LENGTH_LONG).show();

            }

        }
        Toast.makeText(getApplicationContext(), "Sin conexion a la base", Toast.LENGTH_LONG).show();
    }

    public void leerConsejalAsignado() {
        Log.d(LOG_TAG, " leerConsejalAsignado:");
        if (conn != null) {

            String mac = getMacAddr();
//            /En el stsql se puede agregar cualquier consulta SQL deseada.
            String stsql = "Select *, age_Concejales.NumConcejal, age_Concejales.Concejal as NombreConcejal FROM age_Concejales_Dispositivos " +
                    "INNER JOIN age_Concejales ON age_Concejales_Dispositivos.NumConcejal= age_Concejales.NumConcejal " +
                    "where Macaddresses='" + mac + "'";
            QueryBD.QueryData queryData = new QueryBD.QueryData(conn, stsql);
            QueryBD queryBD = new QueryBD();
            QueryBD.OnQueryResult onQueryResult;
            onQueryResult = new QueryBD.OnQueryResult() {
                @Override
                public void onResultSuccess(final ResultSet rs) {
                    Log.d(LOG_TAG, " query success: " + rs);
                    int row = 0;
                    String concejal = "";
                    try {
                        while (rs.next()) {
                            row++;
                            concejal = rs.getString("NombreConcejal");
                            Log.d(LOG_TAG, "concejal: " + rs.getString("NombreConcejal"));
//                            Log.d(LOG_TAG, "NumDispositivo: " + rs.getString("NumDispositivo") + "- serie: " + rs.getString("Serie") + " - Macaddresses:  " + rs.getString("Macaddresses"));

                        }
                        final int finalRow = row;
                        final String finalConcejal = concejal;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (finalRow == 1) {
                                    mConcejalAsignado.setText(finalConcejal);
                                } else if (finalRow > 1) {
                                    mConcejalAsignado.setText(getString(R.string.asignacionConcejalMultiple));
                                } else if (finalRow == 0) {
                                    mConcejalAsignado.setText(getString(R.string.asignacionConcejalSinAsignar));
                                }
                            }
                        });

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onResultFail(String errorMessage) {
                    Log.d(LOG_TAG, " executeUpdate onResultFail: " + errorMessage);
                }
            };
            queryBD.setOnResultListener(onQueryResult);
            queryBD.execute(queryData);

            Log.d(LOG_TAG, " leerDato: qwuery");


        }
    }


    /**
     * Verifica si el Mac y clave existen en la tabla de Consejales-Dispositivos
     * .
     */
    public void userLoginTask(final String mac, String pass) {
        String secetPassword = "";
        SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
        String clave_default = sharedPref.getString(getString(R.string.password_default_key), getString(R.string.password_default));
        Log.d(LOG_TAG, " userLoginTask: " + clave_default);
        Log.d(LOG_TAG, " userLoginTask: " + pass);
        Log.d(LOG_TAG, " userLoginTask: " + clave_default.equals(pass));


        if (clave_default.equals(pass)) {
            secetPassword = pass; //Si es la clave default no la encripta
        } else {
            secetPassword = encriptadoJJ(pass);
        }
//
//            try {
//                secret = generateKey();
//                secetPassword = String.valueOf(encryptMsg(pass, secret));
//                Log.d(LOG_TAG, " secetPassword " + secetPassword);
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            } catch (NoSuchPaddingException e) {
//                e.printStackTrace();
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//            } catch (InvalidParameterSpecException e) {
//                e.printStackTrace();
//            } catch (IllegalBlockSizeException e) {
//                e.printStackTrace();
//            } catch (BadPaddingException e) {
//                e.printStackTrace();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            } catch (InvalidKeySpecException e) {
//                e.printStackTrace();
//            }
//
//            decryptMsg(byte[] toDecrypt, secret));
//    }


        if (conn != null) {


//            /En el stsql se puede agregar cualquier consulta SQL deseada.
            Log.d(LOG_TAG, " userLoginTask:secetPassword " + secetPassword);
            String stsql = "Select * FROM age_Concejales_Dispositivos where Macaddresses='" + mac + "' and Clave='" + secetPassword + "'";
            QueryBD.QueryData queryData = new QueryBD.QueryData(conn, stsql);
            QueryBD queryBD = new QueryBD();
            QueryBD.OnQueryResult onQueryResult;
            onQueryResult = new QueryBD.OnQueryResult() {
                @Override
                public void onResultSuccess(final ResultSet rs) {
                    Log.d(LOG_TAG, " query success: " + rs);


                    try { // reviso el Nro de filas de resulset si es uno el password es correcto
                        int rows = 0;
                        int numeroConcejal = 0;
                        int numeroDispositivo = 0;
                        boolean funcion = false;

                        while (rs.next()) {
                            rows++;
//                            Log.d(LOG_TAG, "NumDispositivo: " + rs.getString("NumDispositivo") + "- serie: " + rs.getString("Serie") + " - Macaddresses:  " + rs.getString("Macaddresses"));

                            numeroConcejal = rs.getInt("NumConcejal");
                            numeroDispositivo = rs.getInt("NumDispositivo");
                            funcion = rs.getBoolean("Funcion");
                        }

                        final int finalNumeroConcejal = numeroConcejal;
                        final int finalNumeroDispositivo = numeroDispositivo;
                        final boolean finalFuncion = funcion;

                        Log.d(LOG_TAG, " query rows: " + rows);
                        if (rows == 1) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    showProgress(false);

                                    SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
                                    String clave_default = sharedPref.getString(getString(R.string.password_default_key), getString(R.string.password_default));
                                    Log.d(LOG_TAG, " clave_default: " + clave_default + "--" + mPassword.getText().toString());

                                    if (mPassword.getText().toString().equals(clave_default)) {
                                        Log.d(LOG_TAG, "mPassword.getText: default");
                                        mConfirmarPasswordLY.setVisibility(View.VISIBLE);
                                        mPassword.setText("");
                                        mPassword.setVisibility(View.VISIBLE);
                                        mButtonIngresar.setVisibility(View.GONE);
                                        mButtonSaveNewPassword.setVisibility(View.VISIBLE);

                                    } else {

                                        ingresaConcejalEnSesion(finalNumeroConcejal, finalNumeroDispositivo, finalFuncion, mac);

                                    }
                                }
                            });
                        } else { //password Incorrecto
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    showProgress(false);
                                    Toast.makeText(getApplicationContext(), "consulta coorecta Incorrecto Password", Toast.LENGTH_LONG).show();
                                    mPassword.setError(getString(R.string.error_invalid_password));
                                    mPassword.requestFocus();
                                    // Store values at the time of the login attempt.
                                    mIntentosLogin++;
                                    if (mIntentosLogin >= getResources().getInteger(R.integer.IntentosLogin)) {
                                        Log.d(LOG_TAG, "mIntentosLogin mayor" + mIntentosLogin);
//                                        Snackbar.make(mLoginFormView, getText(R.string.password_Supero_Cantidad_Intentos_Texto), Snackbar.LENGTH_LONG)
//                                                .setAction("Action", null).show();
                                        new AlertDialog.Builder(MainActivity.this).setTitle(getText(R.string.password_Supero_Cantidad_Intentos_Titulo))
                                                .setMessage(getText(R.string.password_Supero_Cantidad_Intentos_Texto))
                                                .setPositiveButton(getText(R.string.password_Supero_Cantidad_OK),
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                // Perform Action & Dismiss dialog
                                                                dialog.dismiss();
                                                            }
                                                        })
//                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // Do nothing
//                            dialog.dismiss();
//                        }
//                    })
                                                .create()
                                                .show();

                                        mIntentosLogin = 0;
                                    }
                                }
                            });
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onResultFail(String errorMessage) {
                    Log.d(LOG_TAG, " executeUpdate onResultFail: " + errorMessage);
                    mPassword.setError(getString(R.string.error_incorrect_password));
                    mPassword.requestFocus();
                }
            };
            queryBD.setOnResultListener(onQueryResult);
            queryBD.execute(queryData);

            Log.d(LOG_TAG, " UserLoginTask: qwuery");


        }

    }


    private void attemptLogin() {

        mPassword.setError(null);


        Log.d(LOG_TAG, " mIntentosLogin " + mIntentosLogin);
        String password = mPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.error_null_password));
            focusView = mPassword;
            cancel = true;
        } else

            // Check for a valid password, if the user entered one.
            if (!isPasswordValid(password)) {
                mPassword.setError(getString(R.string.error_too_short_password));
                focusView = mPassword;
                cancel = true;
            }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            userLoginTask(getMacAddr(), password);

        }
    }


    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        Log.d(LOG_TAG, " showProgress");
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

            Log.d(LOG_TAG, " showProgress: in " + Build.VERSION.SDK_INT);
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
            Log.d(LOG_TAG, " showProgress: out ");
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void attemptSavePassword() {

        mPassword.setError(null);
        mConfirmPassword.setError(null);


        String password = mPassword.getText().toString();
        String confirmPassword = mConfirmPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;
        SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
        String clave_default = sharedPref.getString(getString(R.string.password_default_key), getString(R.string.password_default));


        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.error_null_password));
            focusView = mPassword;
            cancel = true;
        } else
            // Check for a valid password, if the user entered one.
            if (!isPasswordValid(password)) {
                mPassword.setError(getString(R.string.error_too_short_password));
                focusView = mPassword;
                cancel = true;
            } else if (password.equals(clave_default)) {
                mPassword.setError(getString(R.string.error_password_iqual_default));
                focusView = mPassword;
                cancel = true;
            } else if (!TextUtils.isEmpty(confirmPassword) && !isPasswordValid(confirmPassword)) {
                mConfirmPassword.setError(getString(R.string.error_too_short_password));
                focusView = mConfirmPassword;
                cancel = true;
            } else if (!confirmPassword.equals(password)) {
                mConfirmPassword.setError(getString(R.string.error_passwordDistintos));
                focusView = mConfirmPassword;
                cancel = true;
            }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            savePasswordTask(getMacAddr(), password);

        }
    }

    public void savePasswordTask(String mac, String password) {
        Log.d(LOG_TAG, " savePasswordTask " + mac + " " + password);
        String secetPassword = encriptadoJJ(password);

//        SecretKey secret = null;
//        String secetPassword="";
//        try {
//            secret = generateKey();
//            secetPassword= String.valueOf(encryptMsg(password, secret));
//            Log.d(LOG_TAG, " secetPassword " +secetPassword);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (InvalidParameterSpecException e) {
//            e.printStackTrace();
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        }


        if (conn != null) {
            try {

                PreparedStatement pst = conn.prepareStatement("UPDATE age_Concejales_Dispositivos "
                        + " SET Clave= ? "
                        + "WHERE Macaddresses= ?"
                );


                pst.setString(1, secetPassword);
                pst.setString(2, mac);

                UpdateBD updateBD = new UpdateBD();
                UpdateBD.OnUpdateResult onUpDateResult;
                onUpDateResult = new UpdateBD.OnUpdateResult() {
                    @Override
                    public void onResultSuccess(int cantidadLineasModificadas) {
                        Log.d(LOG_TAG, " savePasswordTask success: " + cantidadLineasModificadas);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "La clave se grabo con Éxito", Toast.LENGTH_LONG).show();
                                showProgress(false);
                                mConfirmarPasswordLY.setVisibility(View.GONE);
                                mPassword.setText("");
                                mPassword.setVisibility(View.VISIBLE);
                                mButtonIngresar.setVisibility(View.VISIBLE);
                                mButtonSaveNewPassword.setVisibility(View.GONE);
                            }
                        });

                    }

                    @Override
                    public void onResultFail(String errorMessage) {
                        Log.d(LOG_TAG, " SavePasswordTask onResultFail: " + errorMessage);
                    }
                };
                updateBD.setOnResultListener(onUpDateResult);
                updateBD.execute(pst);

            } catch (SQLException e) {
                Log.d(LOG_TAG, " SavePasswordTask error " + e.toString());
                Toast.makeText(getApplicationContext(), "Error:" + e.getMessage().toString(), Toast.LENGTH_LONG).show();

            }
            Toast.makeText(getApplicationContext(), "Sin conexion a la base", Toast.LENGTH_LONG).show();
        }


    }

}
