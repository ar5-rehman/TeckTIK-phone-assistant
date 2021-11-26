package com.tech.tecktik.spotifyPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.tech.tecktik.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.TracksPager;
import wseemann.media.FFmpegMediaPlayer;

public class SpotifyTrackPlayer extends Activity implements ConnectionStateCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String CLIENT_ID = "c46e8ff1cdba4efba52804b71fa431db";
    private static final String REDIRECT_URI = "spotifystreamer://callback";
    protected static Player premiumPlayer;
    protected static FFmpegMediaPlayer freePlayer;
    private static final int REQUEST_CODE = 1337;

    ImageView next, prev, play, back;
    SeekBar seekBar;
    TextView currentDuration, finalDuration ,trackName;
    ProgressBar spinner;

    private Boolean isPlaying = true;
    private Handler seekHandler = new Handler();

    public ArrayList<TracksPojoClass> trackList;
    int songPosition = 0;
    String musicName = null;

    public static String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userType = prefs.getString(getString(R.string.user_type_key),
                getString(R.string.user_type_key));
        if (userType.equals("free")) {
            AuthenticationClient.logout(getApplicationContext());
        } else {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        }

        setContentView(R.layout.activity_spotify_track_player);

        Intent in = getIntent();
        musicName = in.getStringExtra("trackName");

        next = findViewById(R.id.nextButton);
        prev = findViewById(R.id.prevButton);
        play = findViewById(R.id.playButton);
        back = findViewById(R.id.back);
        seekBar = findViewById(R.id.seekBar);
        currentDuration = findViewById(R.id.currentDuration);
        finalDuration = findViewById(R.id.finalDuration);
        trackName = findViewById(R.id.trackName);
        spinner = findViewById(R.id.progressBar3);

        next.setClickable(false);
        prev.setClickable(false);
        back.setClickable(false);
        seekBar.setClickable(false);
        play.setClickable(false);
        finalDuration.setClickable(false);
        currentDuration.setClickable(false);
        trackName.setClickable(false);
        spinner.setClickable(false);

        trackList = new ArrayList<>();

        spinner.setVisibility(View.VISIBLE);

        if (freePlayer != null) {
            freePlayer.pause();
        }
        if (premiumPlayer != null) {
            premiumPlayer.pause();
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                if(premiumPlayer!=null) {
                    songPosition++;
                    if (songPosition > trackList.size() - 1) {
                        songPosition = 0;
                    }
                    setUI();
                    play.setImageResource(R.drawable.play);
                    if (freePlayer != null) {
                        freePlayer.reset();
                    }
                    prepareMusic();
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                if(premiumPlayer!=null) {
                    songPosition = songPosition - 1;
                    if (songPosition < 0) {
                        songPosition = 0;
                    }
                    setUI();
                    play.setImageResource(R.drawable.play);
                    if (freePlayer != null) {
                        freePlayer.reset();
                    }
                    prepareMusic();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(premiumPlayer!=null) {
                    if (isPlaying) {
                        premiumPlayer.pause();
                    }
                }
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(premiumPlayer!=null) {
            if (isPlaying) {
                premiumPlayer.pause();
            }
        }
    }

    public void setUI()
    {
        if(!trackList.isEmpty()) {
            trackName.setText(trackList.get(songPosition).trackName);
            seekBar.setMax(Integer.parseInt(trackList.get(songPosition).trackDuration));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {

                        if (freePlayer != null) {
                            freePlayer.seekTo(progress);
                        }
                        if (premiumPlayer != null) {
                            premiumPlayer.seekToPosition(progress);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            currentDuration.setText("00:00");

            // set end duration of track
            String duration = trackList.get(songPosition).trackDuration;
            int seconds = ((Integer.parseInt(duration) / 1000) % 60);
            int minutes = ((Integer.parseInt(duration) / 1000) / 60);
            if (seconds < 10) {
                finalDuration.setText(String.valueOf(minutes) + ":0" + String.valueOf(seconds));
            } else {
                finalDuration.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));
            }
        }
    }

    private void prepareMusic() {

        // check for free or premium user
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userType = prefs.getString(getString(R.string.user_type_key),
                getString(R.string.user_type_key));

        // free user
        if (userType.equals("free")) {

            freePlayer = new FFmpegMediaPlayer();

            // initially not playing
            isPlaying = false;

            // disable until prepared
            play.setClickable(false);
            play.setImageResource(R.drawable.ic_stop);

            freePlayer.setOnPreparedListener(new FFmpegMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(FFmpegMediaPlayer mp) {
                    spinner.setVisibility(View.GONE);

                    // restore button
                    play.setClickable(true);
                    play.setImageResource(R.drawable.pause);

                    freePlayer.start();
                    setSeekBar();
                    isPlaying = true;

                    // play/pause
                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isPlaying) {
                                freePlayer.start();
                                play.setImageResource(R.drawable.pause);
                                isPlaying = true;
                            } else {
                                freePlayer.pause();
                                isPlaying = false;
                                play.setImageResource(R.drawable.play);
                            }
                        }
                    });
                }
            });
        } else {
            // premium player

            // get track
            final String trackUrl = trackList.get(songPosition).trackUrl;

            // initially not playing
            isPlaying = false;

            // disable until prepared
            play.setClickable(false);
            play.setImageResource(R.drawable.ic_stop);

            Config playerConfig = new Config(SpotifyTrackPlayer.this, SpotifyTrackPlayer.getAccessToken(), SpotifyTrackPlayer.CLIENT_ID);
            premiumPlayer = Spotify.getPlayer(playerConfig, SpotifyTrackPlayer.this, new Player.InitializationObserver() {
                @Override
                public void onInitialized(Player player) {

                    // restore button
                    play.setClickable(true);

                    premiumPlayer.play(trackUrl);
                    spinner.setVisibility(View.GONE);



                    play.setImageResource(R.drawable.pause);
                    setSeekBar();
                    isPlaying = true;

                    // play/pause
                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isPlaying) {

                                premiumPlayer.resume();
                                play.setImageResource(R.drawable.pause);
                                isPlaying = true;

                                premiumPlayer.getPlayerState(new PlayerStateCallback() {
                                    @Override
                                    public void onPlayerState(PlayerState playerState) {
                                        int progress = playerState.positionInMs;


                                    }
                                });

                            } else {
                                premiumPlayer.pause();
                                isPlaying = false;
                                play.setImageResource(R.drawable.play);

                            }
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    //try again
                    prepareMusic();
                }
            });
        }
    }

    /*public void prepareMusic()
    {

        if(!trackList.isEmpty()) {
            String trackUrl = trackList.get(songPosition).trackUrl;
            play.setClickable(true);
            premiumPlayer.play(trackUrl);
            play.setImageResource(R.drawable.pause);
            isPlaying = true;
        }

        // play/pause
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {

                    premiumPlayer.resume();
                    play.setImageResource(R.drawable.pause);
                    isPlaying = true;
                    setSeekBar();

                } else {

                    premiumPlayer.pause();
                    isPlaying = false;
                    play.setImageResource(R.drawable.play);

                }
            }
        });
    }
*/
    public void setAccessToken(String token){
        this.accessToken = token;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                case TOKEN:
                    setAccessToken(response.getAccessToken());
                    Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                    next.setClickable(true);
                    prev.setClickable(true);
                    back.setClickable(true);
                    seekBar.setClickable(true);
                    play.setClickable(true);
                    finalDuration.setClickable(true);
                    currentDuration.setClickable(true);
                    trackName.setClickable(true);
                    spinner.setClickable(true);
                    premiumPlayer = Spotify.getPlayer(playerConfig, SpotifyTrackPlayer.this, new Player.InitializationObserver(){
                        @Override
                        public void onInitialized(Player player) {
                            new SearchTracks(SpotifyTrackPlayer.this, trackList).execute(musicName);

                            premiumPlayer.getPlayerState(new PlayerStateCallback() {
                                @Override
                                public void onPlayerState(PlayerState playerState) {

                                    seekBar.setProgress(playerState.positionInMs);

                                    int seconds = ((playerState.positionInMs / 1000) % 60);
                                    int minutes = ((playerState.positionInMs / 1000) / 60);
                                    if (seconds < 10) {
                                        currentDuration.setText(String.valueOf(minutes) + ":0" + String.valueOf(seconds));
                                    } else {
                                        currentDuration.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable throwable) {
                        }
                    });
                    break;
                case ERROR:
                    Toast.makeText(SpotifyTrackPlayer.this, "Could not log in, please restart app!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    public static String getAccessToken() {
        return accessToken;
    }

    private void setSeekBar() {

        if (freePlayer != null) {
            seekBar.setProgress(freePlayer.getDuration());
        }
        if (premiumPlayer != null) {
            premiumPlayer.getPlayerState(new PlayerStateCallback() {
                @Override
                public void onPlayerState(PlayerState playerState) {
                    seekBar.setProgress(playerState.positionInMs);
                    int seconds = ((playerState.positionInMs / 1000) % 60);
                    int minutes = ((playerState.positionInMs / 1000) / 60);
                    if (seconds < 10) {
                        currentDuration.setText(String.valueOf(minutes) + ":0" + String.valueOf(seconds));
                    } else {
                        currentDuration.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));
                    }
                }
            });
        }

        // ping for updated position every second
        seekHandler.postDelayed(run, 1000);
    }

    // seperate thread for pinging seekbar position
    Runnable run = new Runnable() {
        @Override
        public void run() {
            setSeekBar();
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userType = prefs.getString(getString(R.string.user_type_key),
                getString(R.string.user_type_key));
        if (userType.equals("free")) {
            AuthenticationClient.logout(getApplicationContext());
            premiumPlayer.pause();
            Toast.makeText(this, "Logged out!", Toast.LENGTH_LONG).show();
        } else {
            AuthenticationRequest.Builder builder =
                    new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming"});
            AuthenticationRequest request = builder.build();
            if (freePlayer != null) {
                freePlayer.reset();
            }

            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        }
    }

    public class SearchTracks extends AsyncTask<String, Void, Boolean> {

        Context context;
        ArrayList<TracksPojoClass> trackList;

        public SearchTracks(Context context, ArrayList<TracksPojoClass> trackList)
        {
            this.context = context;
            this.trackList = trackList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... artistId) {
            try {

                SpotifyApi api = new SpotifyApi();
                api.setAccessToken(SpotifyTrackPlayer.getAccessToken());
                SpotifyService spotify = api.getService();

                Map<String, Object> options = new HashMap<>();
                options.put("limit", 30);

                trackList.clear();
                TracksPager tracksPager = spotify.searchTracks(artistId[0], options);
                for(TrackSimple track: tracksPager.tracks.items){

                    trackList.add(new TracksPojoClass(track.uri,track.name,track.duration_ms));
                    ((SpotifyTrackPlayer)context).runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d("URI are: ", track.uri+" Time duration : "+ track.duration_ms);
                        }
                    });
                }

                // set options
                return !trackList.isEmpty();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            setUI();
            prepareMusic();
            setSeekBar();
            super.onPostExecute(aBoolean);
        }
    }
}