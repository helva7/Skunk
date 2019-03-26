package com.scunk.locationtracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {


    /* add variables to associate them with text views and buttons */
    private TextView latitude;
    private TextView longitude;
    private TextView accuracy;
    private TextView speed;
    private TextView sensorType;
    private TextView updatesOnOff;
    private ToggleButton switchOffGPS;
    private ToggleButton locationOnOff;
    /* create an instance of a client */
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int MY_PERMISSION_FINE_LOCATION = 101;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean updateOn = false; // REMOVE later on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* assign values to variables */
        latitude = findViewById(R.id.tvLatitude);
        longitude = findViewById(R.id.tvLongitude);
        accuracy = findViewById(R.id.tvAccuracy);
        speed = findViewById(R.id.tvSpeed);
        sensorType = findViewById(R.id.tvSensor);
        updatesOnOff = findViewById(R.id.tvUpdates);
        switchOffGPS = findViewById(R.id.tbGPS);
        locationOnOff = findViewById(R.id.tbLocationUpdates);

        // https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000); // 5 seconds for update intervals
        locationRequest.setFastestInterval(4000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        /* set up listeners for toggle buttons*/
        switchOffGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //using GPS only
                if (switchOffGPS.isChecked()) {
                    sensorType.setText("GPS");
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                } else {
                    //using cell towers and wifi
                    sensorType.setText("Cell Towers and WiFi");
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                }
            }
        });

        locationOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationOnOff.isChecked()) {
                    //location updates are on
                    updatesOnOff.setText("On");
                    updateOn = true;
                    startLocationUpdates();
                } else {
                    //location updates are off
                    updatesOnOff.setText("Off");
                    updateOn = false;
                    stopLocationUpdates();
                }
            }
        });

        // https://developer.android.com/training/permissions/requesting#java  --> explanation of accessing and handling permissions
        // created fusedlocation provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        /* added permission check */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // if location permission is granted, update location details
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                       //fusedLocationProviderClient.requestLocationUpdates
                        latitude.setText(String.valueOf(location.getLatitude()));
                        longitude.setText(String.valueOf(location.getLongitude()));
                        accuracy.setText(String.valueOf(location.getAccuracy()));
                        if (location.hasSpeed()) {
                            speed.setText(String.valueOf(location.getSpeed() + "m/s"));
                        } else {
                            speed.setText("No speed available");
                        }
                    }
                }
            });
        } else {
            /*
            reguest permissions to proceed with application functioning
            runtime version check as some lower versions don't require permission check
            */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
            }
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    //Update UI with location data
                    if (location != null) {
                        latitude.setText(String.valueOf(location.getLatitude()));
                        longitude.setText(String.valueOf(location.getLongitude()));
                        accuracy.setText(String.valueOf(location.getAccuracy()));
                        if (location.hasSpeed()) {
                            speed.setText(String.valueOf(location.getSpeed() + "m/s"));
                        } else {
                            speed.setText("No speed available");
                        }
                    }

                }
            }
        };


        FloatingActionButton fab;
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /* permission was granted, do nothing and carry on */
                } else {
                    Toast.makeText(getApplicationContext(), "This app requires location permission to be granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (updateOn) startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
