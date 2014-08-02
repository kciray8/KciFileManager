package com.kciray.trash;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextActivity extends Activity{
    public static final String EXTRA_TEXT = "com.kciray.intent.extra.EXTRA_TEXT";

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView = new TextView(this);
        String text = getIntent().getStringExtra(EXTRA_TEXT);
        textView.setText(text);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.addView(textView);

        Button button = new Button(this);
        button.setOnClickListener(event->{finish();});
        button.setText(android.R.string.ok);
        linearLayout.addView(button);

        setContentView(linearLayout);
    }

    public void setText(String text){
        textView.setText(text);
    }
}
