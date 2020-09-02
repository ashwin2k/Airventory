package com.ashwin2k.airventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    AppCompatButton fab;
    Dialog d;
    Context c;
    String TAG="MAA";
    ArrayList a;
    RecyclerView recents;
    NfcAdapter nfcadapter;

    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab=findViewById(R.id.scan_button);
        c=this;
        a=new ArrayList();
        recents=findViewById(R.id.recentlist);
        for(int i=0;i<5;i++)
            a.add(i);
        d=new Dialog(this);
        d.setContentView(R.layout.dialog_det);

        fab.setBackgroundResource(R.drawable.ic_buttonbg);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(c,QRActivity.class),0777);
                //startActivity(new Intent(c,CameraKotlin.class));
            }
        });
        adapter=new ArrayAdapter(this,R.layout.card_recent,a);
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(this);
        MyLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recents.setLayoutManager(MyLayoutManager);
        recents.setAdapter(new RecycleAdapter(a));
        nfcadapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcadapter != null)
            Log.d("NFCTAG", nfcadapter.toString());
    }
    private void handleIntent(Intent intent) {
        String action = intent.getAction();


        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if ("text/plain".equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTaskStuddApp(this, true).execute(tag);
                Log.d("NFC","EdXEX");




            } else {
                Log.d("NFCTASK", "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTaskStuddApp(this, true).execute(tag);
                    Log.d("NFC","EXEX");

                    break;
                }
            }
        }
    }
















    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, nfcadapter);

        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();

        setupForegroundDispatch(this, nfcadapter);
    }


    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter nadapter) {
        nadapter.disableForegroundDispatch(activity);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        super.onNewIntent(intent);
        handleIntent(intent);
        Log.d("NFCTASK", "DETECTED");
    }










    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0777){
            Log.d("QRRETURN","Recieved"+data.getStringExtra("QR"));
            readFire(data.getStringExtra("QR"));
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    protected void readFire(String res){
        final String rs=res;
        Log.d("FIREBASE","Exec Readfire");
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("ashwinkumar14102000@gmail.com", "t")
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d("FIREAUTH", "SUCCESS");


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FIREAUTH", "FAIL");

            }
        });
        final FirebaseFirestore fb = FirebaseFirestore.getInstance();
        fb.collection("equipmentData").document(res).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Map m=document.getData();
                        d.show();
                        TextView sno= d.findViewById(R.id.sno);
                        if(m.get("qrNumber")!=null)
                            sno.setText(m.get("qrNumber").toString());
                        TextView tp=d.findViewById(R.id.type);
                        if(m.get("name")!=null)
                            tp.setText(m.get("name").toString());
                        TextView org=d.findViewById(R.id.origin);
                        if(m.get("Org port")!=null)

                            org.setText(m.get("Org port").toString());
                        TextView last=d.findViewById(R.id.scan);
                        if(m.get("Last Scan")!=null)

                            last.setText(m.get("Last Scan").toString());
                        TextView mfg=d.findViewById(R.id.mfgd);
                        Timestamp timestamp = null;
                        try {
                            timestamp = (Timestamp) m.get("dateOfInstallation");
                            mfg.setText(new Date(timestamp.getNanoseconds()*1000).toString());
                            Log.d("Date",new Date(timestamp.getNanoseconds()*1000).toString());
                        }
                        catch(Exception e){
                            Log.d("TIME","Is Not Timestap");
                            mfg.setText( m.get("dateOfInstallation").toString());

                        }


                        TextView due=d.findViewById(R.id.servicedue);
                        due.setText(m.get("servicedue").toString());
                        TextView info=d.findViewById(R.id.info);
                        info.setText(m.get("modelNumber").toString());
                        info.setMovementMethod(new ScrollingMovementMethod());
                        TextView id=d.findViewById(R.id.textViewx);
                        id.setText(rs);
                        Button rep=d.findViewById(R.id.report);
                        rep.setOnClickListener(v -> {
                            Map mx=new HashMap();
                            mx.put("shelved",true);
                            fb.collection("equipmentData").document(rs).update(mx).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task1) {
                                    if(task1.isSuccessful()) {
                                        Log.d("BTNFB", "Updated");
                                        Toast.makeText(c, "Reported successfully", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                        Log.d("BT", task1.getException().toString());

                                }
                            });
                            Log.d("BTN","Clicked");
                        });

                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(c, "Invalid NFC data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }
}
