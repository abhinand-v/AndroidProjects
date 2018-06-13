package lgztec.tecdaily;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class CardsFragment extends android.support.v4.app.Fragment implements SearchView.OnQueryTextListener {

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView.LayoutManager layoutManager;
    CardRecyclerAdapter adapter;
    ArrayList<CardData> arrayList = new ArrayList<>();
    Context context = getContext();
    Activity fActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards,container,false);
        getActivity().setTitle("News");
        setHasOptionsMenu(true);
        recyclerView = (RecyclerView)view.findViewById(R.id.rcv_cards);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefresh_news);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new CardRecyclerAdapter(arrayList,getContext());
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateCards();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateCards();
    }

    public void updateCards(){

        arrayList.clear();
        DbCardHelper dbCardHelper = new DbCardHelper(getContext());
        SQLiteDatabase db = dbCardHelper.getReadableDatabase();
        Cursor cursor = dbCardHelper.readToCard(db);
        boolean haveNews = false;

        while (cursor.moveToNext())
        {
            haveNews = true;
            String img_url = cursor.getString(cursor.getColumnIndex(DbCardData.CARD_ID));
            String title = cursor.getString(cursor.getColumnIndex(DbCardData.CARD_TITLE));
            String tag = cursor.getString(cursor.getColumnIndex(DbCardData.CRAD_TAG));
            String time = cursor.getString(cursor.getColumnIndex(DbCardData.CARD_TIME));
            String fav_state = cursor.getString(cursor.getColumnIndex(DbCardData.CARD_FAV_STAT));
            arrayList.add(new CardData(img_url,title,tag,time,fav_state));
        }

        if (!haveNews)
        {
            Toast.makeText(fActivity, "No news", Toast.LENGTH_SHORT).show();
        }
        swipeRefreshLayout.setRefreshing(false);
        adapter.notifyDataSetChanged();
        cursor.close();
        dbCardHelper.close();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fActivity = getActivity();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.actionbar_items, menu);
        MenuItem item = menu.findItem(R.id.ab_search);
        SearchView searchView = new SearchView(getContext());

        //searchview customisation
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
        searchEditText.setHintTextColor(ContextCompat.getColor(getContext(),R.color.faded_white));
        searchEditText.setHint(R.string.hint_news_search);

        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();
        ArrayList<CardData> newList = new ArrayList<>();
        for (CardData cardData : arrayList) {
            String title = cardData.getCard_title().toLowerCase();
            if (title.contains(newText))
                newList.add(cardData);
        }
        adapter.setFilter(newList);
        return true;
    }
}
