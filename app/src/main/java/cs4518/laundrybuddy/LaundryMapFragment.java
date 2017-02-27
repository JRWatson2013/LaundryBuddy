package cs4518.laundrybuddy;

import android.*;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
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


public class LaundryMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener{

    private GoogleMap mMap;
    public GoogleApiClient mApiClient;
    private Button mLaundromatButton;
    private Map<String,LaundryLocation> markerLaundyMap;
    private List<Geofence> mGeofenceList = new ArrayList<Geofence>();
    private PendingIntent mGeofencePendingIntent;
    private static View view;


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
                LaundryLocation thisLocation = markerLaundyMap.get(marker.getId());
                View v = getActivity().getLayoutInflater().inflate(R.layout.info_window, null);
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
        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getMaxZoomLevel() - 6));

        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(getContext());

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
        LaundryLocation loc;
        loc = markerLaundyMap.get(marker.getId());
        Intent i = new Intent(getActivity(), LaundromatActivity.class);
        i.putExtra("location",loc);
        startActivity(i);
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
