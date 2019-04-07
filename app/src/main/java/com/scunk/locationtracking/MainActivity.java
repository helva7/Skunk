package com.scunk.locationtracking;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static android.support.constraint.Constraints.TAG;


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

    private String latitude;
    private String longitude;

    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestQueue = Volley.newRequestQueue(this);


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
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());

                        if (location.hasSpeed()) {
                            speedTV.setText(String.valueOf(location.getSpeed() + "m/s"));
                        } else {
                            speedTV.setText("No speed available");
                        }
                        //postToDb();
                        //postToDb2(latitude, longitude);
                        postToDb3(latitude, longitude);
                        //new MyHttpRequestTask().execute(latitude,longitude);
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
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    //Update UI with location data
                    if (location != null) {
                        latitudeTV.setText(String.valueOf(location.getLatitude()));
                        longitudeTV.setText(String.valueOf(location.getLongitude()));
                        accuracyTV.setText(String.valueOf(location.getAccuracy()));

                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());

                        if (location.hasSpeed()) {
                            speedTV.setText(String.valueOf(location.getSpeed() + "m/s"));
                        } else {
                            speedTV.setText("No speed");
                        }
                        //postToDb();
                        //new MyHttpRequestTask().execute(latitude,longitude);
                        //postToDb3(latitude, longitude);
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

            URL url = new URL("http://192.168.0.14/android_connect/backend.php");

            String charset = StandardCharsets.UTF_8.name();
            String latitude = "value1";
            String longitude = "value2";

            String query = String.format("%1$s:%2$s",
                    URLEncoder.encode(latitude, charset),
                    URLEncoder.encode(longitude, charset));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true); // Triggers POST.
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", charset);
            //connection.setRequestProperty("Key", "Value");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);

            connection.setRequestProperty("Content-Length", Integer.toString(query.getBytes(charset).length));

            connection.setFixedLengthStreamingMode(query.getBytes(charset).length);
            connection.setChunkedStreamingMode(0);

            //connection.connect();

            //try with resources
            try (OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream())) {

                OutputStreamWriter outputPost = new OutputStreamWriter(outputStream);
                outputPost.write(query);
                outputPost.flush();
                outputPost.close();
            }
        }  catch (Exception e) {
                e.printStackTrace();
        }
    };



    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void postToDb2(String latitude, String longitude) {
        try {

            URL url = new URL("http://192.168.0.14/android_connect/backend.php");

            String charset = StandardCharsets.UTF_8.name();

            String query = String.format("%1$s:%2$s",
                    URLEncoder.encode(latitude, charset),
                    URLEncoder.encode(longitude, charset));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true); // Triggers POST.
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", charset);
            //connection.setRequestProperty("Key", "Value");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);

            connection.setRequestProperty("Content-Length", Integer.toString(query.getBytes(charset).length));

            connection.setFixedLengthStreamingMode(query.getBytes(charset).length);
            connection.setChunkedStreamingMode(0);

            //connection.connect();

            //try with resources
            try (OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream())) {

                OutputStreamWriter outputPost = new OutputStreamWriter(outputStream);
                outputPost.write(query);
                outputPost.flush();
                outputPost.close();
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }
    };


    private void postToDb3(final String latitude, final String longitude) {

        String url = "http://192.168.0.14/android_connect/backend.php";

        StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> jsonLatLong = new HashMap<>();

                jsonLatLong.put("latitude", latitude);
                jsonLatLong.put("longitude", longitude);
                return jsonLatLong;
            }
        };

        requestQueue.add(strRequest);

    };



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

class MyHttpRequestTask extends AsyncTask<String,Integer,String> {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... params) {
        String latitude = params[0];
        String longitude = params[1];
        String query = null;
        query = String.format("%1$s:%2$s", latitude, longitude);

        try {
            URL url = new URL("http://192.168.0.14/android_connect/backend.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            // setting the  Request Method Type
            httpURLConnection.setRequestMethod("POST");
            // adding the headers for request
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            try{
                //to tell the connection object that we will be wrting some data on the server and then will fetch the output result
                httpURLConnection.setDoOutput(true);
                // this is used for just in case we don't know about the data size associated with our request
                httpURLConnection.setChunkedStreamingMode(0);

                // to write tha data in our request
                OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                outputStreamWriter.write(query);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                // to log the response code of your request
                Log.d(TAG, "MyHttpRequestTask doInBackground : " +httpURLConnection.getResponseCode());
                // to log the response message from your server after you have tried the request.
                Log.d(TAG, "MyHttpRequestTask doInBackground : " +httpURLConnection.getResponseMessage());


            }catch (Exception e){
                e.printStackTrace();
            }finally {
                // this is done so that there are no open connections left when this task is going to complete
                httpURLConnection.disconnect();
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}

