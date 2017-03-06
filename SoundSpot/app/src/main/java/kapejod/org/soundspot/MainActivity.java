package kapejod.org.soundspot;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import kapejod.org.soundspot.dummy.DummyContent;

public class MainActivity extends AppCompatActivity implements CategoryFragment.OnListFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private SpotMapFragment spotMapFragment;
    private CategoryFragment categoryFragment;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SoundSpotApplication.setContext(this);
        SoundSpotApplication.getInstance().setMainActivity(this);
        SoundSpotApplication.getInstance().loadSpotData();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        registerGeofences();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("SoundSpot beenden?")
                .setMessage("App beenden und keine neuen Geopodcasts mehr empfangen?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void onDestroy() {
        SoundSpotApplication.getInstance().destroy();
        unregisterGeofences();
        super.onDestroy();
    }

    public void unregisterGeofences() {
        try {
//            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, SoundSpotApplication.getInstance().geofenceList)
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getRequestPendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {

                        }
                    });
        } catch (SecurityException securityException) {
            Log.e("soundspot", "unregisterGeofences: " + securityException.getStackTrace());
        }
    }

    public void registerGeofences(){

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e("soundspot", "onConnected: ");

        mGeofencePendingIntent = createRequestPendingIntent();

        try {
            PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, SoundSpotApplication.getInstance().geofenceList, mGeofencePendingIntent);
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i("soundspot", "registered geofences list");
                        // Successfully registered
/*                    if(mCallback != null){
                        mCallback.onGeofencesRegisteredSuccessful();
                    } */

                    } else if (status.hasResolution()) {
                        // Google provides a way to fix the issue
                        Log.i("soundspot", "could not register geofences list");
                    /*
                    status.startResolutionForResult(
                            mContext,     // your current activity used to receive the result
                            RESULT_CODE); // the result code you'll look for in your
                    // onActivityResult method to retry registering
                    */
                    } else {
                        // No recovery. Weep softly or inform the user.
                        Log.e("soundspot", "Registering failed: " + status.getStatusMessage());
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("soundspot", "onConnected: " + e.getStackTrace());
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.e("soundspot", "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e("soundspot", "onConnectionFailed: " + connectionResult.getErrorCode());
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onSpotMapFragmentInteraction(Uri uri) {

    }

    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }



    public PendingIntent getRequestPendingIntent() {
        return createRequestPendingIntent();
    }

    /**
     * Get a PendingIntent to send with the request to add Geofences. Location
     * Services issues the Intent inside this PendingIntent whenever a geofence
     * transition occurs for the current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence
     * transitions.
     */
    private PendingIntent createRequestPendingIntent() {
        Log.i("soundspot", "createRequestPendingIntent");
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        } else {
            Intent intent = new Intent(this, GeofencingReceiver.class);
            return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }





    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    if (spotMapFragment == null) {
                        spotMapFragment = SpotMapFragment.newInstance(new LatLng(37.4005502611301, -5.98233461380005));
                    }

                   return spotMapFragment;
            }
            return CategoryFragment.newInstance();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Karte";
                case 1:
                    return "Filter";
                case 2:
                    return "Favoriten";
            }
            return null;
        }
    }
}
