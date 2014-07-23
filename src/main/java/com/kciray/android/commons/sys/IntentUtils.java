package com.kciray.android.commons.sys;

import android.content.Intent;
import android.os.Bundle;

import static com.kciray.android.commons.sys.KLog.v;

public class IntentUtils {
    public static void outExtras(Intent intent) {
        Bundle bundle = intent.getExtras();
        BundleUtils.outExtras(bundle);
    }

}
