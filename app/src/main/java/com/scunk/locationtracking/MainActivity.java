package com.scunk.locationtracking;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    /* add variables to associate them with text views and buttons */
    private TextView latitudeTV;
    private TextView longitudeTV;
    private TextView accuracyTV;
    private TextView speedTV;


    /* create an instance of a client */
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int MY_PERMISSION_FINE_LOCATION = 101;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean updateOn = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        /* assign values to variables */
        latitudeTV = findViewById(R.id.tvLatitude);
        longitudeTV = findViewById(R.id.tvLongitude);
        accuracyTV = findViewById(R.id.tvAccuracy);
        speedTV = findViewById(R.id.tvSpeed);


        /*
        https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest --> info on priority
        https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#setInterval(long) --> info on set priority intervals in miliseconds
        */
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // 1 second for update intervals
        locationRequest.setFastestInterval(500);


        // https://developer.android.com/training/permissions/requesting#java  --> explanation of accessing and handling permissions
        // created fusedlocation provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        /* added permission check */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // if location permission is granted, update location details
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                       //fusedLocationProviderClient.requestLocationUpdates
                        latitudeTV.setText(String.valueOf(location.getLatitude()));
                        longitudeTV.setText(String.valueOf(location.getLongitude()));
                        accuracyTV.setText(String.valueOf(location.getAccuracy()));

                        //String Latitude = latitude.getText().toString();
                        //String Longitude = longitude.getText().toString();


                        if (location.hasSpeed()) {
                            speedTV.setText(String.valueOf(location.getSpeed() + "m/s"));
                        } else {
                            speedTV.setText("No speed available");
                        }
                        postToDb();

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

        //when location is changed
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    //Update UI with location data
                    if (location != null) {
                        latitudeTV.setText(String.valueOf(location.getLatitude()));
                        longitudeTV.setText(String.valueOf(location.getLongitude()));
                        accuracyTV.setText(String.valueOf(location.getAccuracy()));


                        //String latitude = String.valueOf(location.getLatitude());
                        //String longitude = String.valueOf(location.getLongitude());

                        if (location.hasSpeed()) {
                            speedTV.setText(String.valueOf(location.getSpeed() + "m/s"));
                        } else {
                            speedTV.setText("No speed");
                        }
                        //postToDb(latitude, longitude);
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


// https://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void postToDb() {

        try {

            URL url = new URL("http://localhost/android_connect/backend.php");

            String charset = StandardCharsets.UTF_8.name();
            String latitude = "value1";
            String longitude = "value2";

            String query = String.format("latitude=%1$s&longitude=%2$s",
                    URLEncoder.encode(latitude, charset),
                    URLEncoder.encode(longitude, charset));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true); // Triggers POST.
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", charset);
            //connection.setRequestProperty("Key", "Value");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
            connection.setFixedLengthStreamingMode(query.getBytes(charset).length);
            connection.setChunkedStreamingMode(0);

            //connection.connect();

            //try with resources
            try (OutputStream outputPost = connection.getOutputStream()) {

                //BufferedOutputStream outputPost = new BufferedOutputStream(outputStream);

                outputPost.write(query.getBytes(charset));
                outputPost.flush();
                outputPost.close();

                InputStream in = new BufferedInputStream(connection.getInputStream());
                in.read();
            }
            //OutputStream outputPost = new BufferedOutputStream(connection.getOutputStream());
            //writeStream(outputPost);

           // outputPost.flush();
           // outputPost.close();

        } catch (Exception e) {
            //System.out.println(e);
            // some networking error
        }
    };



  /*  private void writeStream(OutputStream outputPost){
        String output = "Hello world";

        outputPost.write(output.getBytes());
        outputPost.flush();
    }; */



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
        //if On-Off button is on, location updates start
        if (updateOn) startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //location updates are enabled
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

    /*private void postToDb() {

        try {

            /*

            // Create a new HttpClient and Post request
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://localhost/android_connect/backend.php");

            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("latitude", "test1"));
            nameValuePairs.add(new BasicNameValuePair("longitude", "test 2"));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {

                String responseStr = EntityUtils.toString(resEntity).trim();
                Log.v("MainActivity.java", "Response: " +  responseStr);

                // you can add an if statement here and do other actions based on the response
            }

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }; */
