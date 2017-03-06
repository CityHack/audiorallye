package kapejod.org.soundspot;

import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by kapejod on 12/02/17.
 */

public class SoundSpot {
    public static String CATEGORY_TOURISM = "tourism";
    public static String CATEGORY_MUSIC = "music";
    public static String CATEGORY_NEWS = "news";
    public static String CATEGORY_GAMES = "games";
    public static String CATEGORY_GEOPODCAST = "geopodcast";
    public static String CATEGORY_ENTERTAINMENT = "entertainment";

    protected String uuid;
    protected String name;
    protected String description;
    protected String soundUrl;
    protected String infoUrl;
    protected LatLng geoPosition;
    protected float geoRadius;
    protected String category;
    protected String beaconId;
    protected boolean loopSound;

    public static int circleStrokeColor(String category) {
        if (category.equalsIgnoreCase(CATEGORY_NEWS)) {
            return 0xff802BA3;
        } else if (category.equalsIgnoreCase(CATEGORY_MUSIC)) {
            return 0xFF3BA0CC;
        } else if (category.equalsIgnoreCase(CATEGORY_TOURISM)) {
            return 0xFFC41414;
        } else if (category.equalsIgnoreCase(CATEGORY_ENTERTAINMENT)) {
            return 0xFFFF8F29;
        } else if (category.equalsIgnoreCase(CATEGORY_GEOPODCAST)) {
            return 0xFF9E9E9E;
        }
        return 0xff999999;
    }

    public static int circleShadeColor(String category) {
        if (category.equalsIgnoreCase(CATEGORY_NEWS)) {
            return 0x24802BA3;
        } else if (category.equalsIgnoreCase(CATEGORY_MUSIC)) {
            return 0x243BA0CC;
        } else if (category.equalsIgnoreCase(CATEGORY_TOURISM)) {
            return 0x24C41414;
        } else if (category.equalsIgnoreCase(CATEGORY_ENTERTAINMENT)) {
            return 0x24FF8F29;
        } else if (category.equalsIgnoreCase(CATEGORY_GEOPODCAST)) {
            return 0x249E9E9E;
        }
        return 0x24999999;
    }

    public static int markerIconResource(String category) {
        if (category.equalsIgnoreCase(CATEGORY_NEWS)) {
            return R.drawable.ic_news;
        } else if (category.equalsIgnoreCase(CATEGORY_MUSIC)) {
            return R.drawable.ic_music;
        } else if (category.equalsIgnoreCase(CATEGORY_TOURISM)) {
            return R.drawable.ic_tourism;
        } else if (category.equalsIgnoreCase(CATEGORY_GAMES)) {
            return R.drawable.ic_games;
        } else if (category.equalsIgnoreCase(CATEGORY_ENTERTAINMENT)) {
            return R.drawable.ic_entertainment;
        } else if (category.equalsIgnoreCase(CATEGORY_GEOPODCAST)) {
            return R.drawable.ic_geopodcast;
        }
        return -1;
    }

}
