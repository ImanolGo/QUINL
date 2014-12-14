package net.imanolgomez.qnl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by imanolgo on 12/12/14.
 */

public class SoundManager {

    private final String SAMPLES_PATH = "/QNL/Samples/";
    public static String TAG = "SoundManager";
    private HashMap<Integer, Sample> mSamples;

    private static SoundManager sSoundManager;

    private Context mAppContext;
    private MediaPlayer mPlayer;

    private SoundManager(Context appContext) {
        mAppContext = appContext;
        this.initialize();
    }

    public static SoundManager get(Context c) {
        if (sSoundManager == null) {
            sSoundManager = new SoundManager(c.getApplicationContext());
        }
        return sSoundManager;
    }

    private void initialize(){
        File folder = new File(Environment.getExternalStorageDirectory() + SAMPLES_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
     }

    public void playSample(int sampleId) {

        if(!mSamples.containsKey(sampleId)){
            Log.e(TAG,"PlaySample-> No key found with Id: " + sampleId);
            return;
        }

        stop();

        try {
            String uri = SAMPLES_PATH + mSamples.get(sampleId).getName() + ".aiff";
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(uri);
            mPlayer.prepare();

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });

            mPlayer.start();

        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
    }

    public void addSample(Sample sample) {
        mSamples.put(sample.getId(), sample);
    }

}
