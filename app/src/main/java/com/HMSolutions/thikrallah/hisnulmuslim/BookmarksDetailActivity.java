package com.HMSolutions.thikrallah.hisnulmuslim;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.hisnulmuslim.adapter.BookmarksDetailAdapter;
import com.HMSolutions.thikrallah.hisnulmuslim.adapter.BookmarksDetailRecycleAdapter;
import com.HMSolutions.thikrallah.hisnulmuslim.loader.BookmarkDetailsLoader;
import com.HMSolutions.thikrallah.hisnulmuslim.model.Dua;

import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Khalid on 31 يوليو.
 */
public class BookmarksDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Dua>> {
    private int duaIdFromDuaListActivity;
    private String duaTitleFromDuaListActivity;
    private BookmarksDetailAdapter adapter;
    private BookmarksDetailRecycleAdapter adapterRecycle;
    // private ListView recyclerView;
    private RecyclerView recyclerView;

    private Toolbar toolbar;
    private TextView my_toolbar_duaGroup_number;
    private AutofitTextView my_autofit_toolbar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks_detail);

        toolbar = (Toolbar) findViewById(R.id.my_detail_action_bar);
        my_toolbar_duaGroup_number = (TextView) findViewById(R.id.txtReference_duaDetail);
        my_autofit_toolbar_title = (AutofitTextView) findViewById(R.id.dua_detail_autofit_actionbar_title);
        View mToolbarShadow = findViewById(R.id.view_toolbar_shadow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.recyclerView = (RecyclerView) findViewById(R.id.bookmarksDuaDetailListView);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        duaIdFromDuaListActivity = bundle.getInt("dua_id");
        duaTitleFromDuaListActivity = bundle.getString("dua_title");

        my_toolbar_duaGroup_number.setText(Integer.toString(duaIdFromDuaListActivity));
        my_autofit_toolbar_title.setText(duaTitleFromDuaListActivity);
        setTitle("");

        if (Build.VERSION.SDK_INT >= 21) {
            mToolbarShadow.setVisibility(View.GONE);
        }

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<Dua>> onCreateLoader(int id, Bundle args) {
        return new BookmarkDetailsLoader(BookmarksDetailActivity.this, duaIdFromDuaListActivity);
    }

    @Override
    public void onLoadFinished(Loader<List<Dua>> loader, List<Dua> data) {
        if (adapterRecycle == null) {
        // adapterRecycle = new BookmarksDetailRecycleAdapter(this, data, duaTitleFromDuaListActivity);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapterRecycle = new BookmarksDetailRecycleAdapter(this, data, duaTitleFromDuaListActivity);
            recyclerView.setAdapter(adapterRecycle);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        } else {
            adapterRecycle = new BookmarksDetailRecycleAdapter(this, data, duaTitleFromDuaListActivity);
            //adapter.setData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Dua>> loader) {
        if (adapterRecycle != null) {
            //adapterRecycle.setData(null);
        }
    }
}