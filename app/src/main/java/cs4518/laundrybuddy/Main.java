package cs4518.laundrybuddy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Main extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    public GoogleApiClient mApiClient;
    private Button mLaundromatButton;
    private Map<String,LaundryLocation> markerLaundyMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        mLaundromatButton = (Button) findViewById(R.id.laundromat_test_button);
        mLaundromatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Main.this, LaundromatActivity.class);
                startActivity(i);
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
        if(mLastLocation != null){
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getMaxZoomLevel() - 5));
        }
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                LaundryLocation thisLocation = markerLaundyMap.get(marker.getId());
                View v = getLayoutInflater().inflate(R.layout.info_window, null);
                TextView title =(TextView) v.findViewById(R.id.info_window_title);
                title.setText(thisLocation.getName());
                TextView washers =(TextView) v.findViewById(R.id.info_window_washers);
                TextView dryers =(TextView) v.findViewById(R.id.info_window_dryers);
                TextView busy =(TextView) v.findViewById(R.id.info_window_busy);
                washers.setText(thisLocation.getWashersInUse() + "/" + thisLocation.getNumWashers() + " washers");
                dryers.setText(thisLocation.getDryersInUse() + "/" + thisLocation.getNumDryers() + " dryers");
                busy.setText(thisLocation.getBusy() + " busy");
                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        markerLaundyMap = new HashMap<String, LaundryLocation>();
    }

    public void goToInstr(View v) {
        Intent mInstrIntent = new Intent(Main.this, Instructions.class);
        startActivity(mInstrIntent);
    }

    public void goToTimer(View v) {
        Intent mTimerIntent = new Intent(Main.this, NumberPickerActivity.class);
        startActivity(mTimerIntent);
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//            LocationRequest mLocationRequest = LocationRequest.create()
//                    .setInterval(5000)
//                    .setFastestInterval(3000)
//                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//            LocationServices.FusedLocationApi.requestLocationUpdates(
//                    mApiClient, mLocationRequest, this);
//        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
        initMap(mLastLocation);
    }

    @Override
    public void onConnectionSuspended (int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
//                LocationRequest mLocationRequest = LocationRequest.create()
//                        .setInterval(5000)
//                        .setFastestInterval(3000)
//                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//                LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
                initMap(mLastLocation);
            }
        }
    }
    public void initMap(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.curlocation));
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getMaxZoomLevel() - 6));

        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(this);

        String requestURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyCp0KMvz_OD8nDFZ1dcAJU9s4kOwVjqjUg&location=" + latLng.latitude + "," + latLng.longitude + "&rankby=distance&type=laundry";
        Log.v("LB",requestURL);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){
                        Log.v("LB",response.toString());
                        JSONArray results;
                        try {
                            results = response.getJSONArray("results");
                        } catch (JSONException e) {
                            Log.v("LG:", "No results section");
                            return;
                        }
                        for(int i = 0; i < results.length(); i ++){
                            LaundryLocation loc;
                            try{
                                loc = new LaundryLocation(results.getJSONObject(i));
                            } catch (JSONException e){
                                Log.v("LG:", "Bad result");
                                continue;
                            }

                            // Get info from LaundryBuddy server
                            loc.getInfoFromLaundryBuddy(queue);

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(loc.getLocation());
                            markerOptions.title(loc.getName());
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            markerOptions.snippet("30 Washers\n40 Dryers\nVery Busy");
                            String id = mMap.addMarker(markerOptions).getId();
                            markerLaundyMap.put(id,loc);
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.v("LB","NO RESPONSE");
                    }
                });
        queue.add(request);
    }
}
