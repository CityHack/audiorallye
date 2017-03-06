package kapejod.org.soundspot;

import android.util.Log;

public class GeofencingReceiver extends GeofenceTransitionIntentService {
    @Override
    protected void onEnteredGeofences(String[] geofenceIds) {
        for (int i = 0; i < geofenceIds.length; i++) {
            Log.d("soundspot", "onEnter: " + geofenceIds[i]);
            SoundSpot spot = SoundSpotApplication.getInstance().spotByUuid(geofenceIds[i]);
            if (spot != null) {
                if (spot.soundUrl != null) {
                    SoundSpotApplication.getInstance().playSound(spot);
                    SoundSpotApplication.getInstance().startRanging(spot);
                }
            }
        }
    }

    @Override
    protected void onExitedGeofences(String[] geofenceIds) {
        for (int i = 0; i < geofenceIds.length; i++) {
            Log.d("soundspot", "onExit: " + geofenceIds[i]);
            SoundSpot spot = SoundSpotApplication.getInstance().spotByUuid(geofenceIds[i]);
            if (spot != null) {
                if (spot.soundUrl != null) {
                    SoundSpotApplication.getInstance().stopSound(spot);
                    SoundSpotApplication.getInstance().stopRanging(spot);
                }
            }
        }
    }

    @Override
    protected void onError(int errorCode) {
        Log.e("soundspot", "Error: " + errorCode);
    }
}
