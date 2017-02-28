package cs4518.laundrybuddy;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by Jake on 2/22/2017.
 */

public class GeofenceTransitionsIntentService extends IntentService {
    protected static final String URL = "http://130.215.250.210:8080/checkIn";
    protected static final String TAG = "GeofenceTransitionsIS";
    RequestQueue queue;
    public GeofenceTransitionsIntentService() {

        // Use the TAG to name the worker thread.

        super(TAG);

    }

    @Override

    public void onCreate() {

        super.onCreate();
        queue = Volley.newRequestQueue(this);

    }

    protected void onHandleIntent(Intent intent) {
        Map<String, String> jsonParams = new HashMap<String, String>();
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = "";
            Integer errorNum = geofencingEvent.getErrorCode();
            errorMessage = Integer.toString(errorNum);
            Log.e(TAG, errorMessage.toString());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            Geofence targetGeofence = (Geofence) triggeringGeofences.get(0);
            String targetString = targetGeofence.getRequestId();
            String isEntering = "true";
            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                isEntering = "false";
            }
            jsonParams.put("locationID",targetString);
            jsonParams.put("inLocation",isEntering);
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, URL,
                    new JSONObject(jsonParams),
                    new Response.Listener<JSONObject>(){
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("GEO", response.toString());
                            String responseCode = "";
                            try {
                                responseCode = response.getString("result");
                            } catch (JSONException e) {
                                Log.v("GEO", "Error reporting the Geofence");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.v("GEO", "Error reporting check in info to LaundryBuddy Server");
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
            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, "Bad Geofence");
        }

    }

    private String getGeofenceTransitionDetails(

            Context context,

            int geofenceTransition,

            List<Geofence> triggeringGeofences) {



        String geofenceTransitionString = getTransitionString(geofenceTransition);



        // Get the Ids of each geofence that was triggered.

        ArrayList triggeringGeofencesIdsList = new ArrayList();

        for (Geofence geofence : triggeringGeofences) {

            triggeringGeofencesIdsList.add(geofence.getRequestId());

        }

        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);



        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;

    }


    private void sendNotification(String notificationDetails) {

        // Create an explicit content Intent that starts the main Activity.

        Intent notificationIntent = new Intent(getApplicationContext(), Main.class);



        // Construct a task stack.

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);



        // Add the main Activity to the task stack as the parent.

        stackBuilder.addParentStack(Main.class);



        // Push the content Intent onto the stack.

        stackBuilder.addNextIntent(notificationIntent);



        // Get a PendingIntent containing the entire back stack.

        PendingIntent notificationPendingIntent =

                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);



        // Get a notification builder that's compatible with platform versions >= 4

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);



        // Define the notification settings.

        builder.setSmallIcon(R.drawable.cast_ic_notification_small_icon)

                // In a real app, you may want to use a library like Volley

                // to decode the Bitmap.

                .setLargeIcon(BitmapFactory.decodeResource(getResources(),

                        R.drawable.cast_ic_notification_0))

                .setColor(Color.RED)

                .setContentTitle(notificationDetails)

                .setContentText("GEOFENCE ALERT")

                .setContentIntent(notificationPendingIntent);



        // Dismiss notification once the user touches it.

        builder.setAutoCancel(true);



        // Get an instance of the Notification manager

        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        // Issue the notification

        mNotificationManager.notify(0, builder.build());

    }


    private String getTransitionString(int transitionType) {

        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:

                return "Entering";

            case Geofence.GEOFENCE_TRANSITION_EXIT:

                return "Leaving";

            default:

                return "Dunno";

        }

    }

}
