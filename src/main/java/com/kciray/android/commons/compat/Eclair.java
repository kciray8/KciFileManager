package com.kciray.android.commons.compat;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class Eclair {
    public static String DIRECTORY_DCIM = "DCIM";
    public static String DIRECTORY_DOWNLOADS = "Download";


    /**
     * @param context
     * @return
     */
    public static File getExternalFilesDir(Context context){
        String packageName = context.getPackageName();
        File externalPath = Environment.getExternalStorageDirectory();
        File appFiles = new File(externalPath.getAbsolutePath() + "/Android/data/" + packageName + "/files");
        return appFiles;
    }
}
