package kapejod.org.soundspot;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

public class BackgroundAudioPlayer {
    private static BackgroundAudioPlayer instance;
    private MediaPlayer mediaPlayer;
    protected SoundSpot spot;


    public BackgroundAudioPlayer() {
    }

    public static BackgroundAudioPlayer getInstance() {
        if (instance == null) {
            instance = new BackgroundAudioPlayer();
        }
        return null;
    }

    public void play(SoundSpot spot) {
        this.spot = spot;
        Log.i("soundspot", "buffering " + spot.soundUrl);
        try {
            mediaPlayer =
                    new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(spot.loopSound);
            mediaPlayer.setDataSource(spot.soundUrl);
            mediaPlayer.prepare(); //  take long! (for buffering, etc)
            if (spot.loopSound) {
                Log.i("soundspot", "looping " + spot.soundUrl);
            } else {
                Log.i("soundspot", "playing " + spot.soundUrl);
            }
            mediaPlayer.start();
        } catch (IOException ioe) {
            Log.e("soundspot", "BackgroundAudioPlayer: " + ioe.getStackTrace());
            ioe.printStackTrace();
        }
    }

    public void stop() {
        spot = null;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

}
