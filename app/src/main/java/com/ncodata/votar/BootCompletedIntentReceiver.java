package com.ncodata.votar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by perez.juan.jose on 30/01/2018.
 */

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedIntentRece";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onSupportNavigateUp()");
        if ("android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())
                ||  "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())
                ) {
            Intent pushIntent = new Intent(context, MainActivity.class);
            pushIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(pushIntent);
        }
    }
}
