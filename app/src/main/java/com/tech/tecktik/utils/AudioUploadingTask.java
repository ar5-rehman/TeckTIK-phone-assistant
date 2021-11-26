package com.tech.tecktik.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationServices;
import com.tech.tecktik.activities.MainActivity;
import com.tech.tecktik.cloudservices.WebServices;
import com.tech.tecktik.googleMap.MapActivity;
import com.tech.tecktik.model.BluetoothDevicePojo;
import com.tech.tecktik.model.PostSpeech;
import com.tech.tecktik.spotifyPlayer.SpotifyTrackPlayer;
import com.tech.tecktik.youtubePlayer.PlayerActivity;
import com.tech.tecktik.youtubePlayer.YoutubeConnector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class AudioUploadingTask extends AsyncTask<File, Void,Void> {

    ListView artistView;

    Context context;
    ImageView  wtsapselected,fbselected,instaselected,blueselected, contactselected, facetimeselected;
    MainActivity activity;
    ImageView musicTheme, assistantPlay;
    RelativeLayout topAssist;
    TextView assisttext;

    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    List<BluetoothDevicePojo> list;

    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    String deviceName;

    SharedPreferences spotActivation;

    private List<com.tech.tecktik.youtubePlayer.VideoItem> searchResults;

    public AudioUploadingTask()
    {

    }

    public AudioUploadingTask(Context context, ListView artistView)
    {
        this.context = context;
        this.artistView = artistView;
    }

    public AudioUploadingTask(Context context, ImageView wtsapselected, ImageView fbselected,
                              ImageView instaselected, ImageView blueselected , ImageView contactselected , ImageView facetimeselected,
                              RecyclerView recyclerView,
                              ImageView musicTheme, ImageView assistantPlay, RelativeLayout topAssist, TextView assisttext)
    {
        this.context = context;
        this.wtsapselected = wtsapselected;
        this.fbselected = fbselected;
        this.instaselected = instaselected;
        this.blueselected = blueselected;
        this.contactselected = contactselected;
        this.facetimeselected = facetimeselected;
        this.recyclerView = recyclerView;
        this.musicTheme = musicTheme;
        this.assistantPlay = assistantPlay;
        this.topAssist = topAssist;
        this.assisttext = assisttext;
        activity = (MainActivity) context;

        spotActivation = context.getSharedPreferences("spotAc", Context.MODE_PRIVATE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported!", Toast.LENGTH_SHORT).show();
        }
        list = new ArrayList<>();
        pairedDevices = bluetoothAdapter.getBondedDevices();
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    protected Void doInBackground(File... files) {
        final WebServices myWebService = WebServices.retrofit.create(WebServices.class);
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        files[0]
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part audio =
                MultipartBody.Part.createFormData("audio", files[0].getName(), requestFile);

        Call<PostSpeech> call = myWebService.uploadAudio(audio);
        call.enqueue(new Callback<PostSpeech>() {
            @Override
            public void onResponse(Call<PostSpeech> call, Response<PostSpeech> response) {

                String transcription = null;

                PostSpeech speechdata  = response.body();

                if(speechdata!=null)
                {
                    transcription = speechdata.getTranscription();
                   // Toast.makeText(context, transcription.toString(), Toast.LENGTH_LONG).show();
                }

                if(response.isSuccessful())
                {
                    if(transcription.equals("call"))
                    {
                        Toast.makeText(context, "Call", Toast.LENGTH_SHORT).show();
                    }else
                    if(transcription.contains("open facebook"))
                    {
                        fbselected.setVisibility(View.VISIBLE);
                        instaselected.setVisibility(View.GONE);
                        wtsapselected.setVisibility(View.GONE);
                        contactselected.setVisibility(View.GONE);
                        blueselected.setVisibility(View.GONE);
                        facetimeselected.setVisibility(View.GONE);
                        String apppackage = "com.facebook.katana";
                        try {
                            Intent i = context.getPackageManager().getLaunchIntentForPackage(apppackage);
                            context.startActivity(i);
                        } catch (Exception  e) {
                            Toast.makeText(context, "Sorry, Facebook Not Found", Toast.LENGTH_LONG).show();
                        }
                    }else if(transcription.contains("open instagram"))
                    {
                        fbselected.setVisibility(View.GONE);
                        instaselected.setVisibility(View.VISIBLE);
                        wtsapselected.setVisibility(View.GONE);
                        contactselected.setVisibility(View.GONE);
                        blueselected.setVisibility(View.GONE);
                        facetimeselected.setVisibility(View.GONE);
                        String apppackage = "com.instagram.android";
                        try {
                            Intent i = context.getPackageManager().getLaunchIntentForPackage(apppackage);
                            context.startActivity(i);
                        } catch (Exception  e) {
                            Toast.makeText(context, "Sorry, Instagram Not Found", Toast.LENGTH_LONG).show();
                        }
                    }else if(transcription.contains("make a phone call"))
                    {
                        fbselected.setVisibility(View.GONE);
                        instaselected.setVisibility(View.GONE);
                        wtsapselected.setVisibility(View.GONE);
                        contactselected.setVisibility(View.VISIBLE);
                        blueselected.setVisibility(View.GONE);
                        facetimeselected.setVisibility(View.GONE);

                        String userName = null;
                        if(transcription.length()>17)
                        {
                            userName = transcription.substring(18);
                            Toast.makeText(context, "Phone calling to "+userName, Toast.LENGTH_SHORT).show();

                            ContentResolver resolver = context.getContentResolver();
                            Cursor cursor = resolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null, null, null,
                                    ContactsContract.Contacts.DISPLAY_NAME);


                            while (cursor.moveToNext()) {
                                long _id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data._ID));
                                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                                String phoneNu = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                if(displayName!=null) {

                                    if (displayName.equalsIgnoreCase(userName)) {
                                        Log.d("Data", _id + " " + displayName + " " +  " "+ phoneNu);


                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                                        callIntent.setData(Uri.parse("tel:"+phoneNu));
                                        context.startActivity(callIntent);
                                    }
                                }
                            }
                            cursor.close();
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }

                    } else if(transcription.contains("whatsapp audio call"))
                    {
                        fbselected.setVisibility(View.GONE);
                        instaselected.setVisibility(View.GONE);
                        wtsapselected.setVisibility(View.VISIBLE);
                        contactselected.setVisibility(View.GONE);
                        blueselected.setVisibility(View.GONE);
                        facetimeselected.setVisibility(View.GONE);

                        String userName = null;
                        if(transcription.length()>19)
                        {
                            userName = transcription.substring(20);
                            Toast.makeText(context, "WhatsApp audio calling to "+userName, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);

                            ContentResolver resolver = context.getContentResolver();
                            Cursor cursor = resolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null, null, null,
                                    ContactsContract.Contacts.DISPLAY_NAME);

                            while (cursor.moveToNext()) {
                                long _id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data._ID));
                                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                                String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

                                if(displayName!=null) {
                                    Log.d("Data", _id + " " + displayName + " " + mimeType);

                                    if (displayName.equalsIgnoreCase(userName)) {
                                        if(mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.voip.call")) {

                                            String voiceCallID = Long.toString(_id);

                                            intent.setDataAndType(Uri.parse("content://com.android.contacts/data/" + voiceCallID),
                                                    "vnd.android.cursor.item/vnd.com.whatsapp.voip.call");
                                            intent.setPackage("com.whatsapp");
                                            context.startActivity(intent);
                                        }
                                    }
                                }
                            }
                            cursor.close();
                        }else {
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }



                    }else if(transcription.contains("whatsapp video call"))
                    {
                        fbselected.setVisibility(View.GONE);
                        instaselected.setVisibility(View.GONE);
                        wtsapselected.setVisibility(View.VISIBLE);
                        contactselected.setVisibility(View.GONE);
                        blueselected.setVisibility(View.GONE);
                        facetimeselected.setVisibility(View.GONE);

                        String userName = null;
                        if(transcription.length()>19)
                        {
                            userName = transcription.substring(20);
                            Toast.makeText(context, "WhatsApp video calling to "+userName, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);

                            ContentResolver resolver = context.getContentResolver();
                            Cursor cursor = resolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null, null, null,
                                    ContactsContract.Contacts.DISPLAY_NAME);

                            while (cursor.moveToNext()) {
                                long _id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data._ID));
                                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                                String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

                                if(displayName!=null) {

                                    if (displayName.equalsIgnoreCase(userName)) {
                                        if(mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.video.call")) {
                                            Log.d("Data", _id + " " + displayName + " " + mimeType);
                                            String voiceCallID = Long.toString(_id);

                                            intent.setDataAndType(Uri.parse("content://com.android.contacts/data/" + voiceCallID),
                                                    "vnd.android.cursor.item/vnd.com.whatsapp.video.call");
                                            intent.setPackage("com.whatsapp");
                                            context.startActivity(intent);
                                        }
                                    }
                                }
                            }
                            cursor.close();
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }


                    }else if(transcription.contains("open whatsapp"))
                    {
                        fbselected.setVisibility(View.GONE);
                        instaselected.setVisibility(View.GONE);
                        wtsapselected.setVisibility(View.VISIBLE);
                        contactselected.setVisibility(View.GONE);
                        blueselected.setVisibility(View.GONE);
                        facetimeselected.setVisibility(View.GONE);

                        String apppackage = "com.whatsapp";
                        try {
                            Intent i = context.getPackageManager().getLaunchIntentForPackage(apppackage);
                            context.startActivity(i);
                        } catch (Exception  e) {
                            Toast.makeText(context, "Sorry, WhatsApp Not Found", Toast.LENGTH_LONG).show();
                        }

                    }
                    else if(transcription.contains("bluetooth on") || transcription.contains("on bluetooth") ||
                            transcription.contains("turn on bluetooth") || transcription.contains("turn the bluetooth on"))
                    {
                        fbselected.setVisibility(View.GONE);
                        instaselected.setVisibility(View.GONE);
                        wtsapselected.setVisibility(View.GONE);
                        contactselected.setVisibility(View.GONE);
                        blueselected.setVisibility(View.VISIBLE);
                        facetimeselected.setVisibility(View.GONE);

                        if (!bluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            activity.startActivityForResult(enableBtIntent, 150);
                        }

                    }else if(transcription.contains("discover devices"))
                    {
                        recyclerView.setVisibility(View.VISIBLE);
                        assistantPlay.setVisibility(View.INVISIBLE);
                        musicTheme.setVisibility(View.INVISIBLE);
                        topAssist.setVisibility(View.VISIBLE);
                        assisttext.setVisibility(View.INVISIBLE);

                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.VISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if (!bluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            activity.startActivityForResult(enableBtIntent, 150);
                        } else {
                            list.clear();
                            if(bluetoothAdapter.startDiscovery()){

                            }else{
                                Toast.makeText(context,"Something is wrong with bluetooth discovery",Toast.LENGTH_SHORT).show();
                            }
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                            filter.addAction(BluetoothDevice.ACTION_FOUND);
                            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                            activity.registerReceiver(bReceiver, filter);
                        }
                    }
                    else if(transcription.contains("paired devices"))
                    {
                        recyclerView.setVisibility(View.VISIBLE);
                        assistantPlay.setVisibility(View.INVISIBLE);
                        musicTheme.setVisibility(View.INVISIBLE);
                        topAssist.setVisibility(View.VISIBLE);
                        assisttext.setVisibility(View.INVISIBLE);

                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.VISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if (!bluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            activity.startActivityForResult(enableBtIntent, 150);
                        }else {
                            if (pairedDevices.size() > 0) {
                                for (BluetoothDevice device : pairedDevices) {
                                    deviceName = device.getName();
                                }
                            }
                            list.clear();
                            list.add(new BluetoothDevicePojo(deviceName));
                            adapter = new BluetoothClassAdapter(list, context);
                            recyclerView.setAdapter(adapter);
                        }
                    }else if(transcription.contains("spotify play me song"))
                    {
                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(transcription.length()>20) {
                            String trackName = transcription.substring(21);
                            Toast.makeText(context, trackName, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(context,SpotifyTrackPlayer.class);
                            intent.putExtra("trackName", trackName);
                            activity.startActivity(intent);
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(transcription.contains("spotify play me a song"))
                    {
                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(transcription.length()>22) {
                            String trackName = transcription.substring(23);
                            Toast.makeText(context, trackName, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(context,SpotifyTrackPlayer.class);
                            intent.putExtra("trackName", trackName);
                            activity.startActivity(intent);
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(transcription.contains("spotify play a song"))
                    {
                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(transcription.length()>19) {
                            String trackName = transcription.substring(20);
                            Toast.makeText(context, trackName, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(context,SpotifyTrackPlayer.class);
                            intent.putExtra("trackName", trackName);
                            activity.startActivity(intent);
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }

                    }else if(transcription.contains("spotify play song"))
                    {
                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(transcription.length()>17) {
                            String trackName = transcription.substring(18);
                            Toast.makeText(context, trackName, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(context,SpotifyTrackPlayer.class);
                            intent.putExtra("trackName", trackName);
                            activity.startActivity(intent);
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }

                    }
                    else if(transcription.contains("youtube play me a video"))
                    {

                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(transcription.length()>23) {
                            String videoName = transcription.substring(24);
                            Toast.makeText(context, videoName, Toast.LENGTH_SHORT).show();

                            searchOnYoutube(videoName);
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }
                    }else if(transcription.contains("youtube play me video"))
                    {
                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(transcription.length()>21) {
                            String videoName = transcription.substring(22);
                            Toast.makeText(context, videoName, Toast.LENGTH_SHORT).show();

                            searchOnYoutube(videoName);
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }
                    }else if(transcription.contains("youtube play video"))
                    {

                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(transcription.length()>18) {
                            String videoName = transcription.substring(19);
                            Toast.makeText(context, videoName, Toast.LENGTH_SHORT).show();

                            searchOnYoutube(videoName);
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }
                    }else if(transcription.contains("youtube play a video"))
                    {
                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(transcription.length()>20) {
                            String videoName = transcription.substring(21);
                            Toast.makeText(context, videoName, Toast.LENGTH_SHORT).show();

                            searchOnYoutube(videoName);
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(transcription.contains("map take me to"))
                    {

                        Toast.makeText(context, transcription.toString(), Toast.LENGTH_LONG).show();

                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(transcription.length()>14) {
                            if(!isLocationEnabled(context)){
                                Toast.makeText(context, "Please activate maps first!", Toast.LENGTH_LONG).show();
                            }else{
                                String location = transcription.substring(15);
                                Toast.makeText(context, location, Toast.LENGTH_SHORT).show();

                                Intent in = new Intent(context, MapActivity.class);
                                in.putExtra("locationName", location);
                                activity.startActivity(in);
                            }
                        }else{
                            Toast.makeText(context, "Sorry, search again!", Toast.LENGTH_SHORT).show();
                        }
                    }else if(transcription.contains("activate map"))
                    {
                        fbselected.setVisibility(View.INVISIBLE);
                        instaselected.setVisibility(View.INVISIBLE);
                        wtsapselected.setVisibility(View.INVISIBLE);
                        contactselected.setVisibility(View.INVISIBLE);
                        blueselected.setVisibility(View.INVISIBLE);
                        facetimeselected.setVisibility(View.INVISIBLE);

                        if(!isLocationEnabled(context)){

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Location");  // GPS not found
                            builder.setMessage("Want to enable location services permission?"); // Want to enable?
                            builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(intent);
                                }
                            });

                            //if no - bring user to selecting Static Location Activity
                            builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(context, "Please enable Location-based service / GPS!", Toast.LENGTH_LONG).show();
                                }

                            });
                            builder.create().show();
                        }else{
                            Toast.makeText(context, "Location-based service / GPS already enabled!", Toast.LENGTH_LONG).show();
                        }
                    }
                }else{

                    Toast.makeText(context,"Something is wrong! "+response.message(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PostSpeech> call, Throwable t) {
                Toast.makeText(context,"Fail"+ t.getMessage(),Toast.LENGTH_SHORT).show();
            }

        });
        return null;
    }

    final  public BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                list.add(new BluetoothDevicePojo(device.getName()));
                adapter = new BluetoothClassAdapter(list, context, device);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(context,"Start Scanning",Toast.LENGTH_SHORT).show();
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context,"Stop Scanning",Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void searchOnYoutube(final String keywords){
        new Thread(){

            public void run(){
                YoutubeConnector yc = new YoutubeConnector(context);
                searchResults = yc.search(keywords);
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(0).getId());
                intent.putExtra("VIDEO_TITLE",searchResults.get(0).getTitle());
                intent.putExtra("VIDEO_DESC",searchResults.get(0).getDescription());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                activity.startActivity(intent);
            }
        }.start();
    }

    private boolean isLocationEnabled(Context mContext) {
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}