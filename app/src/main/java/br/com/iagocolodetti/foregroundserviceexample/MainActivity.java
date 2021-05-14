package br.com.iagocolodetti.foregroundserviceexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String CLASS_NAME = getClass().getSimpleName();

    private MyService myService;

    private TextView tvServiceStatus;
    private TextView tvServiceSeconds;
    private EditText etText;
    private TextView tvText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvServiceStatus = findViewById(R.id.tvServiceStatus);
        tvServiceSeconds = findViewById(R.id.tvServiceSeconds);
        etText = findViewById(R.id.etText);
        tvText = findViewById(R.id.tvText);
        Button btChangeText = findViewById(R.id.btChangeText);
        Button btStart = findViewById(R.id.btStart);
        Button btStop = findViewById(R.id.btStop);

        btChangeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SingletonServiceManager.isMyServiceRunning) {
                    String text = etText.getText().toString();
                    if (!text.isEmpty()) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("text", text);
                        editor.apply();
                        myService.changeText();
                        tvText.setText(text);
                    } else {
                        Toast.makeText(MainActivity.this, "You need to write some text", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "This service is not running", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SingletonServiceManager.isMyServiceRunning) {
                    String text = etText.getText().toString();
                    if (!text.isEmpty()) {
                        try {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("text", text);
                            editor.apply();
                            Intent intent = new Intent(MainActivity.this, MyService.class);
                            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                            if (Build.VERSION.SDK_INT >= 22 && Build.VERSION.SDK_INT < 26) {
                                startService(intent);
                            } else if (Build.VERSION.SDK_INT >= 26) {
                                startForegroundService(intent);
                            }
                            tvServiceStatus.setText(R.string.tv_service_status_on);
                            tvText.setText(text);
                        } catch (Exception ex) {
                            Toast.makeText(MainActivity.this, "Error: Service could not be started", Toast.LENGTH_SHORT).show();
                            Log.e(CLASS_NAME, "btStart(): " + ex.getMessage());
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "You need to write some text", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "This service already running", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SingletonServiceManager.isMyServiceRunning) {
                    unbindService(mConnection);
                    stopService(new Intent(MainActivity.this, MyService.class));
                    tvServiceStatus.setText(R.string.tv_service_status_off);
                    tvText.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "This service is not running", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Log.d(CLASS_NAME, "onCreate()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (SingletonServiceManager.isMyServiceRunning) {
                Intent intent = new Intent(MainActivity.this, MyService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                tvServiceStatus.setText(R.string.tv_service_status_on);
                etText.setText(preferences.getString("text", ""));
                tvText.setText(preferences.getString("text", ""));
            } else {
                tvServiceStatus.setText(R.string.tv_service_status_off);
            }
            Log.d(CLASS_NAME, "onStart()");
        } catch (Exception ex) {
            Log.e(CLASS_NAME, "onStart(): " + ex.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("myService"));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int seconds = intent.getIntExtra("seconds", 0);
            tvServiceSeconds.setText(getString(R.string.tv_service_seconds, seconds));
        }
    };

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (SingletonServiceManager.isMyServiceRunning) {
            unbindService(mConnection);
        }
        Log.d(CLASS_NAME, "onDestroy()");
        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getServerInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {

        }
    };
}