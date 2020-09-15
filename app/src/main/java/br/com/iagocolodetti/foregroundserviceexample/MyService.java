package br.com.iagocolodetti.foregroundserviceexample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;

public class MyService extends Service {

    private final String CLASS_NAME = getClass().getSimpleName();

    IBinder mBinder = new LocalBinder();

    private String text;

    private final int CURRENT_TIME_MILLIS = ((int) System.currentTimeMillis());
    private final int INTENT_REQUEST_CODE = CURRENT_TIME_MILLIS;
    private final int CHANNEL_ID = CURRENT_TIME_MILLIS + 1;

    private DoSomething doSomething;

    public class LocalBinder extends Binder {
        public MyService getServerInstance() {
            return MyService.this;
        }
    }

    private Notification getNotification(String channelId) {
        if (Build.VERSION.SDK_INT >= 22 && Build.VERSION.SDK_INT < 26) {
            Spanned bigText = Build.VERSION.SDK_INT >= 24 ?
                    Html.fromHtml("<small><font color=\"#CDCDCD\">Running...</font></small>", HtmlCompat.FROM_HTML_MODE_LEGACY) :
                    Html.fromHtml("<small><font color=\"#CDCDCD\">Running...</font></small>");
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_CODE, intent, 0);
            Notification.Builder builder = new Notification.Builder(this)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Running")
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(bigText)
                            .setSummaryText("Service is running"))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.notification_icon);
            return builder.build();
        } else if (Build.VERSION.SDK_INT >= 26) {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_CODE, intent, 0);
            NotificationChannel channel = new NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Notification.Builder builder = new Notification.Builder(this, channelId)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Running")
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(Html.fromHtml("<small><font color=\"#CDCDCD\">Running...</font></small>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            .setSummaryText("Service is running"))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.notification_icon);
            return builder.build();
        } else {
            return null;
        }
    }

    public void onCreate() {
        super.onCreate();
        Log.d(CLASS_NAME, "onCreate()");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        text = preferences.getString("text", "Example");
        doSomething = new DoSomething(MyService.this, text);
        startForeground(CHANNEL_ID, getNotification(String.valueOf(CHANNEL_ID)));
        startService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(CLASS_NAME, "onStartCommand()");
        return START_STICKY;
    }

    private void startService() {
        SingletonServiceManager.isMyServiceRunning = true;
        doSomething.start();
        Log.d(CLASS_NAME, "startService()");
    }

    private void stopService() {
        SingletonServiceManager.isMyServiceRunning = false;
        doSomething.stop();
        Log.d(CLASS_NAME, "stopService()");
    }

    protected void changeText() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        text = preferences.getString("text", "Example");
        doSomething.changeText(text);
        Log.d(CLASS_NAME, "changeText()");
    }

    public void onDestroy() {
        stopService();
        Log.d(CLASS_NAME, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
