package com.ncodata.votar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ncodata.votar.sql.ConnextionBD;
import com.ncodata.votar.sql.InsertBD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.ncodata.votar.utils.SporteConf.getMacAddr;

public class VotacionActivity extends AppCompatActivity  {
    public static final String LOG_TAG = "VotacionActivity";

    Connection conn = null;//Conexion a la base de datos. Al entrar a la actividad nos conectamos y al salir nos desconectamos.
    ConnextionBD connextionBD; //Tarea que se ejecuta en segundo plano para conseguir una conexion a la base
    ConnextionBD.OnConnectionResult onConnectionResult;
    private ImageView mIconoBaseConectada;
    private ImageView mIconoBaseDesonectada;

    public static final int VOTO_NEGATIVO=0;
    public static final int VOTO_POSITIVO=1;
    public static final int VOTO_ABSTENCION=2;

    private ProgressBar pbarProgreso;
    private MiTareaAsincrona mTarea1;
    private InspectorBanderas mInspectorBanderas;
    private TextView mTextTimerVotacion;
    private TextView mTextParrafo;
    private TextView mVotoSeleccionado;
    private  Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton mFabPositivo;
    private FloatingActionButton mFabNegativo;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_votacion);
         toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        toolbar.setTitle("titulo");
        toolbar.setSubtitle("sub t");
        setSupportActionBar(toolbar);

        mIconoBaseConectada = (ImageView) findViewById(R.id.inconoBaseConectada);
        mIconoBaseDesonectada = (ImageView) findViewById(R.id.inconoBaseDesonectada);

        mFabPositivo = (FloatingActionButton) findViewById(R.id.fabPositivo);
        mFabNegativo = (FloatingActionButton) findViewById(R.id.fabNegativo);
        mFabPositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTarea1 !=null) {
                    mTarea1.cancel(true);

                }
//                Snackbar.make(view, "Voto Positivo", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Animation animation1 =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.votoseleccionado);
                Animation animation2 =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.votonoseleccionado);

                Animation animation3 =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.cartelvotosemitido);
                mFabPositivo.startAnimation(animation1);

                mFabNegativo.startAnimation(animation2);
                mVotoSeleccionado.setText("VOTO POSITIVO");
                mVotoSeleccionado.setBackgroundColor((getResources().getColor(R.color.colorVotoPositivo) ));
                mVotoSeleccionado.startAnimation(animation3);
                mFabPositivo.setVisibility(View.GONE);
                mFabNegativo.setVisibility(View.GONE);
                votar(VOTO_POSITIVO);
            }
        });

        mFabNegativo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTarea1 !=null) {
                    mTarea1.cancel(true);

                }
//                Snackbar.make(view, "Voto Negativo", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                Animation animation1 =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.votoseleccionado);
                Animation animation2 =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.votonoseleccionado);

                Animation animation3 =
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.cartelvotosemitido);
                mFabPositivo.startAnimation(animation2);

                mFabNegativo.startAnimation(animation1);

                mVotoSeleccionado.setText("VOTO NEGATIVO");
                mVotoSeleccionado.setBackgroundColor((getResources().getColor(R.color.colorVotoNegativo) ));
                mVotoSeleccionado.startAnimation(animation3);
                votar(VOTO_NEGATIVO);
                mFabPositivo.setVisibility(View.GONE);
                mFabNegativo.setVisibility(View.GONE);
                sacarConcejalEnSesion();

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFabPositivo.setVisibility(View.GONE);
        mFabNegativo.setVisibility(View.GONE);


        pbarProgreso = (ProgressBar)findViewById(R.id.pbarProgreso);
        mTextTimerVotacion = (TextView)findViewById(R.id.textView);
        pbarProgreso.setVisibility(View.GONE);
        mTextTimerVotacion.setVisibility(View.GONE);

        mTextParrafo = (TextView)findViewById(R.id.textoParrafo);
        mVotoSeleccionado = (TextView)findViewById(R.id.votoSeleccionado);
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




    @Override
    protected void onPostResume() {
        Log.d(LOG_TAG, "task onPostResume()");
        super.onPostResume();
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "task onStart()");
        mInspectorBanderas = new InspectorBanderas();
        mInspectorBanderas.execute();
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "task onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        Log.d(LOG_TAG, "task onDestroy");
        if(mInspectorBanderas!=null){
            mInspectorBanderas.cancel(true);
        }
        if(mTarea1!=null){
            mTarea1.cancel(true);
        }
        super.onDestroy();
    }

    private void tareaLarga()
    {
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}
    }


    private class MiTareaAsincrona extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(LOG_TAG, "MiTareaAsincrona doInBackground");
            for(int i=9; i>=0; i--) {

                Log.d(LOG_TAG, "MiTareaAsincrona i: "+i);
                tareaLarga();

                publishProgress(i);

                if(isCancelled()){
                    return false; // este valor pasa a onPostExecute

                }
            }
