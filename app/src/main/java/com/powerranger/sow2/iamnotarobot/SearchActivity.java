package com.powerranger.sow2.iamnotarobot;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private MaterialSearchView searchView;
    private ArrayList<User> arrayListUser = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Init();
        InitData();

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toLowerCase();
                ArrayList<User> newList = new ArrayList<>();
                for(User user : arrayListUser) {
                    String name = user.getName().toLowerCase();
                    if(name.contains(newText)) {
                        newList.add(user);
                    }
                }

                searchAdapter.setFilter(newList);
                return true;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void Init() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        searchView = findViewById(R.id.search_view);
        searchView.setVoiceSearch(true); //or false
        recyclerView = findViewById(R.id.recycler_search_result);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        searchAdapter = new SearchAdapter(this, arrayListUser);
        recyclerView.setAdapter(searchAdapter);
    }

    private void InitData() {
        arrayListUser.add(new User("Đạt", "https://i.amz.mshcdn.com/jVlbT_4Y4wfaJaONj6dO6Qaie6M=/950x534/filters:quality(90)/2015%2F11%2F25%2F55%2FGettyImages.b564b.jpg", "thuy@gmail.com", "male"));
        arrayListUser.add(new User("Thắng", "https://ste.india.com/sites/default/files/2014/08/02/164995-aamir-khan-650.jpg", "d.newgate@yahoo.com.vn", "male"));
        arrayListUser.add(new User("Hari Won", "https://images.indianexpress.com/2018/07/aamir-khan-759.jpg", "hari@gmail.com", "male"));
    }
}
