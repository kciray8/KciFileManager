package com.kciray.android.filemanager;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends ListActivity {
    public static final String EXTRA_ID = "com.kciray.intent.extra.id";

    ArrayAdapter<File> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.history);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MainActivity.getHistory());

        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent data = new Intent();
        data.putExtra(EXTRA_ID, position);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
