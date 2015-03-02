package com.camforte.memento;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notifierServiceIntent = new Intent(context, MementoNotifierService.class);
        context.startService(notifierServiceIntent);
    }
}
