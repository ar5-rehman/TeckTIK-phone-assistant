package com.tech.tecktik.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.FileUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.tech.tecktik.R;
import com.tech.tecktik.model.AudioChannel;
import com.tech.tecktik.model.AudioSampleRate;
import com.tech.tecktik.model.AudioSource;
import com.tech.tecktik.utils.AudioUploadingTask;
import com.tech.tecktik.utils.Util;
import com.tech.tecktik.utils.VisualizerHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PullTransport.OnAudioChunkPulledListener, MediaPlayer.OnCompletionListener{

    public static final int REQUEST_RECORD_AUDIO = 0;
    private static final int PERMISSION_REQUEST_CODE = 200;
    public ImageView assistantPlay, musicTheme, wtsapselected, fbselected, instaselected, contactselected, blueselected, facetimeselected;

    public String filePath = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";
    public String fileCopy = Environment.getExternalStorageDirectory() + "/audio.wav";


    public AudioSource source = AudioSource.MIC;
    public AudioChannel channel = AudioChannel.STEREO;
    public AudioSampleRate sampleRate = AudioSampleRate.HZ_48000;
    public int color = Color.parseColor("#546E7A");
    File tempFile, copyFile;

    AudioUploadingTask task;

    private MediaPlayer player;
    private Recorder recorder;
    private VisualizerHandler visualizerHandler;
    private boolean isRecording;
    private GLAudioVisualizationView visualizerView;
    private RelativeLayout topAssist;
    private RecyclerView recyclerView;
    private TextView assisttext;

    private CountDownTimer mCountDownTimer;
    private long START_TIME_IN_MILLIS = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assistantPlay = findViewById(R.id.assistantplay);
        assistantPlay.setOnClickListener(this);
        topAssist = findViewById(R.id.topassit);

        musicTheme = findViewById(R.id.musictheme);
        wtsapselected = findViewById(R.id.wtsapselected);
        fbselected = findViewById(R.id.fbselected);
        instaselected = findViewById(R.id.instaselected);
        blueselected = findViewById(R.id.blueselected);
        contactselected = findViewById(R.id.contactselected);
        facetimeselected = findViewById(R.id.facetimeselected);
        assisttext = findViewById(R.id.assistanttext);

        copyFile = new File(fileCopy);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        if (checkPermission()) {
        } else {
            requestPermission();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(ContextCompat.getColor(this, R.color.purple_700)));
        }

        visualizerView = new GLAudioVisualizationView.Builder(this)
                .setLayersCount(1)
                .setWavesCount(6)
                .setWavesHeight(R.dimen.aar_wave_height)
                .setWavesFooterHeight(R.dimen.aar_footer_height)
                .setBubblesPerLayer(20)
                .setBubblesSize(R.dimen.aar_bubble_size)
                .setBubblesRandomizeSize(true)
                .setBackgroundColor(Util.getDarkerColor(color))
                .setLayerColors(new int[]{color})
                .build();

    }

    boolean cleanRecording=false;

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.assistantplay){

            if (checkPermission()) {
            } else {
                requestPermission();
                return;
            }

                mCountDownTimer = new CountDownTimer(START_TIME_IN_MILLIS, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        assistantPlay.setClickable(false);
                        if(cleanRecording==false) {
                            resumeRecording();
                        }
                    }
                    @Override
                    public void onFinish() {
                        cleanRecording = false;
                        selectAudio();
                        assistantPlay.setClickable(true);
                    }
                }.start();

        }
    }

    private boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int result5 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result6 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int result7 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int result8 = ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE);


        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED &&
                result3 == PackageManager.PERMISSION_GRANTED && result4 == PackageManager.PERMISSION_GRANTED
                && result5 == PackageManager.PERMISSION_GRANTED  && result6 == PackageManager.PERMISSION_GRANTED
                && result7 == PackageManager.PERMISSION_GRANTED && result8 == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE,
                RECORD_AUDIO, READ_CONTACTS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, READ_PHONE_STATE, CALL_PHONE }, PERMISSION_REQUEST_CODE);
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean read = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean write = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean audio = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean contacts = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean fineLocation = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean coarseLocation = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean phoneState = grantResults[6] == PackageManager.PERMISSION_GRANTED;
                    boolean phoneCall = grantResults[7] == PackageManager.PERMISSION_GRANTED;

                    if (read && write && audio && contacts && fineLocation && coarseLocation && phoneState && phoneCall)
                    {
                        Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                                    showMessageOKCancel("You need to accept all the permissions",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    int targetSdkVersion = getApplicationInfo().targetSdkVersion;
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                        requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_CONTACTS, READ_PHONE_STATE, CALL_PHONE},
                                                                PERMISSION_REQUEST_CODE);
                                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
                                                        requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                                                                PERMISSION_REQUEST_CODE);
                                                    }
                                                }
                                            });
                                    return;
                                }
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                    }
                }

                break;

        }
    }*/

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            visualizerView.onResume();
        } catch (Exception e){ }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        if(task!=null) {
            registerReceiver(task.bReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        restartRecording(null);
        try {
            visualizerView.onPause();
        } catch (Exception e){

        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        restartRecording(null);
        setResult(RESULT_CANCELED);
        try {
            visualizerView.release();
        } catch (Exception e){ }
        if(task!=null)
        {
            this.unregisterReceiver(task.bReceiver);
        }
        super.onDestroy();
    }


    @Override
    public void onAudioChunkPulled(AudioChunk audioChunk) {
        float amplitude = isRecording ? (float) audioChunk.maxAmplitude() : 0f;
        visualizerHandler.onDataReceived(amplitude);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopPlaying();
    }

    private void selectAudio() {
        musicTheme.setVisibility(View.GONE);
        stopRecording();
        setResult(RESULT_OK);

        try {
            copy(tempFile, copyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        task = new AudioUploadingTask(MainActivity.this, wtsapselected,fbselected,instaselected,blueselected,
                contactselected,facetimeselected, recyclerView, musicTheme, assistantPlay, topAssist, assisttext);
        task.execute(copyFile);
    }

    public void restartRecording(View v){
        if(isRecording) {
            stopRecording();
        } else if(isPlaying()) {
            stopPlaying();
        } else {
            visualizerHandler = new VisualizerHandler();
            visualizerView.linkTo(visualizerHandler);
            visualizerView.release();
            if(visualizerHandler != null) {
                visualizerHandler.stop();
            }
        }

    }


    private void resumeRecording() {
        try {
            isRecording = true;
            cleanRecording = true;
            musicTheme.setVisibility(View.VISIBLE);

            visualizerHandler = new VisualizerHandler();
            visualizerView.linkTo(visualizerHandler);

            if (recorder == null) {

                tempFile = new File(filePath);

                recorder = OmRecorder.wav(
                        new PullTransport.Default(Util.getMic(source, channel, sampleRate), this),
                        tempFile);
            }
            recorder.resumeRecording();
        }catch(Exception e)
        {
            e.printStackTrace();
        }finally {

        }

    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.flush();
                out.close();
            }catch(Exception e)
            {
                e.printStackTrace();
            }finally {

            }
        }
    }


    private void stopRecording(){
        isRecording = false;
        visualizerView.release();
        if(visualizerHandler != null) {
            visualizerHandler.stop();
        }

        if (recorder != null) {
            recorder.stopRecording();
            recorder = null;
        }

    }

    private void stopPlaying(){

        visualizerView.release();
        if(visualizerHandler != null) {
            visualizerHandler.stop();
        }


        if(player != null){
            try {
                player.stop();
            } catch (Exception e){ }
        }

    }

    private boolean isPlaying(){
        try {
            return player != null && player.isPlaying() && !isRecording;
        } catch (Exception e){
            return false;
        }
    }

    Integer backToExitPressedCounter = 0;

    @Override
    public void onBackPressed() {
        assistantPlay.setVisibility(View.VISIBLE);
        assisttext.setVisibility(View.VISIBLE);
        topAssist.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        fbselected.setVisibility(View.INVISIBLE);
        instaselected.setVisibility(View.INVISIBLE);
        wtsapselected.setVisibility(View.INVISIBLE);
        contactselected.setVisibility(View.INVISIBLE);
        blueselected.setVisibility(View.INVISIBLE);
        facetimeselected.setVisibility(View.INVISIBLE);

        if(backToExitPressedCounter==1){
            super.onBackPressed();
            backToExitPressedCounter = 0;
        } else {
            backToExitPressedCounter++;
        }
    }

}