//
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            int progreso = values[0].intValue();
            Log.d(LOG_TAG, "MiTareaAsincrona onProgressUpdate: "+progreso);
            mTextTimerVotacion.setText(String.valueOf(progreso));
            pbarProgreso.setProgress(progreso*10);
        }

        @Override
        protected void onPreExecute() {
            Log.d(LOG_TAG, "MiTareaAsincrona onPreExecute(): ");
            pbarProgreso.setVisibility(View.VISIBLE);
            mTextTimerVotacion.setVisibility(View.VISIBLE);
            pbarProgreso.setMax(100);
            pbarProgreso.setProgress(100);
            mTextTimerVotacion.setText(String.valueOf(10));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(LOG_TAG, "MiTareaAsincrona onPostExecute: ");
            pbarProgreso.setVisibility(View.GONE);
            mTextTimerVotacion.setVisibility(View.GONE);
            if(result)
                Toast.makeText(VotacionActivity.this, "Finalizo el tiempo para votar!", Toast.LENGTH_SHORT).show();
                votar(VOTO_ABSTENCION);
        }

        @Override
        protected void onCancelled() {
            Log.d(LOG_TAG, "MiTareaAsincrona onCancelled(): ");
            pbarProgreso.setVisibility(View.GONE);
            mTextTimerVotacion.setVisibility(View.GONE);
            Toast.makeText(VotacionActivity.this, "Tiempo Voto cancelado!", Toast.LENGTH_SHORT).show();
        }
    }



    private class InspectorBanderas extends AsyncTask<Void, Datos, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Datos datos =new Datos();
            for(int i=1900; i>=0; i--) {
                ResultSet r =leerBanderas();
                Log.d(LOG_TAG, "InspectorBanderas:");



                Boolean banderaVoto=null;
                String titulo=null;
                String texto=null;
                if (r==null){
                    Log.d(LOG_TAG, "InspectorBanderas r: null");
                }
                try {
                    r.next();
                    datos.setBanderaTexto(r.getBoolean("IniciaTexto"));
                    datos.setBanderaVoto(r.getBoolean("InicioVoto"));
                    titulo=r.getString("Titulo");
                    texto=r.getString("Texto");
                    datos.setTitulo(titulo);
                    datos.setTexto(texto);
                    Log.d(LOG_TAG, "InspectorBanderas  bandera Texto: "+ datos.getBanderaTexto());
                    Log.d(LOG_TAG, "InspectorBanderas  texto: "+ texto);

                     if(datos.getBanderaVoto()||datos.getBanderaTexto()) {
                         Log.d(LOG_TAG, "InspectorBanderas  datos: "+ datos.toString());
                         publishProgress(datos);
                     }
//                     datos.reset();




                } catch (SQLException e) {
                    Log.d(LOG_TAG, "InspectorBanderas  catch: ");
                    e.printStackTrace();
                }



                if(isCancelled())
                    break;
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Datos...values) {

            Log.d(LOG_TAG, "InspectorBanderas  datos[0]: "+ values[0].toString());
                if (values[0].getBanderaTexto()) {
                String txt = values[0].getTexto();
                String titulo = values[0].getTitulo();
                Log.d(LOG_TAG, "InspectorBanderas  onProgressUpdate: "+ txt);
                Log.d(LOG_TAG, "InspectorBanderas  titulo: "+titulo );
                collapsingToolbarLayout.setTitle(titulo);
                mTextParrafo.setText(Html.fromHtml(txt));
            } else {
                collapsingToolbarLayout.setTitle("Esperando");
                mTextParrafo.setText("");
            }
            if( values[0].getBanderaVoto()){
                if(mTarea1==null) {
                    Log.d(LOG_TAG, "MiTareaAsincrona:  start" );
                mFabPositivo.setVisibility(View.VISIBLE);
                mFabNegativo.setVisibility(View.VISIBLE);

                    mTarea1 = new MiTareaAsincrona();

                    if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                        mTarea1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mTarea1.execute();
                    }

                }
            }

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mFabPositivo.setVisibility(View.GONE);
            mFabNegativo.setVisibility(View.GONE);

            if(result)
                Toast.makeText(VotacionActivity.this, "Tarea Inspeccion finalizada!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(VotacionActivity.this, "Tarea Inspeccion cancelada!", Toast.LENGTH_SHORT).show();
        }
    }

    private ResultSet leerBanderas()
    {
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}


        Log.d(LOG_TAG, "leerBandera:");
        try {


//            /En el stsql se puede agregar cualquier consulta SQL deseada.
            // tableet NumConcejal=37 and Macaddresses='60:d9:a0:94:f9:5e'
            // moto5   NumConcejal=15 and Macaddresses='80:58:f8:51:db:35'
//            String stsql = "Select * FROM age_Sesiones where NumConcejal=37 and Macaddresses='80:58:f8:51:db:35'";
            String stsql = "Select * FROM age_Sesiones where  Macaddresses='80:58:f8:51:db:35'";
            // String stsql = "SELECT @@VERSION";
            Statement st = conextionBD().createStatement();
            ResultSet rs = st.executeQuery(stsql);
            // rs.next();
            //System.out.println(rs.getString(1));
            Log.d(LOG_TAG, " leerBandera:: qwuery");
//            Log.d(LOG_TAG, " leerDato: ResultSet rs"  +rs.getCursorName());

            if (rs == null) {
                Log.d(LOG_TAG, " leerBandera: rs nulo");
            }
            Log.d(LOG_TAG, " leerBandera:: Resultados");
//            while (rs.next()) {
//
//                Log.d(LOG_TAG, "NumDispositivo: " + rs.getString("NumDispositivo") + "- serie: " + rs.getString("Serie") + " - Macaddresses:  " + rs.getString("Macaddresses"));
//
//            }

            return rs;
//            Toast.makeText(getApplicationContext(), "leerDato", Toast.LENGTH_LONG).show();


        } catch (SQLException e) {
            Log.d(LOG_TAG, " leerBandera: " + e.toString());
            return null;
//            Toast.makeText(getApplicationContext(), "Error:" + e.getMessage().toString(), Toast.LENGTH_LONG).show();

        }


    }

    public Connection conextionBD() {
        Log.d(LOG_TAG, "conextionBD():");
        Connection connection = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            Log.d(LOG_TAG, "conextionBD()-policy:" + policy.toString());
            StrictMode.setThreadPolicy(policy);
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
//             mi pc 192.168.0.34
//             mi pc 192.168.0.17
//             Bulnes 192.168.0.6
            connection = DriverManager.getConnection("jdbc:jtds:sqlserver://192.168.0.6;databaseName=HCD;user=hcd;password=d4k4r");

            Log.d(LOG_TAG, "conextionBD()-connection:" + connection.toString());
        } catch (Exception e) {
            Log.d(LOG_TAG, "conextionBD()-Exception:" + e.getMessage().toString());
//            Toast.makeText(getApplicationContext(), "Error:" + e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
        return connection;
    }

    private  class Datos{

        Boolean  banderaVoto;
        Boolean banderaTexto;
        String titulo;
        String texto;
        int duracionVotacion;



        public Datos() {
            this.banderaVoto = false;
            this.banderaTexto = false;
            this.titulo ="";
            this.texto = "";
            this.duracionVotacion = 1000*10;
        }

        public Datos(Boolean banderaVoto,Boolean banderaTexto,String titulo,String texto,int duracionVotacion) {
            this.banderaVoto = banderaVoto;
            this.banderaTexto = banderaTexto;
            this.titulo =titulo;
            this.texto = texto;
            this.duracionVotacion = 1000*duracionVotacion;
        }

        public void reset(){
            this.banderaVoto = false;
            this.banderaTexto = false;
            this.titulo ="";
            this.texto = "";
            this.duracionVotacion = 1000*10;
        }

        public String toString(){

            String s;
            s= "banderaVoto:"+  this.banderaVoto
            + " BanderaTexto:"+this.banderaTexto
            +" titulo: "+this.titulo
            + " texto "+this.texto
            + " duracionVotacion: "+this.duracionVotacion ;
            return s;
        }


        public Boolean getBanderaTexto() {
            return this.banderaTexto;
        }

        public void setBanderaTexto(Boolean banderaTexto) {
            this.banderaTexto = banderaTexto;
        }

        public Boolean getBanderaVoto() {
            return this.banderaVoto;
        }

        public void setBanderaVoto(Boolean banderaVoto) {
            this.banderaVoto = banderaVoto;
        }

        public String getTitulo() {
            return this.titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getTexto() {
            return this.texto;
        }

        public void setTexto(String texto) {
            this.texto = texto;
        }

        public int getDuracionVotacion() {
            return this.duracionVotacion;
        }

        public void setDuracionVotacion(int duracionVotacion) {
            this.duracionVotacion = duracionVotacion;
        }


    }

    public void votar(int voto) {
        Log.d(LOG_TAG, " votar-connection:");
        try {

            PreparedStatement pst = conextionBD().prepareStatement("UPDATE age_Sesiones "
                    + " SET InicioVoto= ?, ResultadoVoto=?"
                    + "WHERE  NumConcejal=37 and Macaddresses='60:d9:a0:94:f9:5e'"
                    // tableet NumConcejal=37 and Macaddresses='60:d9:a0:94:f9:5e'
                    // moto5   NumConcejal=15 and Macaddresses='80:58:f8:51:db:35'

            );
            Log.d(LOG_TAG, " votar-connection:" + conextionBD().toString());

            pst.setBoolean(1, false);
            pst.setInt(2, voto);

            pst.executeUpdate();
            Log.d(LOG_TAG, "votar  pst.executeUpdate:");

        } catch (SQLException e) {
            Log.d(LOG_TAG, " votar error " + e.toString());


        }

    }

    public void sacarConcejalEnSesion() {

        Log.d(LOG_TAG, " sacarConcejalEnSesion:");
        if (conn != null) {
            Log.d(LOG_TAG, " sacarConcejalEnSesion:" + conn.toString());
            try {
                PreparedStatement pst = conn.prepareStatement("delete age_Sesiones"
                        + " Where Macaddresses=?" );


                Log.d(LOG_TAG, " sacarConcejalEnSesion connection:" + conn.toString());

                pst.setString(1, getMacAddr());

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



}
