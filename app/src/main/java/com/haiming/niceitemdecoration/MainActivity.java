package com.haiming.niceitemdecoration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.haiming.niceitemdecoration.decoration.NiceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private boolean isStagger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.recycler_view);
        initRecycler();
    }

    private void initRecycler() {
        RecyclerAdaper adaper = new RecyclerAdaper(this, getData());
        mRecyclerView.setAdapter(adaper);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL));
        int leftRight = dip2px(15);
        int topBottom = dip2px(15);
        mRecyclerView.addItemDecoration(new NiceItemDecoration(leftRight, topBottom, Color.BLACK));
    }

    private List<String> getData() {
        List<String> infos = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            infos.add("item" + i);
        }
        return infos;
    }

    public int dip2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }


}
