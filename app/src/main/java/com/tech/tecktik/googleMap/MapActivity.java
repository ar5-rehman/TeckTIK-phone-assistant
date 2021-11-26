package com.tech.tecktik.googleMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;
import com.tech.tecktik.R;
import com.tech.tecktik.activities.MainActivity;
import com.tech.tecktik.googleMap.googleModel.DirectionResponses;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

@SuppressLint("Registered")
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient = null;
    private static final int REQUEST_CODE = 101;
    String locationName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent locationIntent = getIntent();
        locationName = locationIntent.getStringExtra("locationName");

        if(isLocationEnabled(this)) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fetchLocation();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isLocationEnabled(this)) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fetchLocation();
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        //TODO: UI updates.
                    }
                }
            }
        };
        LocationServices.getFusedLocationProviderClient(MapActivity.this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        LocationServices.getFusedLocationProviderClient(MapActivity.this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //TODO: UI updates.
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps_view);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

            if(currentLocation!=null && locationName!=null) {

                double lat = currentLocation.getLatitude();
                double lng = currentLocation.getLongitude();

                LatLng latLngg = new LatLng(lat, lng);
                googleMap.addMarker(new MarkerOptions().position(latLngg).title("Current Location"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngg, 10.0f));

                List<Address> addressList = null;
                Address address = null;

                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(locationName, 1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addressList.size() > 0) {
                    address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("Destination"));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.0f));

                    // mMap.animateCamera(CameraUpdateFactory.zoomTo(10),100, null);
                    // Toast.makeText(getApplicationContext(), address.getLatitude() + " " + address.getLongitude(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Location not found!", Toast.LENGTH_LONG).show();
                }
                if (address != null) {
                    String toMonas = address.getLatitude() + "," + address.getLongitude();
                    String fromFKIP = lat + "," + lng;


                    ApiServices apiServices = RetrofitClient.apiServices(this);
                    apiServices.getDirection(fromFKIP, toMonas, getString(R.string.api_map_key))
                            .enqueue(new Callback<DirectionResponses>() {
                                @Override
                                public void onResponse(@NonNull Call<DirectionResponses> call, @NonNull Response<DirectionResponses> response) {
                                    drawPolyline(response);
                                    Log.d(" ", response.message());
                                }

                                @Override
                                public void onFailure(@NonNull Call<DirectionResponses> call, @NonNull Throwable t) {
                                    Log.e(" ", t.getLocalizedMessage());
                                }
                            });
                }
            }
    }

    private void drawPolyline(@NonNull Response<DirectionResponses> response) {
        if (response.body() != null) {
            if(response.body().getRoutes().size()>0) {
                String shape = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                PolylineOptions polyline = new PolylineOptions()
                        .addAll(PolyUtil.decode(shape))
                        .width(8f)
                        .color(Color.RED);
                map.addPolyline(polyline);
            }

        }
    }


    private interface ApiServices {
        @GET("maps/api/directions/json")
        Call<DirectionResponses> getDirection(@Query("origin") String origin,
                                              @Query("destination") String destination,
                                              @Query("key") String apiKey);
    }

    private static class RetrofitClient {
        static ApiServices apiServices(Context context) {
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(context.getResources().getString(R.string.base_url))
                    .build();

            return retrofit.create(ApiServices.class);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {

            }
                //code for deny
        }
    }

    private boolean isLocationEnabled(Context mContext) {
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
