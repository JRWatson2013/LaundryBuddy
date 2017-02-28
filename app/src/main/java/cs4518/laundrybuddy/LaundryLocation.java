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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaundryLocation implements Parcelable {
    private String mName;
    private String mID;
    private String mPlace;
    private LatLng mLocation;
    private int mBusy;
    private int mNumWashers;
    private int mNumDryers;
    private int mWashersInUse;
    private int mDryersInUse;
    private int mCheckInCount;
    public List<LaundryMachine> machineList;

    LaundryLocation(String name, String ID, LatLng location, String place){
        mName = name;
        mID = ID;
        mLocation = location;
        mPlace = place;
    }

    LaundryLocation(JSONObject obj){
        try {
            mName = obj.getString("name");
            mID = obj.getString("id");
            this.setLocation(obj.getJSONObject("geometry").getJSONObject("location"));
            this.setPlace(obj.getString("vicinity"));
            machineList = new ArrayList<LaundryMachine>();
        } catch (JSONException e) {
            Log.v("LG", "Could not convert JSON to LaundryLocation");
        }
    }

    LaundryLocation(String name, String ID, JSONObject location){
        mName = name;
        mID = ID;
        this.setLocation(location);
        machineList = new ArrayList<LaundryMachine>();
    }

    public String getPlace() {
        return mPlace;
    }

    public void setPlace(String mPlace) {
        this.mPlace = mPlace;
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

        String url = "http://130.215.173.246:8080/getPlaceInfo";
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
                            setMachineList(response.getJSONArray("machineList"));
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

    void setMachineList(JSONArray jo){
        for(int i = 0; i < jo.length(); i ++) {
            try {
                JSONObject thisMachine = jo.getJSONObject(i);
                machineList.add(new LaundryMachine(thisMachine.getInt("machNum"), thisMachine.getString("state"), thisMachine.getString("type")));
            } catch (JSONException e){
                Log.v("LG","Error parsing machine number: " + i);
            }
        }
    }

    void setMachineList(List<LaundryMachine> l){
        machineList = l;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mID);
        parcel.writeString(mPlace);
        parcel.writeDouble(mLocation.latitude);
        parcel.writeDouble(mLocation.longitude);
        parcel.writeInt(mBusy);
        parcel.writeInt(mNumWashers);
        parcel.writeInt(mNumDryers);
        parcel.writeInt(mWashersInUse);
        parcel.writeInt(mDryersInUse);
        parcel.writeInt(mCheckInCount);
        parcel.writeTypedList(machineList);
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
        mPlace = in.readString();
        double lat = in.readDouble();
        double lng = in.readDouble();
        mLocation = new LatLng(lat,lng);
        setBusy(in.readInt());
        setNumWashers(in.readInt());
        setNumDryers(in.readInt());
        setWashersInUse(in.readInt());
        setDryersInUse(in.readInt());
        setCheckInCount(in.readInt());
        machineList = new ArrayList<LaundryMachine>();
        in.readTypedList(machineList, LaundryMachine.CREATOR);
    }

}
