package cs4518.laundrybuddy;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andy on 2/21/17.
 */

public class LaundryLocation {
    private String mName;
    private String mID;
    private LatLng mLocation;
    private int mBusy;
    private int mNumWashers;
    private int mNumDryers;

    LaundryLocation(String name, String ID, LatLng location){
        mName = name;
        mID = ID;
        mLocation = location;
    }

    LaundryLocation(JSONObject obj){
        try {
            mName = obj.getString("name");
            mID = obj.getString("id");
            this.setLocation(obj.getJSONObject("geometry").getJSONObject("location"));
        } catch (JSONException e) {
            Log.v("LG", "Could not convert JSON to LaundryLocation");
        }
    }

    LaundryLocation(String name, String ID, JSONObject location){
        mName = name;
        mID = ID;
        this.setLocation(location);
    }

    void setNumDryers(int n){
        mNumDryers = n;
    }

    int getNumDryers(){
        return mNumDryers;
    }

    void setNumWashers(int n){
        mNumWashers = n;
    }

    int getNumWashers(){
        return mNumWashers;
    }

    int getBusy(){
        return mBusy;
    }

    void setBusy(int busyness){
        mBusy = busyness;
    }

    String getID(){
        return mID;
    }

    String getName(){
        return mName;
    }

    LatLng getLocation(){
        return mLocation;
    }

    void setLocation(LatLng location){
        mLocation = location;
    }
    void setLocation(JSONObject location){
        try {
            mLocation = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
        } catch (JSONException e){
            Log.v("LG","Bad Location JSONObject");
            return;
        }
    }
}
