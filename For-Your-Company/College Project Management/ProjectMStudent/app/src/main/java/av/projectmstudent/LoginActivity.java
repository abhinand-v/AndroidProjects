package av.projectmstudent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    final int PERMISSION_REQUEST_CODE = 1101;
    String DEVICE_IMEI,DEVICE_NAME,SID;
    TelephonyManager telephonyManager;
    private ProgressDialog mProgressDialog;
    boolean isDeviceReg;
    SharedPreferences settings;

    EditText etx_idno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etx_idno = findViewById(R.id.etx_login_idNo);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading . . .");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        if (settings.contains("sid")){
            String s = settings.getString("sid",null);
            devicePresent(s);
            if (s!=null){
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }
        }
    }

    //request for permission
    @SuppressLint("HardwareIds")
    public void requestPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
        }
        else {
            telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                DEVICE_IMEI = telephonyManager.getDeviceId();
            }
            //Toast.makeText(this, "IMEI1 : "+DEVICE_IMEI, Toast.LENGTH_SHORT).show();
        }
    }

    //on permission grant
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (telephonyManager != null) {
                        DEVICE_IMEI = telephonyManager.getDeviceId();

                    }
                    //Toast.makeText(this, "IMEI2 : "+DEVICE_IMEI, Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "Permission Required! fix this mannualy", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    public void onLoginClick(View view){
       switch (view.getId())
       {
           case R.id.btn_login:
               SID = etx_idno.getText().toString();
               if (!SID.equals("") && SID.substring(0,3).equals("LEM") && SID.length()==7) {
                   if (!checkId(SID)) {
                       registerDevice();
                   }
                   /* error auth message */

               }
               else Toast.makeText(this, "Check Enterd ID", Toast.LENGTH_SHORT).show();
               break;
       }
    }

    boolean checkId(final String studid){
        requestPermission();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading . . .");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        isDeviceReg = false;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DbData.device_reg_read,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String d_name, d_id;
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            d_name = jsonObject.getString("device_name");
                            d_id = jsonObject.getString("device_imei");
                            //Toast.makeText(LoginActivity.this, DEVICE_IMEI+" : "+d_name, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(LoginActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();

                            if (d_id.equals(DEVICE_IMEI))
                            {
                                mProgressDialog.dismiss();
                                showRedAlert();
                                isDeviceReg = true;
                                //Toast.makeText(LoginActivity.this, "Next Activity", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                mProgressDialog.dismiss();
                                showWarnAlertUI(d_name);
                                //Toast.makeText(LoginActivity.this, "Cannot proceed !", Toast.LENGTH_SHORT).show();
                            }

                            if (jsonObject.getString("message").equals("nodata"))
                            {
                                isDeviceReg = false;
                                Toast.makeText(LoginActivity.this, "DEVICE NOT REG", Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                            }
                            else
                            {
                                isDeviceReg = true;
                                //Toast.makeText(LoginActivity.this, "DEVICE REG", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(LoginActivity.this, "DID : "+d_name+" IMEI : "+d_id, Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                            }

                        } catch (JSONException e) {
                            mProgressDialog.dismiss();
                            //Log.d("CKJSONException chekid:",e.toString());
                            //Toast.makeText(LoginActivity.this, "SERVER ERROR chid: "+e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Check Connection!", Toast.LENGTH_SHORT).show();
                Log.d("CKonErrResp chekid : ",error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("sid",studid);
                return params;
            }
        };

        Singleton.getInstance(LoginActivity.this).addToRequestQueue(stringRequest);

        return isDeviceReg;
    }

    protected String getDeiceName(){
        String Man = Build.MANUFACTURER;
        String Mod = Build.MODEL;
        if (Mod.startsWith(Man))
            return Mod.toUpperCase();
        else
            return Man.toUpperCase()+" "+Mod.toUpperCase();
    }

    private void registerDevice(){

        String sid = etx_idno.getText().toString();
        DEVICE_NAME = getDeiceName();
        SID = sid;

        mProgressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DbData.device_reg,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mProgressDialog.dismiss();
                        try {
                            mProgressDialog.dismiss();
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String message = jsonObject.getString("message");
                            Toast.makeText(LoginActivity.this, "SERVER : "+message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mProgressDialog.dismiss();
                            //Toast.makeText(LoginActivity.this, "SERVER : JSONException", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                //Toast.makeText(LoginActivity.this, "Check Connection!", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("d_name",DEVICE_NAME);
                params.put("d_imei",DEVICE_IMEI);
                params.put("sid",SID);
                return params;
            }
        };

        Singleton.getInstance(LoginActivity.this).addToRequestQueue(stringRequest);
    }

    private void showWarnAlertUI(String d_name){
        mProgressDialog.dismiss();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final View view = layoutInflater.inflate(R.layout.warning_ui,null);

        TextView tx_dev_name = view.findViewById(R.id.txt_alertBox_deviceName);
        tx_dev_name.setText(d_name);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void showRedAlert(){
        mProgressDialog.dismiss();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final View view = layoutInflater.inflate(R.layout.alert_terms_ui,null);



        Button btn_terms = view.findViewById(R.id.btn_terms_agree);
        btn_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("sid",SID);
                editor.commit();
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }
        });

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void devicePresent(final String sid){
        requestPermission();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DbData.get_stat,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String status = jsonObject.getString("message");
                            if(status.equals("nodata")){
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("sid",null);
                                editor.commit();
                                Toast.makeText(LoginActivity.this, "SRP "+status, Toast.LENGTH_SHORT).show();
                                Toast.makeText(LoginActivity.this, "Login again!", Toast.LENGTH_SHORT).show();
                                recreate();
                            }

                            if (status.equals("hasdata")){
                                Toast.makeText(LoginActivity.this, "SRP "+status, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                finish();
                            }
                        } catch (JSONException e) {
                            //Log.d("JSONException(POST) : ",e.toString());
                            Toast.makeText(LoginActivity.this, "SERVER ERROR"+e.toString() , Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "SERVER ERROR", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                        //Log.d("VOLLEY ERR(POST) : ",error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("sid",sid);
                return params;
            }
        };

        Singleton.getInstance(LoginActivity.this).addToRequestQueue(stringRequest);
    }

}
