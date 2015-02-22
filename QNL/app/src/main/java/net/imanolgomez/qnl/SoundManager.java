package net.imanolgomez.qnl;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by imanolgo on 12/12/14.
 */

public class SoundManager {

    private static final String SAMPLES_RELATIVE_PATH = "/QNL/Samples/";
    private static final String SAMPLES_ABSOLUTE_PATH = Environment.getExternalStorageDirectory() + SAMPLES_RELATIVE_PATH;
    private static final String EMPTY_STRING = "";
    public static String TAG = "SoundManager";
    private static String DEFAULT_SAMPLE_NAME =  "radar_7s_2.5Khz.wav";
    private static final String ENDPOINT = "http://www.o-a.info/qnl/lib/sound.php";

    private DBManager mDBManager;

    private HashMap<Integer, Sample> mSamples;
    private Sample mCurrentSample;

    private static SoundManager sSoundManager;

    private Context mAppContext;
    private MediaPlayer mPlayer;

    private SoundManager(Context appContext) {
        mAppContext = appContext;
        mSamples = new HashMap<Integer, Sample>();
        this.initialize();
    }

    public static SoundManager get(Context c) {
        if (sSoundManager == null) {
            sSoundManager = new SoundManager(c.getApplicationContext());
        }
        return sSoundManager;
    }

    private void initialize(){
        mDBManager = DBManager.get(mAppContext);
        createSamplesFolder();
        //addDefaultSample();
        startRetrievingSamples();
    }

    private void createSamplesFolder(){
        File folder = new File(SAMPLES_ABSOLUTE_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public void startRetrievingSamples(){
        mCurrentSample = null;
        new retrieveSamples().execute();
    }


    private class retrieveSamples extends AsyncTask<Void,Void,String> {
        String result;
        @Override
        protected String doInBackground(Void... params) {
            try {
                result = new ServerCommunicator(mAppContext).getUrl(ENDPOINT+"?versions=1");
                Log.i(TAG, "Retrieving sample data: " + result);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send tracking data ", ioe);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            createSamples(result);
        }

    }

    private class getSampleInfo extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = EMPTY_STRING;
            for (String url : urls) {
                try {
                    result = new ServerCommunicator(mAppContext).getUrl(url);
                    Log.i(TAG, "Retrieving sample data from: " + url);
                    //Log.i(TAG, "Retrieving routes data: " + result);
                } catch (IOException ioe) {
                    Log.e(TAG, "Failed to retrieve data: " + url , ioe);
                }

            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            createSingleSample(result);
        }

    }


    private void createSamples(String regionInfo) {
        try {
            JSONObject reader = new JSONObject(regionInfo);
            Iterator iterator = reader.keys();
            while(iterator.hasNext()){
                String key = (String)iterator.next();
                String url = ENDPOINT + "?sound=" + key;
                new getSampleInfo().execute(url);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSingleSample(String sampleInfo) {
        Sample sample = Sample.createSampleFromJson(sampleInfo);
        if (sample != null) {
            addSample(sample);

           /* Log.i(TAG,"Sample id: " + sample.getId());
            Log.i(TAG,"Sample name: " + sample.getName());
            Log.i(TAG,"Sample version: " + sample.getVersion());
            Log.i(TAG,"Sample sampleId: " + sample.getUrl());*/
        }
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
     }

    public void playSample(int sampleId) {
        playSample(sampleId, true);
    }


    public void playSample(int sampleId, boolean setLooping) {

        stop();

        if(!mSamples.containsKey(sampleId)){
            Log.e(TAG,"PlaySample-> No key found with Id: " + sampleId);
            mCurrentSample = null;
            return;
        }

        try {
            mCurrentSample = mSamples.get(sampleId);
            String uri = SAMPLES_ABSOLUTE_PATH + mCurrentSample.getName();
            mPlayer = new MediaPlayer();
            mPlayer.setLooping(setLooping);
            mPlayer.setDataSource(uri);
            mPlayer.prepare();

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });

            mPlayer.start();

        } catch (Exception e) {
            mCurrentSample = null;
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
    }

    public void addDefaultSample(){

        int id = -1; //The default sample is the non valid -1
        double version = 1;
        String name = DEFAULT_SAMPLE_NAME;

        BasicElement basicElement = new BasicElement(id,name,version);
        Sample sample = new Sample(basicElement);
        addSample(sample);
    }


    public void addSample(Sample sample) {
        if(sample==null){
            return;
        }

        Log.i(TAG,"Added Sample: " + sample.getId() + ", " + sample.getName());
        mSamples.put(sample.getId(), sample);

        if(mDBManager!=null){
            mDBManager.insertSample(sample);
        }
    }

    public Sample getSampleFromId(int sampleId){
        if(mSamples.containsKey(sampleId)){
            return mSamples.get(sampleId);
        }

        return null;
    }

    public Sample getCurrentSample() {
        return mCurrentSample;
    }
}
