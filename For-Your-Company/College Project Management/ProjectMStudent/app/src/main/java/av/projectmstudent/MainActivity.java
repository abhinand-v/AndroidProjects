package av.projectmstudent;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //buttons
    Button btn_reg,btn_stat;

    String sid,status;
    SharedPreferences settings;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_reg = findViewById(R.id.btn_reg);
        btn_stat = findViewById(R.id.btn_stat);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        sid = settings.getString("sid",null);
        status = getStat(sid);
        hasSub(sid);

        //Toast.makeText(this, sid, Toast.LENGTH_SHORT).show();
    }

    public void onMainClick(View view){

        switch (view.getId())
        {
            case R.id.btn_reg:
                showRegUI();
                break;
            case R.id.btn_stat:
                showStatUI();
                break;
        }
    }

    protected void showRegUI() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final View view = layoutInflater.inflate(R.layout.register_ui, null);

        //views
        final Spinner spn_gid = view.findViewById(R.id.spn_gr_id);
        final EditText etx_mem1 = view.findViewById(R.id.etx_mem1);
        final EditText etx_projTitle = view.findViewById(R.id.etx_proj_title);
        final EditText etx_projLang = view.findViewById(R.id.etx_proj_lang);
        Button btn_submit = view.findViewById(R.id.btn_submit);

        //fill spinner
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("Select Group number");
        ArrayAdapter<String> stringArrayAdapter = null;
        for (int i = 1; i < 29; i++) {
            spinnerArray.add("" + i);
            stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        }
        spn_gid.setAdapter(stringArrayAdapter);

        //data submission


        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spn_gid.getSelectedItemPosition()==0 || etx_mem1.getText().toString().equals("") || etx_projTitle.getText().toString().equals("") )
                {
                    Toast.makeText(MainActivity.this, "Check data !", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (regProj(spn_gid.getSelectedItem().toString(),etx_mem1.getText().toString(),etx_projTitle.getText().toString(),etx_projLang.getText().toString()))
                        hasSub(sid);
                    Toast.makeText(MainActivity.this, "SUBMITED", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    protected void showStatUI(){

        final String t_stat = getStat(sid);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final View view = layoutInflater.inflate(R.layout.status_ui,null);
        final TextView tx_status = view.findViewById(R.id.txt_status);
        tx_status.setText(t_stat);
        Button btn_stat_ref = view.findViewById(R.id.btn_ref_stat);
        builder.setView(view);

        if (t_stat!=null){
            switch (getStat(sid)) {
                case "pending":
                    tx_status.setText("NOT VERIFIED YET");
                    tx_status.setTextColor(Color.RED);
                    break;
                case "approved":
                    tx_status.setText("APPROVED");
                    tx_status.setTextColor(Color.BLUE);
                    break;
                case "contact_me":
                    tx_status.setText(getStat("CONTACT FACULTY"));
                    tx_status.setTextColor(Color.BLACK);
                    break;
            }
        }



        final AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btn_stat_ref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t_stat = getStat(sid);
                if (t_stat!=null){
                    switch (getStat(sid)) {
                        case "pending":
                            tx_status.setText("NOT VERIFIED YET");
                            tx_status.setTextColor(Color.RED);
                            break;
                        case "approved":
                            tx_status.setText("APPROVED");
                            tx_status.setTextColor(Color.DKGRAY);
                            break;
                        default:
                            tx_status.setText(getStat(sid));
                            tx_status.setTextColor(Color.BLACK);
                            break;
                    }
                }
            }
        });
    }

    protected boolean regProj(final String gid, final String name1, final String pro_title, final String pro_lang){


        StringRequest stringRequest = new StringRequest(Request.Method.POST, DbData.submit_reg,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String message = jsonObject.getString("message");
                            //Toast.makeText(MainActivity.this, "SERVER : "+message, Toast.LENGTH_SHORT).show();
                            btn_reg.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            Log.d("JSONException(POST) : ",e.toString());

                            Toast.makeText(MainActivity.this, "SERVER ERROR", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(MainActivity.this, "SERVER ERROR", Toast.LENGTH_SHORT).show();
                        //Log.d("VOLLEY ERR(POST) : ",error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("group_id",gid);
                params.put("mem_name_1",name1);
                params.put("proj_title",pro_title);
                params.put("proj_lang",pro_lang);
                params.put("sid",sid);
                params.put("status","pending");
                return params;
            }
        };

        Singleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
        return true;
    }

    protected String getStat(final String gid){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DbData.get_stat,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            //Toast.makeText(MainActivity.this, "SERVER : "+message, Toast.LENGTH_SHORT).show();
                            status = jsonObject.getString("message");
                        } catch (JSONException e) {
                           //Log.d("JSONException(POST) : ",e.toString());
                            Toast.makeText(MainActivity.this, "SERVER ERROR"+e.toString() , Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "SERVER ERROR", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                        //Log.d("VOLLEY ERR(POST) : ",error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("sid",gid);
                return params;
            }
        };

        Singleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
        return status;
    }

    protected void hasSub(final String sid){

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Checking . . .");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DbData.hasSub,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mProgressDialog.dismiss();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String message = jsonObject.getString("message");

                            if (message.equals("hasdata")){
                                mProgressDialog.dismiss();
                                btn_reg.setVisibility(View.GONE);
                            }


                            if (message.equals("nodata")){
                                mProgressDialog.dismiss();
                                btn_reg.setVisibility(View.VISIBLE);

                            }



                        } catch (JSONException e) {

                            e.printStackTrace();
                            //Toast.makeText(MainActivity.this, "SERVER ERROR"+e.toString() , Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btn_reg.setVisibility(View.VISIBLE);
                        mProgressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "SERVER ERROR", Toast.LENGTH_SHORT).show();
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

        Singleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}