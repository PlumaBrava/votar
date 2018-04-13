package com.ncodata.votar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import static com.ncodata.votar.utils.SporteConf.getMacAddr;

public class VotacionActivity extends AppCompatActivity {
    public static final String LOG_TAG = "VotacionActivity";

    Connection conn = null;//Conexion a la base de datos. Al entrar a la actividad nos conectamos y al salir nos desconectamos.
    ConnextionBD connextionBD; //Tarea que se ejecuta en segundo plano para conseguir una conexion a la base
    ConnextionBD.OnConnectionResult onConnectionResult;
    private ImageView mIconoBaseConectada;
    private ImageView mIconoBaseDesonectada;

    public static final int VOTO_NEGATIVO = 0;
    public static final int VOTO_POSITIVO = 1;
    public static final int VOTO_ABSTENCION = 2;

    private ProgressBar pbarProgreso;
    private NestedScrollView mNestedSecrollViewVotacion;
    private TimerTask mTareaTimer;
    private Long mtiempoInicioVotacion;
    private InspectorBanderas mInspectorBanderas;
    private TextView mTextTimerVotacion;
    private TextView mTextParrafo;
    private String mEsperaTitulo;
    private String mEsperaTexto;
    private Button mVotoSeleccionado;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Button mVotoPositivo;
    private Button mVotoNegativo;
    private Button mConfirmaVotoPositivo;
    private Button mConfirmaVotoNegativo;
    private Button mCancelarVoto;
    private String mMac;
    private int mTiempoVotacion=0;
    private int mTratamientoAbstencion=2;// Es 0: voto negativo, 1: Voto Positivo, 2: abstencion
    private Boolean mActualizandoDatosTimerVoto=false;
    private Boolean mActualizandoDatosTextos=false;
    private Boolean mTextosInicializados=false;
    private Boolean mApagar=false;
    private String mConcejal="";
    private Boolean mFuncion;
    private String mAbreviacion;
    private TextView mTextConcejal;
    View decorView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mConcejal=intent.getStringExtra("concejal");
        mAbreviacion=intent.getStringExtra("abreviacion");
        mFuncion=intent.getBooleanExtra("funcion",false);

        setContentView(R.layout.activity_votacion);
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        getActionBar().hide();

//        if (Build.VERSION.SDK_INT < 16) {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
//        toolbar.setTitle("titulo");
//        toolbar.setSubtitle("sub t");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mIconoBaseConectada = (ImageView) findViewById(R.id.inconoBaseConectada);
        mIconoBaseDesonectada = (ImageView) findViewById(R.id.inconoBaseDesonectada);
        mTextConcejal=(TextView)findViewById(R.id.concejal);
        mTextConcejal.setText(mAbreviacion);

        mNestedSecrollViewVotacion = (NestedScrollView) findViewById(R.id.nestedSecrollViewVotacion);







        pbarProgreso = (ProgressBar) findViewById(R.id.pbarProgreso);
        mTextTimerVotacion = (TextView) findViewById(R.id.textTimer);
        pbarProgreso.setVisibility(View.GONE);
        mTextTimerVotacion.setVisibility(View.GONE);



        mTextParrafo = (TextView) findViewById(R.id.textoParrafo);
        mVotoSeleccionado = (Button) findViewById(R.id.votoSeleccionado);
        mVotoSeleccionado.setVisibility(View.GONE);
//        mVotoSeleccionado.setVisibility(View.VISIBLE);

        mVotoPositivo= (Button) findViewById(R.id.VotoPositivo);
        mVotoNegativo = (Button) findViewById(R.id.VotoNegativo);
        mConfirmaVotoPositivo = (Button) findViewById(R.id.confirmaVotoPositivo);
        mConfirmaVotoNegativo = (Button) findViewById(R.id.confirmaVotoNegativo);
        mCancelarVoto = (Button) findViewById(R.id.cancelarVoto);


        mVotoPositivo.setVisibility(View.GONE);
        mVotoNegativo.setVisibility(View.GONE);
        mConfirmaVotoNegativo.setVisibility(View.GONE);
        mConfirmaVotoPositivo.setVisibility(View.GONE);
        mCancelarVoto.setVisibility(View.GONE);

        mVotoPositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation animationVotoPositivo =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.votoseleccionado);
                Animation showConfirmacion =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.showconfirmarvoto);

                Animation animacionhideBoton =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_boton_votacion);


                mVotoPositivo.startAnimation(animationVotoPositivo);
                mVotoNegativo.startAnimation(animacionhideBoton);
                mConfirmaVotoPositivo.startAnimation(showConfirmacion);




                mVotoNegativo.setVisibility(View.GONE);
                mVotoPositivo.setVisibility(View.GONE);
                mConfirmaVotoPositivo.setVisibility(View.VISIBLE);
                mCancelarVoto.setVisibility(View.VISIBLE);


            }
        });

        mConfirmaVotoPositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTareaTimer != null) {
                    mTareaTimer.cancel(true);

                }


                Animation hideConfirmacion =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_boton_votacion);

                Animation animacionShowVotoSeleccionado =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.showconfirmarvoto);



                mConfirmaVotoPositivo.startAnimation(hideConfirmacion);


                mVotoSeleccionado.setText(getText(R.string.texto_Voto_Positivo));
//                mVotoSeleccionado.setBackgroundColor((getResources().getColor(R.color.colorVotoPositivo)));
//                mVotoSeleccionado.getBackground().setColorFilter(getResources().getColor( R.color.colorVotoPositivo), PorterDuff.Mode.MULTIPLY);
                mVotoSeleccionado.setBackground((getResources().getDrawable(R.drawable.boton_confirma_voto_positivo)));
                mVotoSeleccionado.startAnimation(animacionShowVotoSeleccionado);
                mVotoSeleccionado.setVisibility(View.VISIBLE);

                votar(VOTO_POSITIVO);

                mTextTimerVotacion.setVisibility(View.GONE);
                pbarProgreso.setVisibility(View.GONE);
                mConfirmaVotoPositivo.setVisibility(View.GONE);
                mCancelarVoto.setVisibility(View.GONE);


            }
        });



        mVotoNegativo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation animationVotoNegativo =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.votoseleccionado);
                Animation showConfirmacion =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.showconfirmarvoto);

                Animation animacionhideBoton =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_boton_votacion);


                mVotoNegativo.startAnimation(animationVotoNegativo);
                mVotoPositivo.startAnimation(animacionhideBoton);
                mConfirmaVotoNegativo.startAnimation(showConfirmacion);

                mVotoNegativo.setVisibility(View.GONE);
                mVotoPositivo.setVisibility(View.GONE);
                mConfirmaVotoNegativo.setVisibility(View.VISIBLE);
                mCancelarVoto.setVisibility(View.VISIBLE);


            }
        });

        mConfirmaVotoNegativo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTareaTimer != null) {
                    mTareaTimer.cancel(true);

                }


                Animation hideConfirmacion =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_boton_votacion);

                Animation animacionShowVotoSeleccionado =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.cartelvotosemitido);



                mConfirmaVotoNegativo.startAnimation(hideConfirmacion);


                mVotoSeleccionado.setText(getText(R.string.texto_Voto_Negativo));
//                mVotoSeleccionado.setBackgroundColor((getResources().getColor(R.color.colorVotoNegativo)));
//                mVotoSeleccionado.getBackground().setColorFilter(getResources().getColor( R.color.colorVotoNegativo), PorterDuff.Mode.MULTIPLY);
                mVotoSeleccionado.setBackground((getResources().getDrawable(R.drawable.boton_confirma_voto_negativo)));
                mVotoSeleccionado.startAnimation(animacionShowVotoSeleccionado);
                mVotoSeleccionado.setVisibility(View.VISIBLE);

                votar(VOTO_NEGATIVO);

                mTextTimerVotacion.setVisibility(View.GONE);
                pbarProgreso.setVisibility(View.GONE);
                mConfirmaVotoNegativo.setVisibility(View.GONE);
                mCancelarVoto.setVisibility(View.GONE);


            }
        });

mCancelarVoto.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        mCancelarVoto.setVisibility(View.GONE);
        mConfirmaVotoPositivo.setVisibility(View.GONE);
        mConfirmaVotoNegativo.setVisibility(View.GONE);
        mVotoPositivo.setVisibility(View.VISIBLE);
        mVotoNegativo.setVisibility(View.VISIBLE);
        mVotoSeleccionado.setVisibility(View.GONE);
    }
});


        onConnectionResult = new ConnextionBD.OnConnectionResult() {
            @Override
            public void onResultSuccess(final Connection connection) {
                Log.d(LOG_TAG, " onResult Success task " + connection.toString());
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (connection != null) {
                            conn = connection;
                            mIconoBaseConectada.setVisibility(View.VISIBLE);
                            mIconoBaseDesonectada.setVisibility(View.GONE);
                            leerParametros();

                        }

                    }
                });


            }

            @Override
            public void onResultFail(final String errorMessage) {
                Log.d(LOG_TAG, " onResult Fail task " + errorMessage);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(LOG_TAG, " onResult Fail runable" + errorMessage);
                        Toast.makeText(getApplicationContext(), "Error:" + errorMessage, Toast.LENGTH_LONG).show();
                        mIconoBaseConectada.setVisibility(View.GONE);
                        mIconoBaseDesonectada.setVisibility(View.VISIBLE);
                    }
                });


            }
        };
        mMac = getMacAddr();
    }



    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "task onStart()");
        mInspectorBanderas = new InspectorBanderas();
