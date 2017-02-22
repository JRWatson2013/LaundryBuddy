package cs4518.laundrybuddy;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.instantapps.LaunchData;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andy on 2/21/17.
 */

public class LaundryLocation implements Parcelable {
    private String mName;
    private String mID;
    private LatLng mLocation;
    private int mBusy;
    private int mNumWashers;
    private int mNumDryers;
    private int mWashersInUse;
    private int mDryersInUse;
    private int mCheckInCount;

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

    public int getCheckInCount() {
        return mCheckInCount;
    }

    public void setCheckInCount(int checkInCount) {
        this.mCheckInCount = checkInCount;
    }

    public int getDryersInUse() {
        return mDryersInUse;
    }

    public void setDryersInUse(int mDryersInUse) {
        this.mDryersInUse = mDryersInUse;
    }

    public int getWashersInUse() {
        return mWashersInUse;
    }

    public void setWashersInUse(int mWashersInUse) {
        this.mWashersInUse = mWashersInUse;
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

    void getInfoFromLaundryBuddy(RequestQueue queue){

        String url = "http://130.215.251.227:8080/getPlaceInfo";
        Map<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("location",this.getID());
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST,url,
                new JSONObject(jsonParams),
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.v("LG",response.toString());
                        String returnedID = "";
                        try {
                            returnedID = response.getString("locationID");
                        } catch (JSONException e){
                            Log.v("LG","Error parsing out locationID");
                        }
                        if(returnedID == "-1" || returnedID == ""){
                            setNumWashers(-1);
                            setNumDryers(-1);
                            setDryersInUse(-1);
                            setWashersInUse(-1);
                            setCheckInCount(-1);
                            return;
                        }
                        try {
                            setNumWashers(response.getInt("washerCount"));
                            setNumDryers(response.getInt("dryerCount"));
                            setDryersInUse(response.getInt("dryersInUse"));
                            setWashersInUse(response.getInt("washersInUse"));
                            setCheckInCount(response.getInt("checkInCount"));
                        } catch (JSONException e){
                            Log.v("LG","Error parsing placeInfo JSON object");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("LG","Error getting location info from LaundryBuddy server");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", System.getProperty("http.agent"));
                return headers;
            }
        };
        queue.add(postRequest);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mID);
        parcel.writeDouble(mLocation.latitude);
        parcel.writeDouble(mLocation.longitude);
        parcel.writeInt(mBusy);
        parcel.writeInt(mNumWashers);
        parcel.writeInt(mNumDryers);
        parcel.writeInt(mWashersInUse);
        parcel.writeInt(mDryersInUse);
        parcel.writeInt(mCheckInCount);
    }

    public static final Parcelable.Creator<LaundryLocation> CREATOR = new Parcelable.Creator<LaundryLocation>() {
        public LaundryLocation createFromParcel(Parcel in) {
            return new LaundryLocation(in);
        }

        public LaundryLocation[] newArray(int size) {
            return new LaundryLocation[size];
        }
    };

    public LaundryLocation(Parcel in){
        mName = in.readString();
        mID = in.readString();
        double lat = in.readDouble();
        double lng = in.readDouble();
        mLocation = new LatLng(lat,lng);
        setBusy(in.readInt());
        setNumWashers(in.readInt());
        setNumDryers(in.readInt());
        setWashersInUse(in.readInt());
        setDryersInUse(in.readInt());
        setCheckInCount(in.readInt());
    }
}
