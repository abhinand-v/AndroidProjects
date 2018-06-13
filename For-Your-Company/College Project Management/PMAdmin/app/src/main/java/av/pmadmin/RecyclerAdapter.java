package av.pmadmin;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Map;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private ArrayList<CardData> arrayList;
    private Context context;
    private ProgressDialog mProgressDialog;

    RecyclerAdapter(ArrayList<CardData> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_ui,null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final String gid = arrayList.get(position).getGid();
        final String proj_title = arrayList.get(position).getProj_title();
        final String proj_lang = arrayList.get(position).getProj_lang();
        final String mem1 = arrayList.get(position).getMem1();
        final String status = arrayList.get(position).getStatus();

        holder.txt_gid.setText(context.getString(R.string.group_no_txt)+gid);
        holder.txt_proTitile.setText(proj_title);
        holder.txt_proLang.setText(proj_lang);
        holder.txt_status.setText(status);

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                statUpdateUI(gid,proj_title,proj_lang,mem1,status);
                return  true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {


        CardView cardView;
        TextView txt_gid;
        TextView txt_proTitile;
        TextView txt_proLang;
        TextView txt_status;

        MyViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_card);
            txt_gid = itemView.findViewById(R.id.txt_card_gno);
            txt_proTitile = itemView.findViewById(R.id.txt_card_projTitle);
            txt_proLang = itemView.findViewById(R.id.txt_card_proLang);
            txt_status = itemView.findViewById(R.id.txt_status);
        }
    }

    private void statUpdateUI(final String gid, String p_tit, String p_lang, String members, final String status){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View view = layoutInflater.inflate(R.layout.update_stat_ui,null);

        final EditText etx_gid = view.findViewById(R.id.etx_stat_gid);
        final EditText etx_tit = view.findViewById(R.id.etx_stat_title);
        final EditText etx_mem = view.findViewById(R.id.etx_stat_members);
        final EditText etx_lang = view.findViewById(R.id.etx_stat_lang);
        final Spinner spn_stat = view.findViewById(R.id.spn_stat_stat);
        final Button up_btn = view.findViewById(R.id.btn_update_stat);
        final Button del_btn = view.findViewById(R.id.btn_delete);

        etx_gid.setText(gid);
        etx_tit.setText(p_tit);
        etx_mem.setText(members);
        etx_lang.setText(p_lang);

        switch (status) {
            case "pending":
                spn_stat.setSelection(0);
                break;
            case "approved":
                spn_stat.setSelection(1);
                break;
            case "contact_me":
                spn_stat.setSelection(2);
                break;
        }

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        up_btn.setOnClickListener(new View.OnClickListener() {
            int cnf = 0;
            @Override
            public void onClick(View v) {
                cnf++;
                if (cnf == 1){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        up_btn.setBackground(context.getDrawable(R.drawable.red_button_cnf));
                        up_btn.setTextColor(Color.YELLOW);
                    }
                    up_btn.setText("CONFIRM ?");
                }
                if (cnf == 2)
                {
                    if(etx_gid.getText().toString().equals("") || etx_tit.getText().toString().equals("") || etx_lang.getText().toString().equals("") || etx_mem.getText().toString().equals(""))
                        Toast.makeText(context, "Check Data", Toast.LENGTH_SHORT).show();
                    else {
                        update_stat(gid,etx_gid.getText().toString(),etx_tit.getText().toString(),etx_lang.getText().toString(),etx_mem.getText().toString(),spn_stat.getSelectedItemPosition());
                        dialog.dismiss();
                    }
                }
            }
        });

        del_btn.setOnClickListener(new View.OnClickListener() {
            int cnf = 0;
            @Override
            public void onClick(View v) {
                cnf++;
                if (cnf == 1){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        del_btn.setBackground(context.getDrawable(R.drawable.red_button_cnf));
                    }
                    del_btn.setTextColor(Color.YELLOW);
                    del_btn.setText("CONFIRM ?");
                }

                if (cnf == 2)
                {
                    delete_card(gid);
                    dialog.dismiss();
                }
            }
        });
    }

    private void update_stat(final String ogid, final String gid, final String p_tit, final String p_lang, final String p_mem, final int stat){

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Updating ! Please wait");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DbData.setStat,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String message = jsonObject.getString("message");
                            //Toast.makeText(context, "GID : "+gid+" status : "+stat, Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, "SERVER : "+message, Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                            mProgressDialog.dismiss();
                        } catch (JSONException e){
                            mProgressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.dismiss();
                        error.printStackTrace();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("ogid",ogid);
                params.put("gid",gid);
                params.put("title",p_tit);
                params.put("lang",p_lang);
                params.put("mem",p_mem);
                switch (stat) {
                    case 0:
                        params.put("status", "pending");
                        break;
                    case 1:
                        params.put("status", "approved");
                        break;
                    case 2:
                        params.put("status", "contact_me");
                        break;
                }
                return params;
            }
        };
        Singleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    private void delete_card(final String gid){

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Deleting ! Please wait");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DbData.delete,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String message = jsonObject.getString("message");
                            Toast.makeText(context, "SERVER : "+message, Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                            mProgressDialog.dismiss();
                        } catch (JSONException e){
                            e.printStackTrace();
                            mProgressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        mProgressDialog.dismiss();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("gid",gid);
                return params;
            }
        };
        Singleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    void setFilter(ArrayList<CardData> newList){
        arrayList = new ArrayList<>();
        arrayList.addAll(newList);
        notifyDataSetChanged();
    }
}
