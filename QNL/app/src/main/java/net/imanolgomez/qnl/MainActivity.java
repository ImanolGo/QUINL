package net.imanolgomez.qnl;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class MainActivity extends Activity {

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

        Log.i("MainActivity", "Initialize QNL Service");

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(QnlService.ON_UPDATE_LOCATION);

        Log.i("MainActivity", "Start Service");
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
            Log.i("MainActivity", "onReceive");

            String action = intent.getAction();
            if(action.equalsIgnoreCase(QnlService.ON_UPDATE_LOCATION)){
                Log.i("MainActivity", "ON_UPDATE_LOCATION");
                updateTextLabels();
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
