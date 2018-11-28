package com.surya.smoothmarkermovement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.surya.smoothmarkermovement.LocationUtils.LocationUtil;
import com.surya.smoothmarkermovement.LocationUtils.PicassoMarker;

public class MainActivity extends AppCompatActivity implements LocationUtil.GetLocationListener {

    private GoogleMap googleMapHomeFrag;
    private double[] latLng = new double[2];
    LatLng driverLatLng;
    private PicassoMarker marker;
    private LocationUtil locationUtilObj;
    private final int REQUEST_CODE_PERMISSION_MULTIPLE = 123;
    private boolean isDeninedRTPs = true;       // initially true to prevent anim(2)
    private boolean showRationaleRTPs = false;
    private float start_rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestRunTimePermissions();
        getCurrentLocation();
        //locationUtilObj = new LocationUtil(this, this);
        initMap();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getCurrentLocation();
        if (locationUtilObj != null /*&& !locationUtilObj.isGoogleAPIConnected()*/) {
            locationUtilObj.checkLocationSettings();
            locationUtilObj.restart_location_update();
        }
    }

    private void initMap() {
        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (googleMap != null) {
                    googleMapHomeFrag = googleMap;
                    googleMapHomeFrag.getUiSettings().setAllGesturesEnabled(true);
                    googleMapHomeFrag.getUiSettings().setScrollGesturesEnabled(true);
                    googleMapHomeFrag.getUiSettings().setCompassEnabled(false);
                    googleMapHomeFrag.getUiSettings().setMapToolbarEnabled(false);

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    googleMapHomeFrag.setMyLocationEnabled(false);
                    googleMapHomeFrag.getUiSettings().setMyLocationButtonEnabled(true);

                    if(driverLatLng!=null){
                        if (googleMapHomeFrag != null) {
                            googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 12.0f));
                            googleMapHomeFrag.getUiSettings().setZoomControlsEnabled(false);
                        }
                    }


                }
            }
        });
    }

    private void getCurrentLocation() {
        if (locationUtilObj == null) {
            locationUtilObj = new LocationUtil(this, this);
        } else {
            locationUtilObj.checkLocationSettings();
        }
    }

    @Override
    public void updateLocation(Location location) {
        latLng[0] = location.getLatitude();
        latLng[1] = location.getLongitude();

        if(marker==null){
            marker = new PicassoMarker(googleMapHomeFrag.addMarker(new MarkerOptions().position(new LatLng(latLng[0], latLng[1]))));
            Picasso.with(MainActivity.this).load(R.mipmap.car).resize( 50,  50)
                    .into(marker);
            googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng[0], latLng[1]), 12.0f));
        }

        if ((latLng[0] != -1 && latLng[0] != 0) && (latLng[1] != -1 && latLng[1] != 0)) {
            //googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 12.0f));
            //float bearing = (float) bearingBetweenLocations(driverLatLng, new LatLng(location.getLatitude(), location.getLongitude()));
            if (marker != null) {
                moveVechile(marker.getmMarker(), location);
                rotateMarker(marker.getmMarker(), location.getBearing(), start_rotation);
            }
            driverLatLng = new LatLng(latLng[0], latLng[1]);
        } else {
            Toast.makeText(this, "Location Not Found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void location_Error(String error) {
        Log.e("LOCATIO_ERROR",error);
    }

    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    /**
     * custom method to check and request for run time permissions
     * if not granted already
     */
    private void checkAndRequestRunTimePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+

            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_PERMISSION_MULTIPLE);

            }
        }


        onRunTimePermissionGranted();
    }
    /******************************************************/
    /**
     * custom method to execute after run time permissions
     * granted or if it run time permission no required at all
     */
    private void onRunTimePermissionGranted() {

        isDeninedRTPs = false;
        getCurrentLocation();
    }
    /******************************************************/

    /**
     * predefined method to check run time permissions list call back
     *
     * @param requestCode   : to handle the corresponding request
     * @param permissions:  contains the list of requested permissions
     * @param grantResults: contains granted and un granted permissions result list
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_MULTIPLE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        isDeninedRTPs = true;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showRationaleRTPs = shouldShowRequestPermissionRationale(permission);
                        }

                        break;
                    }

                }
                onRunTimePermissionDenied();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /******************************************************/

    private void onRunTimePermissionDenied() {
        if (isDeninedRTPs) {
            if (!showRationaleRTPs) {
                //goToSettings();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_PERMISSION_MULTIPLE);
                }
            }
        } else {
            onRunTimePermissionGranted();
        }
    }

    public void moveVechile(final Marker myMarker, final Location finalPosition) {

        final LatLng startPosition = myMarker.getPosition();

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + (finalPosition.getLatitude()) * t,
                        startPosition.longitude * (1 - t) + (finalPosition.getLongitude()) * t);
                myMarker.setPosition(currentPosition);
                // myMarker.setRotation(finalPosition.getBearing());


                // Repeat till progress is completeelse
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                    // handler.postDelayed(this, 100);
                } else {
                    if (hideMarker) {
                        myMarker.setVisible(false);
                    } else {
                        myMarker.setVisible(true);
                    }
                }
            }
        });


    }


    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;


                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                start_rotation = -rot > 180 ? rot / 2 : rot;
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

}
