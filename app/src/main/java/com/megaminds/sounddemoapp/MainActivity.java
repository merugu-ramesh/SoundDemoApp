package com.megaminds.sounddemoapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.toolbar_top)
    Toolbar toolbarTop;
    @Bind(R.id.toolbacontent)
    TextView toolbacontent;
    @Bind(R.id.radiogroup)
    RadioGroup radiogroup;
    @Bind(R.id.quiet_radio_btn)
    RadioButton quietRadioBtn;
    @Bind(R.id.group_radio_btn)
    RadioButton groupRadioBtn;
    @Bind(R.id.noisy_radio_btn)
    RadioButton noisyRadioBtn;
    @Bind(R.id.quiet_dur_txtview)
    TextView quietDurTxtview;
    @Bind(R.id.group_dur_txtview)
    TextView groupDurTxtview;
    @Bind(R.id.noisy_dur_txtview)
    TextView noisyDurTxtview;
    private int FEATURE_NOT_AVAILBLE;
    private int mThreshold;
    private static final int POLL_INTERVAL = 1000;
    private SoundMeter soundMeter;
    private long quietSec, groupSec, noisySec;

    private Handler handler = new Handler();
    private Runnable mRunPool = new Runnable() {
        @Override
        public void run() {
            //TODO refresh UI
//            volume.setLevel((int) soundMeter.getAmplitudeEMA());
            updateTime((int) soundMeter.getAmplitudeEMA());
            handler.postDelayed(this, POLL_INTERVAL);
        }
    };

    private void updateTime(int level) {
        if (level <= 4) {
            quietRadioBtn.setChecked(true);
            quietSec++;
            quietDurTxtview.setText(getTime(quietSec));
        } else if (level < 8) {
            groupRadioBtn.setChecked(true);
            groupSec++;
            groupDurTxtview.setText(getTime(groupSec));
        } else {
            noisyRadioBtn.setChecked(true);
            noisySec++;
            noisyDurTxtview.setText(getTime(noisySec));
        }
    }

    String getTime(long sec) {
        if (sec < 60) {
            return "<1";
        } else {
            return String.valueOf(sec / 60);
        }
    }

    private void readApplicationPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        mThreshold = Integer.parseInt(prefs.getString("threshold", null));
        long millis = prefs.getLong("millis", System.currentTimeMillis());
        Calendar currCal = Calendar.getInstance();
        Calendar prevCal = Calendar.getInstance();
        prevCal.setTimeInMillis(millis);
//        if (currCal.get(Calendar.DATE) != prevCal.get(Calendar.DATE)) {
            prefs.edit().clear().commit();
//        }
        quietSec = prefs.getLong("Quiet_millis", 0);
        groupSec = prefs.getLong("Group_millis", 0);
        noisySec = prefs.getLong("Noisy_millis", 0);
        Log.i(TAG, "threshold=" + mThreshold);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putLong("Quiet_millis", quietSec)
                .putLong("Group_millis", groupSec)
                .putLong("Noisy_millis", noisySec).commit();
        stopMeasuring();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMeasuring();
        quietDurTxtview.setText(getTime(quietSec));
        groupDurTxtview.setText(getTime(groupSec));
        noisyDurTxtview.setText(getTime(noisySec));
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
        readApplicationPreferences();
    }

    private void startMeasuring() {
        soundMeter.start();
        handler.postDelayed(mRunPool, POLL_INTERVAL);
    }

    private void stopMeasuring() {
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

}
