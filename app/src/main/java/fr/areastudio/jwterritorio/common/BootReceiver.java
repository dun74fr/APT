package fr.areastudio.jwterritorio.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fr.areastudio.jwterritorio.services.Updater;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Updater updater = new Updater();
            updater.setAlarm(context);
        }
    }
}