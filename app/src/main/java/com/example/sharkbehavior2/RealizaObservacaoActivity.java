package com.example.sharkbehavior2;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RealizaObservacaoActivity extends AppCompatActivity {
    private TextView valorObservadoresTextView;
    private int numeroObservadores;
    private SeekBar observadoresSeekBar;
    private int estado; //parado
    private int local;
    private String comentarios;
    private RadioGroup radioGroupComportamento;
    RadioGroup radioGroupLocal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realiza_observacao);

        valorObservadoresTextView = (TextView) findViewById(R.id.valorObservadoresTextView);
        observadoresSeekBar = (SeekBar) findViewById(R.id.observadoresSeekBar);
        observadoresSeekBar.setOnSeekBarChangeListener(seekBarListener);

    }

    private final SeekBar.OnSeekBarChangeListener seekBarListener =
            new SeekBar.OnSeekBarChangeListener() {
                // update percent, then call calculate
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    valorObservadoresTextView.setText(String.valueOf(progress));
                    numeroObservadores = progress;

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };


    public void cadastrarObservacaoClick(View v) {

        if(validacao()==true){

           final CharSequence[] items = {"Sim", "Não"};
           String comportamento;
           String sLocal;
            if(local==0){
              sLocal = "Local: Borda";
            }else if(local ==1){
                sLocal = "Local: Meio do tanque";
            }else{
                sLocal = "Local: Pedras";
            }

            if(estado ==0){
                comportamento="Comportamento: Parado";
            }else {
                comportamento = "Comportamento: Nadando";
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(RealizaObservacaoActivity.this);
            builder.setTitle("Confirma o cadastro das seguintes informações?");
            builder.setMessage("-Observadores: " + numeroObservadores + "\n-" + sLocal + "\n-" + comportamento +"\n" +
                    "-Comentários: " +comentarios );

            builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog , int id) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true).build();
                    db.setFirestoreSettings(settings);
                    LocalDateTime ldt = LocalDateTime.now();
                    ZonedDateTime zdt = ldt.atZone(ZoneOffset.UTC); //you might use a different zone
                    String iso8601 = zdt.toString();
                    String date = zdt.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    String hora = zdt.format(DateTimeFormatter.ISO_LOCAL_TIME);

                    Map<String, Object> user = new HashMap<>();
                    user.put("usuario", "Flavia");
                    user.put("data", date);
                    user.put("hora", hora);
                    user.put("horario", iso8601);
                    user.put("observadores", numeroObservadores);
                    user.put("local", local);
                    user.put("comportamento", estado);
                    user.put("comentarios", comentarios);

                    db.collection("estudo")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("REALIZAOBSERVACAO", "DocumentSnapshot added with ID: " + documentReference.getId());
                                    //finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Log.w("REALIZAOBSERVACAO", "Error adding document", e);


                                }
                            });
                    finish();



                }
            });
            builder.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    Toast.makeText(RealizaObservacaoActivity.this, "Envio cancelado.", Toast.LENGTH_SHORT).show();


                }
            });

            builder.create();
            builder.show();

        }else{
            Toast.makeText(RealizaObservacaoActivity.this, "Erro ao enviar os dados!", Toast.LENGTH_SHORT).show();

        }



    }

    //metodo de validaçao
    public boolean validacao(){

        boolean status = true;
        //Validação do campo comportamento
        radioGroupComportamento = (RadioGroup) findViewById(R.id.radioGroupComportamento);
        int selectedId = radioGroupComportamento.getCheckedRadioButtonId();

        if (selectedId ==-1){
            TextView textViewComportamento = (TextView) findViewById(R.id.textViewComportamento);
            textViewComportamento.setError("Selecione um Item!");
            textViewComportamento.requestFocus();
            status = false;
        }else if(selectedId == R.id.radioButtonNadando){
            estado = 0;
        }else{
            estado =1;
        }


        // Validação do campo local
        radioGroupLocal = (RadioGroup) findViewById(R.id.radioGroupLocal);
        int selectedIdLocal = radioGroupLocal.getCheckedRadioButtonId();
        if (selectedIdLocal == -1) {
            TextView textViewLocal = (TextView) findViewById(R.id.textViewLocal);
            textViewLocal.setError("Selecione um Item!");
            textViewLocal.requestFocus();
            status = false;

        } else if(selectedIdLocal == R.id.radioButtonBorda) {
            local = 0;//pedras
        }else if(selectedIdLocal == R.id.radioButtonCentro){
            local =1;
        }else{
            local =2;
        }



        //Validação do campo comentário
        TextView comentariosTextview = (TextView) findViewById(R.id.editTextComentario);
        comentarios = comentariosTextview.getText().toString();
            return status;
        }






}
