package lgztec.tecdaily;


import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static android.content.Context.CONNECTIVITY_SERVICE;

class CardRecyclerAdapter  extends RecyclerView.Adapter<CardRecyclerAdapter.MyViewHolder> {

    private ArrayList<CardData> arrayList = new ArrayList<>();
    private Activity activity;
    private Context context;

    CardRecyclerAdapter(ArrayList<CardData> arrayList, Context context){
        this.arrayList = arrayList;
        activity = (Activity)context;
        this.context = context;
    }

    //Binded Layoutfile
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout,parent,false);
        return new MyViewHolder(view);
    }

    //setting up data
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final DbCardHelper dbCardHelper = new DbCardHelper(context);
        SQLiteDatabase dbr = dbCardHelper.getReadableDatabase();

        final String card_id = arrayList.get(position).getCard_id();
        final String card_title = arrayList.get(position).getCard_title();
        final String card_tag = arrayList.get(position).getCard_tag();
        final String card_time = arrayList.get(position).getCard_time();
        String img_url = DbCardData.IMAGE_URL+arrayList.get(position).getCard_id()+".png";
        final boolean fav_state = dbCardHelper.isFavorite(dbr,arrayList.get(position).getCard_id());

        Glide.with(context)
                .load(img_url)
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_image)
                .into(holder.card_image);
        holder.card_title.setText(arrayList.get(position).getCard_title());
        holder.card_tag.setText(arrayList.get(position).getCard_tag());
        holder.card_time.setText(arrayList.get(position).getCard_time());

        //favorite icon chager

        if (dbCardHelper.isFavorite(dbr,arrayList.get(position).getCard_id()))
            holder.fav_button.setImageDrawable(activity.getDrawable(R.drawable.ic_favorite_yes));
        else
            holder.fav_button.setImageDrawable(activity.getDrawable(R.drawable.ic_favorite_no));

        //favorites adding and removing
        holder.fav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase dbw = dbCardHelper.getWritableDatabase();
                SQLiteDatabase dbr = dbCardHelper.getReadableDatabase();
                if(dbCardHelper.isFavorite(dbr,card_id))
                {
                    dbCardHelper.removeFavorite(dbw,card_id);
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }
                else {
                    dbCardHelper.setFavorite(dbw,card_id);
                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }
                dbCardHelper.close();
            }
        });

        //on cardclick
        holder.card_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeNewsCard(card_id,card_title,card_tag,card_time,fav_state);
            }
        });

        holder.card_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeNewsCard(card_id,card_title,card_tag,card_time,fav_state);
            }
        });
        dbCardHelper.close();
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView card_image;
        TextView card_title;
        TextView card_tag;
        TextView card_time;
        ImageView fav_button;

        MyViewHolder(View itemView) {
            super(itemView);
            card_image = (ImageView)itemView.findViewById(R.id.imv_cardImage);
            card_title = (TextView)itemView.findViewById(R.id.tv_cardTitle);
            card_tag = (TextView)itemView.findViewById(R.id.tv_newsCardTag);
            card_time = (TextView)itemView.findViewById(R.id.tv_newsCardTime);
            fav_button = (ImageView) itemView.findViewById(R.id.imbtn_fav);
        }
    }

    //refreshing data
    void setFilter(ArrayList<CardData> newList){
        arrayList = new ArrayList<>();
        arrayList.addAll(newList);
        notifyDataSetChanged();
    }

    private void makeNewsCard(String id, String card_title, String card_tag, String card_time, boolean fav_state){

        final DbCardHelper dbCardHelper = new DbCardHelper(context);
        SQLiteDatabase dbr = dbCardHelper.getReadableDatabase();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.news_card,null);

        ImageView newsCrad_image = (ImageView)view.findViewById(R.id.imv_newscard);
        TextView newsCrad_title = (TextView)view.findViewById(R.id.tv_newsCardTitle);
        TextView newsCrad_tag = (TextView)view.findViewById(R.id.tv_newsCardTag);
        TextView newsCrad_time = (TextView)view.findViewById(R.id.tv_newsCardTime);
        final ImageView newsCrad_favbtn = (ImageView)view.findViewById(R.id.imbtn_fav_newsCard);
        ImageButton newsCrad_closebtn = (ImageButton)view.findViewById(R.id.imbtn_newsCard_close);
        final ProgressBar WebLoadStatProgressBar = (ProgressBar)view.findViewById(R.id.progress_newsCard_webLoadStat);
        final Button  WebLoadStatButton = (Button)view.findViewById(R.id.btn_newsCard_webLoadStat);
        final RelativeLayout WebLoadStatLayout = (RelativeLayout)view.findViewById(R.id.RL_newsCard_webStat);
        final WebView newsCard_webView = (WebView)view.findViewById(R.id.WebView_newsCard);

        //webview config
        //newsCard_webView.getSettings().setAppCacheMaxSize( 5 * 1024 * 1024 ); // 5MB
        newsCard_webView.getSettings().setAppCachePath( context.getCacheDir().getAbsolutePath() );
        newsCard_webView.getSettings().setAllowFileAccess( true );
        newsCard_webView.getSettings().setAppCacheEnabled( true );
        newsCard_webView.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT );
        if (!haveConnection()){
            newsCard_webView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK );
        }
        newsCard_webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onLoadResource(WebView view, String url) {
                WebLoadStatButton.setText("Loading . . . ");
                WebLoadStatButton.setEnabled(false);
                Log.d("cardloding","loadinggggg");

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                WebLoadStatLayout.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                WebLoadStatLayout.setVisibility(View.VISIBLE);
                WebLoadStatProgressBar.setVisibility(View.GONE);
                WebLoadStatButton.setEnabled(true);
                WebLoadStatButton.setText("REFRESH!");
            }
        });


        String img_url = DbCardData.IMAGE_URL+id+".png";
        final String web_url = DbCardData.PAGE_URL+id+".html";
        final String card_id = id;

        Glide.with(context).load(img_url).placeholder(R.drawable.loading).error(R.drawable.no_image).into(newsCrad_image);
        newsCrad_title.setText(card_title);
        newsCrad_tag.setText(card_tag);
        newsCrad_time.setText(card_time);
        newsCard_webView.loadUrl(web_url);
        if (dbCardHelper.isFavorite(dbr,card_id))
            newsCrad_favbtn.setImageDrawable(activity.getDrawable(R.drawable.ic_favorite_yes));
        else
            newsCrad_favbtn.setImageDrawable(activity.getDrawable(R.drawable.ic_favorite_no));

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();

        newsCrad_favbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase dbw = dbCardHelper.getWritableDatabase();
                SQLiteDatabase dbr = dbCardHelper.getReadableDatabase();
                if(dbCardHelper.isFavorite(dbr,card_id))
                {
                    dbCardHelper.removeFavorite(dbw,card_id);
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    newsCrad_favbtn.setImageDrawable(activity.getDrawable(R.drawable.ic_favorite_no));
                    notifyDataSetChanged();
                }
                else {
                    dbCardHelper.setFavorite(dbw,card_id);
                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                    newsCrad_favbtn.setImageDrawable(activity.getDrawable(R.drawable.ic_favorite_yes));
                    notifyDataSetChanged();
                }
                dbCardHelper.close();
            }
        });

        newsCrad_closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        WebLoadStatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newsCard_webView.loadUrl(web_url);
            }
        });
    }

    private boolean haveConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
