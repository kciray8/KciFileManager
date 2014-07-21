package com.kciray.android.filemanager;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TextView textView = (TextView)findViewById(R.id.text);
        String html = getResources().getString(R.string.aboutText);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(html));

        Button onClose = (Button) findViewById(R.id.close);
        onClose.setOnClickListener(v -> finish());
    }
}
