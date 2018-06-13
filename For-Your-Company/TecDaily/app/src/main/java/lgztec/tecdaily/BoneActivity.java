package lgztec.tecdaily;

import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BoneActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    private android.support.v4.app.Fragment fragment;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateCardDB();
        setContentView(R.layout.activity_bone);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottemNavigation);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationViewBehavior());
        fragmentManager = getSupportFragmentManager();
        setupBottomNavigation();
        setupBottomNavigation();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    private void setupBottomNavigation(){

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        updateCardDB();
                        int item_id = item.getItemId();
                        /*if (itemIndex!=0)
                        {
                            if (itemIndex==1)
                                item_id = R.id.bt_nv_home;
                        }*/
                        switch (item_id){

                            case R.id.bt_nv_home:
                                fragment = new CardsFragment();
                                break;

                            case R.id.bt_nv_favorites:
                                fragment = new FavFragment();
                                break;

                            case R.id.bt_nv_Options:
                                fragment = new OptionsFragment();
                                break;
                        }
                        final FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.cardContainer,fragment).commit();
                        return true;
                    }
                });
        bottomNavigationView.setSelectedItemId(R.id.bt_nv_home);
    }

    public  void updateCardDB(){

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
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("TD_volleyError : ",error.toString());
                        }
                    });
            Singleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
        }
        else {
            Log.d("TD_connection_stat : ","NO CONNECTION");
        }
    }

    private boolean haveConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
