package cs4518.laundrybuddy;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class LaundryMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener{

    private GoogleMap mMap;
    public GoogleApiClient mApiClient;
    private Button mLaundromatButton;
    public static Map<String,LaundryLocation> markerLaundryMap;
    private List<Geofence> mGeofenceList = new ArrayList<Geofence>();
    private PendingIntent mGeofencePendingIntent;
    private static View view;
    private Marker myPositionMarker;
    public static RequestQueue queue;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            //SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(getContext());

        mApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
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
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                LaundryLocation thisLocation = markerLaundryMap.get(marker.getId());
                View v = getActivity().getLayoutInflater().inflate(R.layout.info_window, null);
                TextView title =(TextView) v.findViewById(R.id.info_window_title);
                TextView washers =(TextView) v.findViewById(R.id.info_window_washers);
                TextView dryers =(TextView) v.findViewById(R.id.info_window_dryers);
                TextView busy =(TextView) v.findViewById(R.id.info_window_busy);
                if(myPositionMarker.equals(marker)) {
                    title.setText("Current Location");
                    washers.setVisibility(View.GONE);
                    dryers.setVisibility(View.GONE);
                    busy.setVisibility(View.GONE);
                    return v;
                }
                title.setText(thisLocation.getName());
                Integer washersAvailableNow = thisLocation.getNumWashers() - thisLocation.getWashersInUse() - thisLocation.getCheckInCount();
                washers.setText(washersAvailableNow + "/" + thisLocation.getNumWashers() + " washers available");
                Integer dryersAvailableNow = thisLocation.getNumDryers() - thisLocation.getDryersInUse() - thisLocation.getCheckInCount();
                dryers.setText(dryersAvailableNow + "/" + thisLocation.getNumDryers() + " dryers available");

                if(thisLocation.getCheckInCount()==0){
                    busy.setTextColor(Color.GREEN);
                    busy.setText("Not Busy");
                } else if (thisLocation.getCheckInCount()<3){
                    busy.setTextColor(Color.rgb(0xFF,0x60,0x00));
                    busy.setText("Moderately Busy");
                } else {
                    busy.setTextColor(Color.RED);
                    busy.setText("Very Busy");
                }
                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        markerLaundryMap = new HashMap<String, LaundryLocation>();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
        myPositionMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getMaxZoomLevel() - 6));

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
                            if(loc.getCheckInCount()==0){
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            } else if (loc.getCheckInCount()<3){
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            } else {
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            }
                            markerOptions.snippet("30 Washers\n40 Dryers\nVery Busy");
                            String id = mMap.addMarker(markerOptions).getId();
                            markerLaundryMap.put(id,loc);
                            mGeofenceList.add(new Geofence.Builder()
                                    .setRequestId(loc.getID())
                                    .setCircularRegion(
                                            loc.getLocation().latitude,
                                            loc.getLocation().longitude,
                                            50)
                                    .setExpirationDuration(300000)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                            Geofence.GEOFENCE_TRANSITION_EXIT)
                                    .build());
                        }
                        LocationServices.GeofencingApi.removeGeofences(
                                mApiClient, getGeofencePendingIntent()
                        );
                        LocationServices.GeofencingApi.addGeofences(
                                mApiClient, getGeofencingRequest(), getGeofencePendingIntent()
                        );
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

    @Override
    public void onInfoWindowClick(Marker marker){
        if(marker.equals(myPositionMarker))
            return;
        LaundryLocation loc;
        loc = markerLaundryMap.get(marker.getId());
        Intent i = new Intent(getActivity(), LaundromatActivity.class);
        i.putExtra("location",loc);
        startActivityForResult(i,9001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 9001) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                for(LaundryLocation l : markerLaundryMap.values()){
                    l.getInfoFromLaundryBuddy(queue);
                }
            }
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getActivity(), GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
