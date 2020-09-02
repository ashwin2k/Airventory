package com.ashwin2k.airventory;

import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

public class NdefReaderTaskStuddApp extends AsyncTask<Tag,Void,String> {
    Context con;
    ArrayList<String> total_name=new ArrayList<>();
    int students;
    String TAG="NFCRW";
    Dialog d,finger;
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    Boolean st=false;
    String r;
    private KeyStore keyStore;
    Dialog load;
    String name;
    boolean stat;
    public NdefReaderTaskStuddApp(Context context,boolean status)
    {
        this.con=context;
        this.stat=status;

    }
    @Override
    protected String doInBackground(Tag... params) {
        Tag tag = params[0];
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            return null;
        }

        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    return readText(ndefRecord);
                } catch (UnsupportedEncodingException e) {
                    Log.e("NFCTASK", "Unsupported Encoding", e);
                }
            }
        }



        return null;
    }
    private String readText(NdefRecord record) throws UnsupportedEncodingException {

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;


        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    @Override
    protected void onPostExecute(String result) {
        r = "BAX1234MAA1234";
        Log.d("NFCSCAN","BAX1234MAA1234");
        readFire(r);
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
                        Dialog d=new Dialog(con);
                        d.setContentView(R.layout.dialog_det);

                        d.show();
                        TextView sno= d.findViewById(R.id.sno);
                        sno.setText(m.get("qrNumber").toString());
                        TextView tp=d.findViewById(R.id.type);
                        tp.setText(m.get("name").toString());
                        TextView org=d.findViewById(R.id.origin);
                        org.setText(m.get("Org port").toString());
                        TextView last=d.findViewById(R.id.scan);
                        last.setText(m.get("Last Scan").toString());
                        TextView mfg=d.findViewById(R.id.mfgd);

                        Timestamp timestamp=(Timestamp)m.get("dateOfInstallation");
                        Log.d("Date",new Date(timestamp.getNanoseconds()*1000).toString());
                        mfg.setText(new Date(timestamp.getNanoseconds()*1000).toString());

                        TextView due=d.findViewById(R.id.servicedue);
                        due.setText(m.get("servicedue").toString());
                        TextView info=d.findViewById(R.id.info);
                        info.setText(m.get("modelNumber").toString());
                        info.setMovementMethod(new ScrollingMovementMethod());
                        TextView id=d.findViewById(R.id.textViewx);
                        id.setText(rs);
                        Button rep=d.findViewById(R.id.report);
                        rep.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Map mx=new HashMap();
                                mx.put("shelved",true);
                                fb.collection("equipmentData").document(rs).update(mx).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()) {
                                            Log.d("BTNFB", "Updated");
                                            Toast.makeText(con, "Reported successfully", Toast.LENGTH_LONG).show();
                                        }
                                        else
                                            Log.d("BT",task.getException().toString());

                                    }
                                });
                                Log.d("BTN","Clicked");
                            }
                        });

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }


}


