package av.pmadmin;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static android.graphics.Color.WHITE;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private ProgressDialog mProgressDialog;
    TextView nodata;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerAdapter adapter;
    ArrayList<CardData> arrayList = new ArrayList<>();
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nodata = findViewById(R.id.nodata);



        recyclerView = findViewById(R.id.recycler_msg);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new RecyclerAdapter(arrayList, MainActivity.this);
        recyclerView.setAdapter(adapter);
        showCards();
        Button btn_ref = findViewById(R.id.btn_refresh_main);
        btn_ref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
    }

    private void showCards(){

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading . . .");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        arrayList.clear();
        if (haveConnection()){
            //Toast.makeText(this, "HAVE CONNECTION", Toast.LENGTH_SHORT).show();
            mProgressDialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, DbData.getTitle,(String)null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Toast.makeText(MainActivity.this, "Updating . . .", Toast.LENGTH_SHORT).show();

                            int count = 0;
                            while (count<response.length())
                            {
                                try{
                                    JSONObject jsonObject = response.getJSONObject(count);
                                    if (Objects.equals(jsonObject.getString("message"), "nodata"))
                                    {
                                        Toast.makeText(MainActivity.this, "SERVER : No data", Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                        nodata.setVisibility(View.VISIBLE);
                                        break;
                                    }

                                    if (Objects.equals(jsonObject.getString("message"), "hasdata"))
                                    {
                                        nodata.setVisibility(View.GONE);
                                        mProgressDialog.dismiss();
                                        arrayList.add(new CardData(jsonObject.getString("group_id"),jsonObject.getString("proj_title"),jsonObject.getString("proj_lang"),jsonObject.getString("mem_name_1"),jsonObject.getString("status")));
                                        count++;
                                    }
                                } catch (JSONException e) {
                                    Log.d("JSONEXCEPTION : ",e.toString());
                                    mProgressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "SERVER : JSONException", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                                /*updates_progress.setActivated(false);*/
                                mProgressDialog.dismiss();
                                adapter.notifyDataSetChanged();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this, "onErRsp : "+error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
            Singleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
            adapter.notifyDataSetChanged();
        }
        else{
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items,menu);
        getMenuInflater().inflate(R.menu.actionbar_items,menu);
        MenuItem item = menu.findItem(R.id.ab_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        //search customisation
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(WHITE);
        searchEditText.setHintTextColor(WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            searchEditText.setCompoundDrawableTintList(ColorStateList.valueOf(WHITE));
        }
        searchEditText.setHint(R.string.hint_news_search);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_about : aboutUs();  break;
        }
        return true;
    }

    private boolean haveConnection(){
         ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
         assert connectivityManager != null;
         NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
         return networkInfo != null && networkInfo.isConnected();
     }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();
        ArrayList<CardData> newList = new ArrayList<>();
        for (CardData msg_data : arrayList)
        {
            String query = msg_data.getProj_title().toLowerCase()+" "+msg_data.getGid();
            if (query.contains(newText))
                newList.add(msg_data);
        }
        adapter.setFilter(newList);
        return true;
    }

    void aboutUs(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.about_menu,null);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}
