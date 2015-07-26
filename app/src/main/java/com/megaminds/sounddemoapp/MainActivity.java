package com.megaminds.sounddemoapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.toolbar_top)
    Toolbar toolbarTop;
    @Bind(R.id.volume)
    SoundLevelView volume;
    private int FEATURE_NOT_AVAILBLE;
    private int mThreshold;
    private static final int POLL_INTERVAL = 300;
    private SoundMeter soundMeter;
    private Handler handler = new Handler();
    private Runnable mRunPool = new Runnable() {
        @Override
        public void run() {
            //TODO refresh UI
            volume.setLevel((int) soundMeter.getAmplitudeEMA());
            handler.postDelayed(this, POLL_INTERVAL);
        }
    };

    private void readApplicationPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mThreshold = Integer.parseInt(prefs.getString("threshold", null));
        Log.i(TAG, "threshold=" + mThreshold);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbarTop);
        setTitle("");
        PackageManager pm = getPackageManager();
        boolean micPresent = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        if (!micPresent) {
            showDialog(FEATURE_NOT_AVAILBLE);
            return;
        }
        soundMeter = new SoundMeter();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startMeasuring();

    }


    private void startMeasuring(){
        soundMeter.start();
        handler.postDelayed(mRunPool,POLL_INTERVAL);
    }

    private void stopMeasuring(){
        soundMeter.stop();
        handler.removeCallbacks(mRunPool);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == FEATURE_NOT_AVAILBLE) {
            return new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.no_availble_feture)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create();
        } else return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMeasuring();
    }
}
