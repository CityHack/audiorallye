package kapejod.org.soundspot;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by kapejod on 11/02/17.
 */

public class SoundSpotApplication extends Application {
    private static SoundSpotApplication instance;
    private static Context staticContext;
    protected List<Geofence> geofenceList;
    private PowerManager.WakeLock wakeLock;
    private BackgroundAudioPlayer backgroundAudioPlayer;
    private LocationListener locationListener;
    private Map<String, SoundSpot> soundSpotMap;
    private int activeSpots = 0;
    private MainActivity mainActivity;



    public static SoundSpotApplication getInstance() {
        if (instance == null) {
            instance = new SoundSpotApplication();
        }
        return instance;
    }

    public static void setContext(Context context) {
        staticContext = context;
    }
    public void setMainActivity( MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public Geofence buildGeofence(SoundSpot spot) {
        return new Geofence.Builder()
                .setRequestId(spot.uuid)
                .setCircularRegion(spot.geoPosition.latitude, spot.geoPosition.longitude, spot.geoRadius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    public void playSound(SoundSpot spot) {
        activeSpots++;
        if (wakeLock == null) {
//            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
  //          wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "soundspot");
        }
        if (backgroundAudioPlayer == null) {
            backgroundAudioPlayer = new BackgroundAudioPlayer();
        }
        backgroundAudioPlayer.play(spot);
        if (wakeLock != null) {
            wakeLock.acquire();
        }

    }

    public void stopSound(SoundSpot spot) {
        if (backgroundAudioPlayer != null) {
            if ((spot == null) || (backgroundAudioPlayer.spot == spot)) {
                backgroundAudioPlayer.stop();
                activeSpots--;
            }
        }
        if (wakeLock != null) {
            if (activeSpots == 0) {
                wakeLock.release();
            }
        }
    }

    public void toast(final String message) {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(staticContext, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void startRanging(SoundSpot spot) {
        Log.i("soundspot", "startRanging " + staticContext);
        if (staticContext != null) {
            toast(spot.name);

            LocationManager locationManager = (LocationManager) staticContext.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Log.i("soundspot", "provider " + provider);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i("soundspot", "onLocationChanged");
                    if (location != null) {
                        toast(location.getLatitude() + " " + location.getLongitude());
                    }

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
            try {
                locationManager.requestLocationUpdates(provider, 10, 1, locationListener);
            } catch (SecurityException se) {
                Log.e("soundspot", "SecurityException: " + se.getStackTrace());
            }
        }
    }

    public void stopRanging(SoundSpot spot) {
        Log.i("soundspot", "stopRanging " + staticContext);
        if (staticContext != null) {
            // toast("stopRanging " + spot.name);

            LocationManager locationManager = (LocationManager) staticContext.getSystemService(Context.LOCATION_SERVICE);
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException se) {
                Log.e("soundspot", "SecurityException: " + se.getStackTrace());
            }
            locationListener = null;
        }

    }



    public void destroy() {
        stopSound(null);
        wakeLock = null;
    }

    public SoundSpot spotByUuid(String uuid) {
        return soundSpotMap.get(uuid);
    }

    public void spotifyMap(GoogleMap map) {
        for (SoundSpot spot : soundSpotMap.values()) {
            Log.i("soundspot", "spotifyMap: " + spot.name);
            if (SoundSpot.markerIconResource(spot.category) != -1) {
                map.addMarker(new MarkerOptions()
                        .position(spot.geoPosition)
                        .title(spot.name)
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(staticContext.getResources(), SoundSpot.markerIconResource(spot.category))))
                );

            } else {
                map.addMarker(new MarkerOptions()
                        .position(spot.geoPosition)
                        .title(spot.name)
                );

            }

            CircleOptions circleOptions = new CircleOptions().center(spot.geoPosition).radius(spot.geoRadius).fillColor(SoundSpot.circleShadeColor(spot.category)).strokeColor(SoundSpot.circleStrokeColor(spot.category)).strokeWidth(8);
            map.addCircle(circleOptions);
        }
    }

    public void loadSpotData() {
        geofenceList = new ArrayList();

        soundSpotMap = new HashMap<>();

        InputStream is = staticContext.getResources().openRawResource(R.raw.sound_spots);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
        } catch (IOException ioe) {
        }

        String jsonString = writer.toString();
        try {
            JSONArray poiList = new JSONArray(jsonString);
            for (int i = 0; i < poiList.length(); i++) {
                JSONObject poi = poiList.getJSONObject(i);
                SoundSpot spot = new SoundSpot();
                spot.uuid = poi.getString("uuid");
                spot.category = poi.getString("category");
                spot.name = poi.getString("name");
                if (poi.has("description")) {
                    spot.description = poi.getString("description");
                }
                spot.infoUrl = poi.getString("infoUrl");
                spot.soundUrl = poi.getString("soundUrl");
                if (poi.has("loopSound")) {
                    spot.loopSound = poi.getBoolean("loopSound");
                }
                if (poi.has("location")) {
                    JSONObject location = poi.getJSONObject("location");
                    if (location != null) {
                        spot.geoPosition = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                        spot.geoRadius = (float) location.getDouble("radius");
                        geofenceList.add(buildGeofence(spot));
                    }
                }
                if (poi.has("beacon")) {
                    JSONObject beacon = poi.getJSONObject("beacon");
                    if (beacon != null) {
                        spot.beaconId = beacon.getString("id");
                    }
                }
                Log.i("soundspot", "adding spot " + spot.name);
                soundSpotMap.put(spot.uuid, spot);
            }

        } catch (JSONException je) {
            Log.e("soundspot", "loadSpotData: " + je.getStackTrace());
            je.printStackTrace();

        }
    }
}
