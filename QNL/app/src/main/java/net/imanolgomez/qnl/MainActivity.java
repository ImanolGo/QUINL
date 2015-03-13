package net.imanolgomez.qnl;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    public static String TAG = "MainActivity";

    // Handles to UI widgets
    private TextView mLatLng;
    private TextView mAccuracyView;
    private TextView mRegionText;
    private TextView mRouteText;
    private TextView mBeaconText;
    private TextView mSampleText;

    // Declaring a Location Manager
    protected QnlService m_qnlService;
    // Declaring a Map Manager
    protected MapManager mMapManager;

    //Broadcast receiver
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initialize();
    }

    protected void initialize(){
        this.initializeQnlService();
        this.initializeManager();
        this.initializeViews();

    }

    protected void initializeManager(){
        mMapManager = MapManager.get(getFragmentManager());
    }

    protected void initializeQnlService(){

        Log.i(TAG, "Initialize QNL Service");

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(QnlService.ON_UPDATE_LOCATION);
        mIntentFilter.addAction(QnlService.ON_POWER_CONNECTED);
        mIntentFilter.addAction(QnlService.ON_POWER_DISCONNECTED);

        Log.i(TAG, "Start Service");
        Intent serviceIntent = new Intent(this, QnlService.class);
        startService(serviceIntent);
    }

    protected void initializeViews(){
        // Get handles to the UI view objects
        mLatLng = (TextView) findViewById(R.id.lat_lng);
        mAccuracyView = (TextView) findViewById(R.id.accuracy);
        mRouteText = (TextView) findViewById(R.id.route);
        mRegionText = (TextView) findViewById(R.id.region);
        mSampleText = (TextView) findViewById(R.id.sample);
        mBeaconText = (TextView) findViewById(R.id.beacon);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive");

            String action = intent.getAction();
            if(action.equalsIgnoreCase(QnlService.ON_UPDATE_LOCATION)){
                Log.i(TAG, "ON_UPDATE_LOCATION");
                updateTextLabels();
            }

            else if(action.equalsIgnoreCase(QnlService.ON_POWER_CONNECTED)){
                Log.i(TAG, "ON_POWER_CONNECTED");
                mRegionText.setText("POWER_CONNECTED");
            }

            else if(action.equalsIgnoreCase(QnlService.ON_POWER_DISCONNECTED)){
                Log.i(TAG, "ON_POWER_DISCONNECTED");
                mRegionText.setText("POWER_DISCONNECTED");
            }

            else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);

                Toast toast = Toast.makeText(MainActivity.this,"Finished Downloading " + downloadId, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();

            }
        }
    };

    private void updateTextLabels(){

        SoundManager soundManager = SoundManager.get(this);
        QnlLocationManager qnlLocationManager= QnlLocationManager.get(this);

        // In the UI, set the latitude and longitude to the value received
        mLatLng.setText(LocationUtils.getLatLng(this, qnlLocationManager.getCurrentLocation()));
        mAccuracyView.setText(LocationUtils.getAccuracy(this, qnlLocationManager.getCurrentLocation()));
        mRouteText.setText(LocationUtils.getRoute(this,qnlLocationManager.getCurrentRoute()));
        mBeaconText.setText(LocationUtils.getBeacon(this,qnlLocationManager.getCurrentSpot()));
        mRegionText.setText(LocationUtils.getRegion(this,qnlLocationManager.getCurrentRegion()));
        mSampleText.setText(LocationUtils.getSample(this,soundManager.getCurrentSample()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,QnlService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}
