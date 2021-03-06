package com.example.fabian.matchit;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.example.fabian.matchit.Adapters.lvAdapterShoppingCart;
import com.example.fabian.matchit.JSONURL.JSONURLApiLogin;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class ActivityLogin extends ABaseActivity {

    private CircularProgressButton circularButton1;
    private RippleBackground rippleBackground;

    // NFC
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mNFCTechLists;
    private String stPassword, stUser;
    private String nfcData = null;

    // Orders ophalen
    Orders                                          FOrders;
    Orderline                                       FOrderline;

    // Inloggen
    private JSONObject joInloggen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        circularButton1 = (CircularProgressButton) findViewById(R.id.btnProgress);
        rippleBackground = (RippleBackground) findViewById(R.id.content);
        circularButton1.setProgress(0);
        // Start animation
        rippleBackground.startRippleAnimation();

        // NFC
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter != null) {
            // mTextView.setText("Read an NFC tag");
        } else {
            // mTextView.setText("This phone is not NFC enabled.");
        }

        // create an intent with tag data and deliver to this activity
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // set an intent filter for all MIME data
        IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefIntent.addDataType("*/*");
            mIntentFilters = new IntentFilter[] { ndefIntent };
        } catch (Exception e) {
            Log.e("TagDispatch", e.toString());
        }

        mNFCTechLists = new String[][] { new String[] { NfcF.class.getName() } };

    }

    @Override
    public void onNewIntent(Intent intent) {

        // parse through all NDEF messages and their records and pick text type only
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (data != null) {
            try {
                for (int i = 0; i < data.length; i++) {
                    NdefRecord[] recs = ((NdefMessage)data[i]).getRecords();
                    for (int j = 0; j < recs.length; j++) {
                        if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)) {
                            byte[] payload = recs[j].getPayload();
                            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                            int langCodeLen = payload[0] & 0077;

                            nfcData = new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1,
                                    textEncoding);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TagDispatch", e.toString());
            }
        }

        // Check of de nfc wel gevu

        JSONObject joNFC = null;
        try {
            joNFC = new JSONObject(nfcData);
            stUser = joNFC.optString("user");
            stPassword = joNFC.optString("password");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("user", stUser);
        if(!stUser.equals("")){
            new postLogin().execute();
        }
        else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.userFalse), Toast.LENGTH_SHORT).show();
        }
    }

    // Checken of gebruiker kan inloggen
    private class postLogin extends AsyncTask<Void, Void, Void> {
        // Doe vooraf
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            circularButton1.setProgress(50);
        }
        // Tijdens
        @Override
        protected Void doInBackground(Void... params) {
            Log.i("token", GlobalVariables.URL_MATCH_Token);

            try {
                joInloggen = JSONURLApiLogin.getJSONfromURL(GlobalVariables.URL_MATCH_Token, URLEncoder.encode(stUser, "UTF-8"), URLEncoder.encode(stPassword, "UTF-8"));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return null;
        }
        // Achteraf
        @Override
        protected void onPostExecute(Void args) {
            // Doe wat met de opgehaalde gegevens
            Login();
        }
    }


    public void Login(){

        checkLoginError checkLoginError = new checkLoginError(joInloggen);
        if(checkLoginError.getValue() == true){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.passwordFalse), Toast.LENGTH_SHORT).show();
            circularButton1.setProgress(0);
        }else{
            // Inloggen = true
            GlobalVariables.INGELOGD = true;

            try {
                GlobalVariables.BearerMatch = joInloggen.getString("access_token");

                FOrders = new Orders(this);
                FOrders.addReadyEventListener(new OrderReadyListener());
                FOrders.Get();

                FOrderline = new Orderline(this);
                FOrderline.addReadyEventListener(new OrderLineReadyListener());
                FOrderline.Get();

                Log.i("bearer", GlobalVariables.BearerMatch);
            } catch (JSONException er) {
                er.printStackTrace();
            }

        }
    }

    public class OrderReadyListener implements ReadyListener
    {
        @Override
        public void ready(ReadyEvent e) {
            // Haal orderlines op
            FOrderline.Get();
        }
    }

    public class OrderLineReadyListener implements ReadyListener
    {
        @Override
        public void ready(ReadyEvent e) {
            GlobalVariables.alOr2ders = FOrderline.getItems();
            circularButton1.setProgress(100);
            Timer timer = new Timer();
            timer = new Timer(); // At this line a new Thread will be created
            timer.schedule(new SwitchPageTask(), 1000);
        }
    }

    class SwitchPageTask extends TimerTask {

        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                public void run() {
                    Intent intent = new Intent(ActivityLogin.this, ActivityProductScannen.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

}
