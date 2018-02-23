package com.ncodata.votar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ncodata.votar.utils.Proyecto;

import static com.ncodata.votar.utils.SporteConf.getMacAddr;

public class ActivarAplicacion extends AppCompatActivity {
    private static final String TAG = "ActivarAplicacion";
    private DatabaseReference myRef;
    FirebaseDatabase database;
    private TextView mActivacionTitulo;
    private TextView mActivacionComentario;
    private EditText mActivacionCodigo;
    private Boolean mActivadoCorrecto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activar_aplicacion);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mActivacionTitulo = (TextView) findViewById(R.id.activacionTitulo);
        mActivacionComentario = (TextView) findViewById(R.id.activacionComentario);
        mActivacionCodigo = (EditText) findViewById(R.id.activacionCodigo);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef = database.getReference("proyecto/" + mActivacionCodigo.getText().toString());
                onRegistrarClicked(myRef);


            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();


        mActivacionCodigo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mActivacionComentario.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void onRegistrarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Proyecto p = mutableData.getValue(Proyecto.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.cantidaddeinstalacionesrealizadas >= p.cantidaddeinstalacionesmax) {
                    return Transaction.abort();
                }

                if (p.macs.containsKey(getMacAddr())) {

                    // Al encontrar la mac, no suma  una nueva Instalacion ni agrega la mac
//                    p.cantidaddeinstalacionesrealizadas = p.cantidaddeinstalacionesrealizadas + 1;
//                    p.macs.put(getMacAddr(), true);
                } else {
                    // Suma una instalacion Realizada y sube una mac
                    p.cantidaddeinstalacionesrealizadas = p.cantidaddeinstalacionesrealizadas + 1;
                    p.macs.put(getMacAddr(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete: " + databaseError);
                Log.d(TAG, "postTransaction:boolean b: " + b);
                Log.d(TAG, "postTransaction:dataSnapshot:" + dataSnapshot);
                Proyecto p = dataSnapshot.getValue(Proyecto.class);

                if (p == null) {
                    mActivacionComentario.setText(getString(R.string.ActivacionCodigoIncorrecto));
                    mActivacionComentario.setVisibility(View.VISIBLE);
                } else {

                    if (!b) {// Cuando es false abortó la transacción esto es cuando la cantidad de instalaciones supero el máximo
                        Log.d(TAG, "abortó la transacción esto es cuando la cantidad de instalaciones supero el máximo:");
                        mActivacionComentario.setText(getString(R.string.ActivacionSuperoElMaximo));
                        mActivacionComentario.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG, "grabar preferencias y retornar:");
                        mActivacionComentario.setText(getString(R.string.ActivacionEquipoActivado));
                        mActivacionComentario.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), getString(R.string.ActivacionEquipoActivado), Toast.LENGTH_LONG).show();
                        SharedPreferences sharedPref = getSharedPreferences("Mis Preferencias", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.preference_registrado), "registroOK");
                        editor.commit();
                        mActivadoCorrecto = true;
                        onBackPressed();
                    }


                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp()");
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
        if (mActivadoCorrecto) {
            super.onBackPressed();
        }
    }
}
