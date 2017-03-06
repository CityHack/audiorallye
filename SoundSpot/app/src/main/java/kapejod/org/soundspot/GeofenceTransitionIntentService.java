package kapejod.org.soundspot;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.*;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
abstract class GeofenceTransitionIntentService extends IntentService {
    private static String TAG = "soundspot";
    public GeofenceTransitionIntentService() {
        super("GeofenceTransitionIntentService");
        Log.i(TAG, "GeofenceTransitionIntentService");
    }



    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "GeofenceTransitionService.onHandleIntent");
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if(event != null){

            if(event.hasError()){
                onError(event.getErrorCode());
            } else {
                int transition = event.getGeofenceTransition();
                if(transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                    String[] geofenceIds = new String[event.getTriggeringGeofences().size()];
                    for (int index = 0; index < event.getTriggeringGeofences().size(); index++) {
                        geofenceIds[index] = event.getTriggeringGeofences().get(index).getRequestId();
                    }

                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                        onEnteredGeofences(geofenceIds);
                    } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                        onExitedGeofences(geofenceIds);
                    }
                }
            }

        }
    }

    protected abstract void onEnteredGeofences(String[] geofenceIds);

    protected abstract void onExitedGeofences(String[] geofenceIds);
    protected abstract void onError(int errorCode);

}
