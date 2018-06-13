package lgztec.tecdaily;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {

    private SignInButton btn_signin;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    ProgressBar progress_bar;
    LinearLayout login_box;
    RelativeLayout login_container;
    Snackbar snakbar_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        btn_signin = (SignInButton)findViewById(R.id.gtn_login);
        progress_bar = (ProgressBar)findViewById(R.id.progress_hr_login);
        login_box = (LinearLayout)findViewById(R.id.LL_login_box);
        login_container = (RelativeLayout) findViewById(R.id.login_container);

        loading(false);
        btn_signin.setOnClickListener(this);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();

        snakbar_error = Snackbar.make(login_container,getString(R.string.snakbar_login_error_data_con),Snackbar.LENGTH_SHORT)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCardDB("FORGUEST");
                    }
                });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.gtn_login : signIn(); break;

            case R.id.btn_guestlogin : updateCardDB("FORGUEST"); break;
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signOut(){
         Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
             @Override
             public void onResult(@NonNull Status status) {
                 updateUi(false);
             }
         });
    }

    private void signIn(){

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,REQ_CODE);

    }

    private void handleResult(GoogleSignInResult result){

        if (result.isSuccess())
        {
            GoogleSignInAccount account = result.getSignInAccount();
            String username = account.getDisplayName();
            String email = account.getEmail();
            String image_url = account.getPhotoUrl().toString();
            /*tv_username.setText(username);
            tv_email.setText(email);
            Glide.with(this).load(image_url).into(imv_profile);*/
            updateUi(true);
        }
        else {
            updateUi(false);
        }

    }

    private void updateUi(boolean isLogin){

        if (isLogin)
        {

        }
        else {

        }
    }

    public void GuestLogin(){
        startActivity(new Intent(this,BoneActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    public  void updateCardDB(String client){

        loading(true);
        if (haveConnection())
        {
            Log.d("TD_connection_stat : ","CONNECTED");
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, DbCardData.SERVER_URL,(String) null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            DbCardHelper dbCardHelper = new DbCardHelper(getApplicationContext());
                            SQLiteDatabase db = dbCardHelper.getWritableDatabase();
                            /*dbCardHelper.flushDbData(db);*/
                            int count = 0;
                            while (count<response.length())
                            {
                                try {
                                    JSONObject jsonObject = response.getJSONObject(count);
                                    dbCardHelper.saveToDatabase(db,jsonObject.getString("id"),jsonObject.getString("title"),jsonObject.getString("tag"),jsonObject.getString("time"),jsonObject.getString("date"));
                                    count++;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            GuestLogin();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (hasNews())
                                GuestLogin();
                            else{
                                loading(false);
                                snakbar_error.show();
                            }
                            Log.d("TD_volleyError : ",error.toString());
                        }
                    });
            Singleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
        }
        else {
            DbCardHelper dbCardHelper = new DbCardHelper(getApplicationContext());
            SQLiteDatabase db = dbCardHelper.getReadableDatabase();
            if (dbCardHelper.hasData(db))
            {
                Toast.makeText(this, "Connect to internet to get latest news", Toast.LENGTH_SHORT).show();
                GuestLogin();
            }
            else
                loading(false);
                Toast.makeText(this, "Please make sure Data connection is OK!", Toast.LENGTH_SHORT).show();
            Log.d("TD_connection_stat : ","NO CONNECTION");
        }
    }

    public boolean hasNews(){
        DbCardHelper dbCardHelper = new DbCardHelper(getApplicationContext());
        SQLiteDatabase db = dbCardHelper.getReadableDatabase();
        Cursor cursor = dbCardHelper.readToCard(db);
        boolean haveNews = false;

        while (cursor.moveToNext())
        {
            haveNews = true;
        }

        return haveNews;
    }

    void loading(boolean state){
        if (state)
        {
            progress_bar.setVisibility(View.VISIBLE);
            login_box.setVisibility(View.GONE);
        }
        else {
            progress_bar.setVisibility(View.GONE);
            login_box.setVisibility(View.VISIBLE);
        }

    }

    private boolean haveConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
