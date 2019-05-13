package com.zhuravlevmikhail.rittest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.capur16.digitspeedviewlib.DigitSpeedView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DisplayActivity extends AppCompatActivity implements LocationListener {

    private static final String MIRROR_INDEX = "mirroring";
    private static final String SPEED_MODE_INDEX = "speed mode";
    private static final String SPEED_INDEX = "speed";
    private static final String STATUS_INDEX = "status";
    private static final String DISTANCE_INDEX = "distance";
    private static final String LAT_1_INDEX = "lat 1";
    private static final String LAT_2_INDEX = "lat 2";
    private static final String LONG_1_INDEX = "long 1";
    private static final String LONG_2_INDEX = "long 2";

    private Location mPreLocation;
    private boolean mIsMirroring;
    private boolean mIsAnalogMode;
    private int mSpeed;
    private static int status;
    private static Double lat1 = null;
    private static Double lon1 = null;
    private static Double lat2 = null;
    private static Double lon2 = null;
    private static Double distance = 0.0;


    @BindView(R.id.analog_speed_view)
    AwesomeSpeedometer mAnalogSpeedView;
    @BindView(R.id.digit_speed_view)
    DigitSpeedView mDigitSpeedView;
    @BindView(R.id.distance_tv)
    TextView mDistance;
    @BindView(android.R.id.content)
    View mContentView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mIsMirroring = savedInstanceState.getBoolean(MIRROR_INDEX);
            mIsAnalogMode = savedInstanceState.getBoolean(SPEED_MODE_INDEX);
            mSpeed = savedInstanceState.getInt(SPEED_INDEX);
            status = savedInstanceState.getInt(STATUS_INDEX);
            distance = savedInstanceState.getDouble(DISTANCE_INDEX);
            lon1 = savedInstanceState.getDouble(LONG_1_INDEX);
            lon2 = savedInstanceState.getDouble(LONG_2_INDEX);
            lat1 = savedInstanceState.getDouble(LAT_1_INDEX);
            lat2 = savedInstanceState.getDouble(LAT_2_INDEX);
        }

        if (mIsMirroring) {
            mContentView.setScaleY(-1);
        } else mContentView.setScaleY(1);

        if (mIsAnalogMode) {
            mAnalogSpeedView.setVisibility(View.VISIBLE);
        } else mDigitSpeedView.setVisibility(View.VISIBLE);

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.onLocationChanged(null);
    }

    @OnClick(R.id.mirror_fab)
    public void onMirrorClick() {
        if (mIsMirroring) {
            mContentView.setScaleY(1);
            mIsMirroring = false;
        } else {
            mContentView.setScaleY(-1);
            mIsMirroring = true;
        }
    }

    @OnClick(R.id.switch_speed_mode_fab)
    public void onSwitchSpeedModeClick() {
        if (mIsAnalogMode) {
            mAnalogSpeedView.setVisibility(View.INVISIBLE);
            mDigitSpeedView.setVisibility(View.VISIBLE);
            mIsAnalogMode = false;
        } else {
            mAnalogSpeedView.setVisibility(View.VISIBLE);
            mDigitSpeedView.setVisibility(View.INVISIBLE);
            mIsAnalogMode = true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (mPreLocation == null) mPreLocation = location;
            mSpeed = (int) ((location.getSpeed() * 3600) / 1000);
            mAnalogSpeedView.speedTo(mSpeed);
            mDigitSpeedView.updateSpeed(mSpeed);
            if (status == 0) {
                lat1 = location.getLatitude();
                lon1 = location.getLongitude();
                Log.d("Distance", "LAT1 = " + lat1 + " LON1 = " + lon1);
            } else if ((status % 2) != 0) {
                lat2 = location.getLatitude();
                lon2 = location.getLongitude();
                distance += Math.floor(location.distanceTo(mPreLocation));
                Log.d("Distance", String.valueOf(location.distanceTo(mPreLocation)));
            } else if ((status % 2) == 0) {
                lat1 = location.getLatitude();
                lon1 = location.getLongitude();
                distance += Math.floor(location.distanceTo(mPreLocation));
                Log.d("Distance", String.valueOf(location.distanceTo(mPreLocation)));
            }
            status++;
            Double desDistance = (double) (Math.round((distance / 1000) * 10) / 10);
            String strDistance = String.valueOf(desDistance);
            mDistance.setText(strDistance);
            mPreLocation = location;
        }
    }

    // Does not work correctly
    double distanceBetweenTwoPoint(double srcLat, double srcLng, double desLat, double desLng) {
        double earthRadius = 6371;
        double dLat = (desLat - srcLat) * (Math.PI / 180);
        double dLng = (desLng - srcLng) * (Math.PI / 180);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(srcLat))
                * Math.cos(Math.toRadians(desLat)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Get distance in km
        return earthRadius * c;
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "GPS is disabled", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SPEED_INDEX, mSpeed);
        outState.putInt(STATUS_INDEX, status);
        outState.putBoolean(MIRROR_INDEX, mIsMirroring);
        outState.putBoolean(SPEED_MODE_INDEX, mIsAnalogMode);
        outState.putDouble(DISTANCE_INDEX, distance);
        try {
            outState.putDouble(LAT_1_INDEX, lat1);
            outState.putDouble(LAT_2_INDEX, lat2);
            outState.putDouble(LONG_1_INDEX, lon1);
            outState.putDouble(LONG_1_INDEX, lon2);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
