package kapejod.org.soundspot;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.ServiceConfigurationError;

import static android.content.Context.LOCATION_SERVICE;

public class SpotMapFragment extends SupportMapFragment {
    private LatLng myPosition;
    private GoogleMap map;
    private Marker lvzMarker;
    private Marker pklMarker;

    public SpotMapFragment() {
        super();
        MainActivity mainActivity = (MainActivity) getActivity();
    }

    public static SpotMapFragment newInstance(LatLng position){
        SpotMapFragment frag = new SpotMapFragment();
        frag.myPosition = position;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
        View v = super.onCreateView(arg0, arg1, arg2);


        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                try {
                    SoundSpotApplication.getInstance().spotifyMap(map);

                    map.setMyLocationEnabled(true);
                    MainActivity mainActivity = (MainActivity) getActivity();
                    LocationManager locationManager = (LocationManager) mainActivity.getSystemService(LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String provider = locationManager.getBestProvider(criteria, true);
                    Location location = locationManager.getLastKnownLocation(provider);
                    CameraUpdate cameraUpdate = null;

                    boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mainActivity, R.raw.style_json));
                    if (location != null) {
                        myPosition = new LatLng(location.getLatitude(), location.getLongitude());
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(myPosition, map.getMaxZoomLevel() - 5);
                    } else {
                        cameraUpdate = CameraUpdateFactory.zoomTo(map.getMaxZoomLevel() - 5);
                    }
                    map.animateCamera(cameraUpdate);

                } catch (SecurityException se) {
                    Log.i("soundspot", "SecurityException: " + se.getStackTrace());
                }

            }
        });
        return v;
    }

}
