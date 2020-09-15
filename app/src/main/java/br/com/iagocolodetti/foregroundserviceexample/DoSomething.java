package br.com.iagocolodetti.foregroundserviceexample;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class DoSomething {

    private final String CLASS_NAME = getClass().getSimpleName();

    private Timer timer;

    private final Context CONTEXT;
    private String text;

    public DoSomething(Context context, String text) {
        timer = new Timer();
        CONTEXT = context;
        this.text = text;
        Log.d(CLASS_NAME, "DoSomething()");
    }

    public void changeText(String text) {
        this.text = text;
        Log.d(CLASS_NAME, "changeText()");
    }

    public void start() {
        timer.scheduleAtFixedRate(new mainTask(), 1, 30 * 1000);
        Log.d(CLASS_NAME, "start()");
    }

    private class mainTask extends TimerTask {
        public void run() {
            try {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CONTEXT, "Service running: " + text, Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d(CLASS_NAME, "mainTask(): " + text);
            } catch (Exception ex) {
                Log.e(CLASS_NAME, "mainTask(): " + ex.getMessage());
            }
        }
    }

    public void stop() {
        try {
            timer.cancel();
            timer.purge();
            Log.d(CLASS_NAME, "stop()");
        } catch (Exception ex) {
            Log.e(CLASS_NAME, "stop(): " + ex.getMessage());
        }
    }
}
