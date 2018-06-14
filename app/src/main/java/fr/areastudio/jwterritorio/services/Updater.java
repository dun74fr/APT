package fr.areastudio.jwterritorio.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class Updater {

    private AlarmManager alarmMgr;

    private PendingIntent alarmIntent;

    public void setAlarm(Context context) {

        alarmMgr = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpdaterReceiver.class);

        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {

            alarmIntent = PendingIntent.getBroadcast(context, 1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.cancel(alarmIntent);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis()+1000*30);
//            calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)+
//                    (int) Math.round((Math.random() * 4)));
            // calendar.set(Calendar.MINUTE, 29);
            Log.i("LocationService", "Setting alarm to " + calendar.getTime());
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR * 4,
                    alarmIntent);
        }
        else {
            Log.i("LocationService", "already set.");
        }
    }
}