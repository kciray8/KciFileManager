package com.kciray.android.filemanager;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class FileView extends ListView{
    ScrollPosition scrollPosition;

    public FileView(Context context) {
        super(context);
    }

    public FileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