//        mInspectorBanderas.execute();
        if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
            mInspectorBanderas.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mInspectorBanderas.execute();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "task onResume ");

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
            if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                connextionBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                connextionBD.execute();
            }


        } else {
            Log.d(LOG_TAG, " onResume conn existe");
        }
        super.onResume();
    }


    @Override
    protected void onPostResume() {
        Log.d(LOG_TAG, "task onPostResume()");
        ingresaConcejalEnSesion();
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, " task onpause() ");
        if (conn != null) {
            Log.d(LOG_TAG, "task onpause() conn " + conn.toString());
            sacarConcejalEnSesion();
        } else {
            Log.d(LOG_TAG, "task onpause() conn null");
        }
//        if (conn != null) {
//            try {
//                conn.close();
//                conn = null;
//                Log.d(LOG_TAG, " onpause() Close connection");
//            } catch (SQLException e) {
//                Log.d(LOG_TAG, " onpause() Close exception" + e.getMessage());
//
//            }
//        }
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(LOG_TAG, " onSystemUiVisibilityChange  onWindowFocusChanged" );
        decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions =     (
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                 View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION   |
                 View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                 View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | // hide nav bar
                 View.SYSTEM_UI_FLAG_FULLSCREEN |// hide status bar
                 View.SYSTEM_UI_FLAG_IMMERSIVE);

//        int uiOptions = (  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

//        int uiOptions =  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions );
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        Log.d(LOG_TAG, " onSystemUiVisibilityChange " );
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        // adjustments to your UI, such as showing the action bar or
                        // other navigational controls.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // TODO: The system bars are visible. Make any desired
                            Log.d(LOG_TAG, " onSystemUiVisibilityChange View.SYSTEM_UI_FLAG_FULLSCREE" );

// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
                            int uiOptions1 = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//                                    | View.SYSTEM_UI_FLAG_LOW_PROFILE;



                            decorView.setSystemUiVisibility(uiOptions1);

                        } else {
                            Log.d(LOG_TAG, " onSystemUiVisibilityChange View.SYSTEM_UI_FLAG_FULLSCREE NOT" );
                            // TODO: The system bars are NOT visible. Make any desired

                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.

                        }
                    }
                });

        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "task onStop()");
        if (conn != null) {
            Log.d(LOG_TAG, "task onStop conn " + conn.toString());
            sacarConcejalEnSesion();
        } else {
            Log.d(LOG_TAG, "task onStop conn null");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        Log.d(LOG_TAG, "task onDestroy");
        if (mInspectorBanderas != null) {
            mInspectorBanderas.cancel(true);
        }
        if (mTareaTimer != null) {
            mTareaTimer.cancel(true);
        }
        if (conn != null) {
            Log.d(LOG_TAG, "task onDestroy con " + conn.toString());
            sacarConcejalEnSesion();
        } else {
            Log.d(LOG_TAG, "task onDestroy conn null");
        }
        super.onDestroy();
    }

    private void timer() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "task onActivityResult");
        Log.d(LOG_TAG, "task requestCode: "+requestCode);
        Log.d(LOG_TAG, "task resultCode: " +resultCode);
        Log.d(LOG_TAG, "task data: " +data.toString());

        switch(resultCode)
        {

//            case RESULT_CLOSE_ALL:
//                setResult(RESULT_CLOSE_ALL);
//                finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        ActionBar actionBar = getActionBar();
//        if (actionBar != null) {
//            actionBar.setHomeButtonEnabled(false); // disable the button
//            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
//            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
//        }
//        return super.onCreateOptionsMenu(menu);
//
//    }



    private class TimerTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            Log.d(LOG_TAG, "TimerTask doInBackground");
            Boolean grabaAbstencion = true;
            int tiempoTotal = params[0].intValue();
            for (int i = tiempoTotal; i >= 0; i--) {

                Log.d(LOG_TAG, "TimerTask i: " + i);
                timer();

                publishProgress(i);

                if (isCancelled()) {
                    // este valor pasa a onPostExecute y no graba el vota por abstenciòn
                    grabaAbstencion = false;
                    break;
                }
            }

            return grabaAbstencion;//Cuando termina la Tarea se graba la abstención.
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            int progreso = values[0].intValue();
            Log.d(LOG_TAG, "MiTareaAsincrona onProgressUpdate: " + progreso);
            mTextTimerVotacion.setText(String.valueOf(progreso));
            pbarProgreso.setProgress(progreso);
        }

        @Override
        protected void onPreExecute() {
            Log.d(LOG_TAG, "MiTareaAsincrona onPreExecute(): ");
            pbarProgreso.setVisibility(View.VISIBLE);
            mTextTimerVotacion.setVisibility(View.VISIBLE);
            pbarProgreso.setMax(mTiempoVotacion);
            pbarProgreso.setProgress(mTiempoVotacion);
            mTextTimerVotacion.setText(String.valueOf(mTiempoVotacion));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(LOG_TAG, "MiTareaAsincrona onPostExecute: ");
            pbarProgreso.setVisibility(View.GONE);
            mTextTimerVotacion.setVisibility(View.GONE);

                Toast.makeText(VotacionActivity.this, "Finalizo el tiempo para votar!", Toast.LENGTH_SHORT).show();
            if (result) {
                votar(VOTO_ABSTENCION);
                mCancelarVoto.setVisibility(View.GONE);
                mConfirmaVotoPositivo.setVisibility(View.GONE);
                mConfirmaVotoNegativo.setVisibility(View.GONE);
                mVotoPositivo.setVisibility(View.GONE);
                mVotoNegativo.setVisibility(View.GONE);

                if(mTratamientoAbstencion==2) {
                    mVotoSeleccionado.setBackgroundColor((getResources().getColor(R.color.color_ProgressTimer_border)));
                    mVotoSeleccionado.setText(getString(R.string.texto_Voto_Abstencion));
                } else if(mTratamientoAbstencion==1) {
                    mVotoSeleccionado.setText(getText(R.string.texto_Voto_Positivo));
//                    mVotoSeleccionado.setBackgroundColor((getResources().getColor(R.color.colorVotoPositivo)));
                    mVotoSeleccionado.setBackground((getResources().getDrawable(R.drawable.boton_confirma_voto_positivo)));
                } else if(mTratamientoAbstencion==0) {
                    mVotoSeleccionado.setText(getText(R.string.texto_Voto_Negativo));
                    mVotoSeleccionado.setBackground((getResources().getDrawable(R.drawable.boton_confirma_voto_negativo)));
//                    mVotoSeleccionado.setBackgroundColor((getResources().getColor(R.color.colorVotoNegativo)));
                }
                mVotoSeleccionado.setVisibility(View.VISIBLE);
            }
            onCancelled();
        }

        @Override
        protected void onCancelled() {
            Log.d(LOG_TAG, "MiTareaAsincrona onCancelled(): ");
            pbarProgreso.setVisibility(View.GONE);
            mTextTimerVotacion.setVisibility(View.GONE);
            Toast.makeText(VotacionActivity.this, "Tiempo Voto concluido!", Toast.LENGTH_SHORT).show();
            try {
                mTareaTimer=null;

            } catch (Throwable throwable) {
                Log.d(LOG_TAG, "MiTareaAsincrona error al anular Tarea: ");
                throwable.printStackTrace();
            }
        }
    }


    private class InspectorBanderas extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {


            while (true) {

                leerBanderas();
                Log.d(LOG_TAG, "InspectorBanderas:");

                if (isCancelled())
                    break;
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            mVotoPositivo.setVisibility(View.GONE);
            mVotoNegativo.setVisibility(View.GONE);

            if (result){
//                Toast.makeText(VotacionActivity.this, "Tarea Inspeccion finalizada!", Toast.LENGTH_SHORT).show();
        }
        }

        @Override
        protected void onCancelled() {
//            Toast.makeText(VotacionActivity.this, "Tarea Inspeccion cancelada!", Toast.LENGTH_SHORT).show();
        }
    }

    private void leerBanderas() {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        Log.d(LOG_TAG, "leerBandera:");

        if (conn != null) {

            Log.d(LOG_TAG, "leerBandera: conn-- " + conn.toString());

            String stsql = "Select * FROM age_Sesiones where  Macaddresses='" + mMac + "' and Habilitado = 1 ";

            QueryBD.QueryData queryData = new QueryBD.QueryData(conn, stsql);
            QueryBD queryBD = new QueryBD();
            QueryBD.OnQueryResult onQueryResult;
            onQueryResult = new QueryBD.OnQueryResult() {
                @Override
                public void onResultSuccess(final ResultSet rs) {
                    Log.d(LOG_TAG, " leerBandera success: " + rs);
                    int row = 0;
                    String titulo = "";
                    String texto = "";
                    Boolean funcion = false;
                    Boolean iniciaTexto = false;
                    Boolean iniciaVoto = false;
                    int tiempoVotacion= 0;
                    Boolean limpiar = false;
                    Boolean apagar = false;
                    try {
                        while (rs.next()) {
                            row++;
                            titulo = rs.getString("Titulo");
                            texto = rs.getString("Texto");
                            iniciaTexto = rs.getBoolean("IniciaTexto");
                            iniciaVoto = rs.getBoolean("InicioVoto");
                            tiempoVotacion = rs.getInt("TiempoVotacion");
                            funcion = rs.getBoolean("Funcion");
                            limpiar = rs.getBoolean("Limpiar");
                            apagar = rs.getBoolean("Apagar");
                            Log.d(LOG_TAG, " leerBandera success: " + titulo + " - " + texto + " - " + iniciaTexto + " - " + iniciaVoto);
                        }
                        final int finalRow = row;
                        final String finalTitulo = titulo;
                        final String finalTexto = texto;
                        final Boolean finalFuncion = funcion;
                        final Boolean finalIniciaTexto = iniciaTexto;
                        final Boolean finalIniciaVoto = iniciaVoto;
                        final int finaltiempoVotacion =  tiempoVotacion;
                        final Boolean finalLimpiar =  limpiar;
                        final Boolean finalApagar =  apagar;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (finalRow == 1) {
                                    Log.d(LOG_TAG, " abreviacion mAbreviacion"+ mAbreviacion);
                                    if(finalFuncion){
                                            Log.d(LOG_TAG, " abreviacion funcion verdad: ");
                                            mTextConcejal.setText(getString(R.string.presidente));
                                    }else{
                                        Log.d(LOG_TAG, "abreviacion funcion false: ");
                                        mTextConcejal.setText(mAbreviacion);
                                        }


                                    if(finalIniciaTexto && !mTextosInicializados){
                                        mTextosInicializados=true;// Se ponee en true luego de leer los primero textos
                                    }

                                    if (finalIniciaTexto || mTextosInicializados ) { //ingres cuando iniciaTexto es true
                                        mNestedSecrollViewVotacion.setAlpha(1f);
                                        collapsingToolbarLayout.setTitle(finalTitulo);
                                        mTextParrafo.setText(Html.fromHtml(finalTexto));
                                    } else {
                                        collapsingToolbarLayout.setTitle(mEsperaTitulo);
                                        mTextParrafo.setText(mEsperaTexto);
                                    }
                                    if(finalIniciaTexto){
                                        indicarTextosLeidos();
                                    }
                                    if (finalIniciaVoto) {
                                        mNestedSecrollViewVotacion.scrollTo(0, 0);
                                        mNestedSecrollViewVotacion.stopNestedScroll();
                                        mNestedSecrollViewVotacion.setAlpha(0.5f);


                                        mTiempoVotacion=finaltiempoVotacion;
                                        if (mTareaTimer == null && !mActualizandoDatosTimerVoto) {
                                            Log.d(LOG_TAG, "finalIniciaVoto:  start");
                                            Animation animacionshowBoton =
                                                    AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_boton_votacion);


                                            mVotoPositivo.startAnimation(animacionshowBoton);
                                            mVotoNegativo.startAnimation(animacionshowBoton);
                                            mVotoPositivo.setVisibility(View.VISIBLE);
                                            mVotoNegativo.setVisibility(View.VISIBLE);

                                            mTareaTimer = new TimerTask();
                                            mtiempoInicioVotacion=System.currentTimeMillis();

                                            if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                                                mTareaTimer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mTiempoVotacion);
                                            } else {
                                                mTareaTimer.execute(mTiempoVotacion);
                                            }

                                        } else {
                                            Log.d(LOG_TAG, "finalIniciaVoto:  no nula");
                                        }
                                    }
                                    if(finalApagar){
                                        mApagar=finalApagar;
                                        sacarConcejalEnSesion();
                                    } if(finalLimpiar){
                                        limpiar();
                                    }
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
            if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                queryBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, queryData);
            } else {
                queryBD.execute(queryData);
            }


            Log.d(LOG_TAG, " leerBandera: qwuery");


        } else {
            Log.d(LOG_TAG, "leerBandera: conn nullo-- ");
        }

    }


    private void leerParametros() {



        Log.d(LOG_TAG, "leerParametros:");

        if (conn != null) {

            Log.d(LOG_TAG, "leerParametros: conn-- " + conn.toString());

            String stsql = "Select * FROM gen_Parametros where  NumParametro= 1";

            QueryBD.QueryData queryData = new QueryBD.QueryData(conn, stsql);
            QueryBD queryBD = new QueryBD();
            QueryBD.OnQueryResult onQueryResult;
            onQueryResult = new QueryBD.OnQueryResult() {
                @Override
                public void onResultSuccess(final ResultSet rs) {
                    Log.d(LOG_TAG, " leerParametros success: " + rs);
                    int row = 0;

                    int noVoto= 0;
                    String titulo="";
                    String texto="";

                    try {
                        while (rs.next()) {
                            row++;

                            noVoto = rs.getInt("Sesiones_SinVoto");
                           titulo=rs.getString("Sesiones_Espera_Titulo");
                           texto=rs.getString("Sesiones_Espera_Texto");

                            Log.d(LOG_TAG, " leerParametros: " + noVoto);
                        }
                        final int finalRow = row;
                        final int finaltiempoVotacion =  noVoto;
                        final String finalTitulo=titulo;
                        final String finalTexto=texto;

                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (finalRow == 1) {

                                mTratamientoAbstencion =finaltiempoVotacion;
                                mEsperaTitulo=finalTitulo;
                                mEsperaTexto=finalTexto;
                            }
                        }});

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onResultFail(String errorMessage) {
                    Log.d(LOG_TAG, " leerParametros onResultFail: " + errorMessage);
                }
            };
            queryBD.setOnResultListener(onQueryResult);
            if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                queryBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, queryData);
            } else {
                queryBD.execute(queryData);
            }


            Log.d(LOG_TAG, " leerParametros: qwuery");


        } else {
            Log.d(LOG_TAG, "leerParametros: conn nullo-- ");
        }

    }


    // pone en false el campo IniciaTexto para indicar que ya se actualizaron los titulos y textos
    // cuando esta bander se ponga en un nuevamente se llera otra vez.
    public void indicarTextosLeidos() {
        mActualizandoDatosTextos=true;
        Log.d(LOG_TAG, " indicarTextosLeidos:");
        if (conn != null) {
            Log.d(LOG_TAG, " indicarTextosLeidos-connection:" + conn.toString());
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("UPDATE age_Sesiones "
                        + " SET IniciaTexto= ?"
                        + "WHERE  Macaddresses='" + mMac + "'"
                        +  "and Habilitado = ? "
                );

                pst.setBoolean(1, false);
                pst.setBoolean(2, true);

                UpdateBD updateBD = new UpdateBD();
                UpdateBD.OnUpdateResult onUpDateResult;
                onUpDateResult = new UpdateBD.OnUpdateResult() {
                    @Override
                    public void onResultSuccess(int cantidadLineasModificadas) {
                        Log.d(LOG_TAG, "  indicarTextosLeidos: success: " + cantidadLineasModificadas);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mActualizandoDatosTextos=false;
//                                Toast.makeText(getApplicationContext(), " indicarTextosLeidos: con Éxito", Toast.LENGTH_LONG).show();

                            }
                        });

                    }

                    @Override
                    public void onResultFail(String errorMessage) {
                        Log.d(LOG_TAG, "  indicarTextosLeidos: onResultFail: " + errorMessage);
                    }
                };
                updateBD.setOnResultListener(onUpDateResult);
                ;
                if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                    updateBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pst);
                } else {
                    updateBD.execute(pst);
                }

                Log.d(LOG_TAG, "indicarTextosLeidos  pst.executeUpdate:");
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } else {
            Log.d(LOG_TAG, " indicarTextosLeidos error sin Conexion ");

        }

    }

    public void indicarLimpiarLeido() {

        Log.d(LOG_TAG, " indicarLimpiarLeido:");
        if (conn != null) {
            Log.d(LOG_TAG, " indicarLimpiarLeido-connection:" + conn.toString());
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("UPDATE age_Sesiones "
                        + " SET Limpiar= ?"
                        + "WHERE  Macaddresses='" + mMac + "'");

                pst.setBoolean(1, false);

                UpdateBD updateBD = new UpdateBD();
                UpdateBD.OnUpdateResult onUpDateResult;
                onUpDateResult = new UpdateBD.OnUpdateResult() {
                    @Override
                    public void onResultSuccess(int cantidadLineasModificadas) {
                        Log.d(LOG_TAG, "  indicarLimpiarLeido: success: " + cantidadLineasModificadas);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mActualizandoDatosTextos=false;
//                                Toast.makeText(getApplicationContext(), " indicarLimpiarLeido: con Éxito", Toast.LENGTH_LONG).show();

                            }
                        });

                    }

                    @Override
                    public void onResultFail(String errorMessage) {
                        Log.d(LOG_TAG, "  indicarLimpiarLeido: onResultFail: " + errorMessage);
                    }
                };
                updateBD.setOnResultListener(onUpDateResult);
                ;
                if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                    updateBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pst);
                } else {
                    updateBD.execute(pst);
                }

                Log.d(LOG_TAG, "indicarLimpiarLeido  pst.executeUpdate:");
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } else {
            Log.d(LOG_TAG, "indicarLimpiarLeido error sin Conexion ");

        }

    }

    public void votar(int voto) {
        mActualizandoDatosTimerVoto=true;
        Log.d(LOG_TAG, " votar:");
        if (conn != null) {
            Log.d(LOG_TAG, " votar-connection:" + conn.toString());
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("UPDATE age_Sesiones "
                        + " SET InicioVoto= ?, ResultadoVoto=? , TiempoInicio=?, TiempoFin=?"
                        + "WHERE  Macaddresses='" + mMac + "' and Habilitado= ? ");

                pst.setBoolean(1, false);
                pst.setInt(2, voto);
//                Timestamp t = new Timestamp(System.currentTimeMillis());
//                Log.d(LOG_TAG, " Timestamp:" + System.currentTimeMillis());
                pst.setString(3,Long.toString( mtiempoInicioVotacion));//tiempoInicio
                pst.setString(4,Long.toString( System.currentTimeMillis()));//tiempoFin
                pst.setBoolean(5,true);// Habilitado en Verdadero

                UpdateBD updateBD = new UpdateBD();
                UpdateBD.OnUpdateResult onUpDateResult;
                onUpDateResult = new UpdateBD.OnUpdateResult() {
                    @Override
                    public void onResultSuccess(int cantidadLineasModificadas) {
                        Log.d(LOG_TAG, "  votar: success: " + cantidadLineasModificadas);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mActualizandoDatosTimerVoto=false;

//                                Toast.makeText(getApplicationContext(), " votar: con Éxito", Toast.LENGTH_LONG).show();

                            }
                        });

                    }

                    @Override
                    public void onResultFail(String errorMessage) {
                        Log.d(LOG_TAG, "  votar: onResultFail: " + errorMessage);
                    }
                };
                updateBD.setOnResultListener(onUpDateResult);
                ;
                if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                    updateBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pst);
                } else {
                    updateBD.execute(pst);
                }

                Log.d(LOG_TAG, "votar  pst.executeUpdate:");
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } else {
            Log.d(LOG_TAG, " votar error sin Conexion ");

        }

    }

    public void sacarConcejalEnSesionbkp() {

        Log.d(LOG_TAG, " sacarConcejalEnSesion:");
        if (conn != null) {
            Log.d(LOG_TAG, " sacarConcejalEnSesion:" + conn.toString());
            try {
                PreparedStatement pst = conn.prepareStatement("delete age_Sesiones"
                        + " Where Macaddresses=?");


                Log.d(LOG_TAG, " sacarConcejalEnSesion connection:" + conn.toString());

                pst.setString(1, getMacAddr());

                InsertBD insertBD = new InsertBD();
                InsertBD.OnInsertResult onInsertResult;
                onInsertResult = new InsertBD.OnInsertResult() {
                    @Override
                    public void onResultSuccess(String msg) {
                        Log.d(LOG_TAG, " sacarConcejalEnSesion success: " + msg);

                        if (conn != null) {
                            try {
                                conn.close();
                                conn = null;
                                Log.d(LOG_TAG, " sacarConcejalEnSesion) Close connection");
                            } catch (SQLException e) {
                                Log.d(LOG_TAG, " sacarConcejalEnSesion Close exception" + e.getMessage());

                            }
                        }
                        if(mApagar){
                            finish();
                        }

                    }

                    @Override
                    public void onResultFail(String errorMessage) {
                        Log.d(LOG_TAG, " ingresaConcejalEnSesion onResultFail: " + errorMessage);
                    }
                };
                insertBD.setOnResultListener(onInsertResult);
//                insertBD.execute(pst);xx
                if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                    insertBD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pst);
                } else {
                    insertBD.execute(pst);
                }



            } catch (SQLException e) {
                Log.d(LOG_TAG, " ingresaConcejalEnSesion error " + e.toString());
                Toast.makeText(getApplicationContext(), "Error:" + e.getMessage().toString(), Toast.LENGTH_LONG).show();

            }

        } else {
            Toast.makeText(getApplicationContext(), "Error: No Hay conextion a la base", Toast.LENGTH_SHORT).show();
        }
    }

    public void ingresaConcejalEnSesion() {

//        Log.d(LOG_TAG, " ingresaConcejalEnSesion:" + conn.toString());
        if (conn != null) {
            Log.d(LOG_TAG, " ingresaConcejalEnSesion-connection:" + conn.toString());
            try {
                PreparedStatement pst = conn.prepareStatement("update age_Sesiones"
                        + " SET Estado = ? "
//                        + "  Habilitado = ? "
                        + " WHERE  Macaddresses = ? ");





                pst.setBoolean(1, true);//Titulo
//                pst.setString(2, "Texto update" );
                pst.setString(2, getMacAddr());//iniciaVoto
                Log.d(LOG_TAG, " ingresaConcejalEnSesion-connection update:");

                UpdateBD updateBD = new UpdateBD();
                UpdateBD.OnUpdateResult onUpDateResult;

                onUpDateResult = new UpdateBD.OnUpdateResult() {
                    @Override
                    public void onResultSuccess(int cantidadLineasModificadas) {
                        Log.d(LOG_TAG, "  ingresaConcejalEnSesion: success: " + cantidadLineasModificadas);
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//
//                                Toast.makeText(getApplicationContext(), "ingresaConcejalEnSesion correctamente", Toast.LENGTH_LONG).show();
//                                Intent intent = new Intent(getApplication(), VotacionActivity.class);
//                                intent.putExtra("concejal",mConcejalAsignado.getText().toString());
//
//                                startActivity(intent);
//
//                            }
//                        });

                    }

                    @Override
                    public void onResultFail(final String errorMessage) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), " ingresaConcejalEnSesion: " + errorMessage, Toast.LENGTH_LONG).show();
                                Log.d(LOG_TAG, "  ingresaConcejalEnSesion: onResultFail: " + errorMessage);

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
                Log.d(LOG_TAG, " ingresaConcejalEnSesion  pst.executeUpdate");


            } catch (SQLException e) {
                Log.d(LOG_TAG, " ingresaConcejalEnSesion  SQLException" + e.getMessage());
                e.printStackTrace();
            }

        } else {Log.d(LOG_TAG, " ingresaConcejalEnSesion  catch  conn==null");}


    }



    public void sacarConcejalEnSesion() {

        Log.d(LOG_TAG, " sacarConcejalEnSesion1:");
        if (conn != null) {


                Log.d(LOG_TAG, " sacarConcejalEnSesion-connection:" + conn.toString());
                try {
                    PreparedStatement pst = conn.prepareStatement("update age_Sesiones"
                            + " SET Estado = ? , "
                            + "  Apagar = ? , "
                            + "  Habilitado = ? "
                            + " WHERE  Macaddresses = ? ");





                    pst.setBoolean(1, false);//Estado
                    pst.setBoolean(2, false);//Apagar
                    pst.setBoolean(3, false);//Habilitado

                    pst.setString(4, getMacAddr());//iniciaVoto
                    Log.d(LOG_TAG, " sacarConcejalEnSesion-connection update:");

                    UpdateBD updateBD = new UpdateBD();
                    UpdateBD.OnUpdateResult onUpDateResult;

                    onUpDateResult = new UpdateBD.OnUpdateResult() {
                        @Override
                        public void onResultSuccess(int cantidadLineasModificadas) {
                            Log.d(LOG_TAG, "  sacarConcejalEnSesion: onResultSuccess: " + cantidadLineasModificadas);

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    finish();

                                }
                            });

                        }

                        @Override
                        public void onResultFail(final String errorMessage) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), " sacarConcejalEnSesion: " + errorMessage, Toast.LENGTH_LONG).show();
                                    Log.d(LOG_TAG, "  sacarConcejalEnSesion: onResultFail: " + errorMessage);

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
                    Log.d(LOG_TAG, " sacarConcejalEnSesion  pst.executeUpdate");


                } catch (SQLException e) {
                    Log.d(LOG_TAG, " sacarConcejalEnSesion  SQLException" + e.getMessage());
                    e.printStackTrace();
                }


            } else {
            Toast.makeText(getApplicationContext(), "Error: No Hay conextion a la base", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(LOG_TAG, "onSupportNavigateUp()");
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed()");
//        if (mActivadoCorrecto) {
//            super.onBackPressed();
//        }
    }

    public void limpiar(){
        mVotoSeleccionado.setText("");
        mVotoSeleccionado.setVisibility(View.GONE);
        collapsingToolbarLayout.setTitle("");
        mTextParrafo.setText("");
        indicarLimpiarLeido();
    }

}
