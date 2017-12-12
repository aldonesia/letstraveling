package com.aldonesia.letstraveling;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GetNearbyPlacesData.markerInterface,
        GoogleMap.OnMarkerClickListener,
        ParserTask.ParserInterface,
        GoogleMap.OnInfoWindowClickListener{

    private DownloadTask downloadTask;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location LastLocation;
    private Marker CurrentLocationMarker;
    public final static int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude;
    SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
    private Marker marker;
    DatabaseHelper mydb;
    private List<MarkerOptions> MarkerDatas = new ArrayList<>();
    private List<MarkerOptions> ArrMarker = new ArrayList<>();
    private List<LatLng> ArrLatLng = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        ArrMarker= new ArrayList<>();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //set db
        mydb = new DatabaseHelper(this);
        RemoveData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission granted
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(client == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else{
                    //permission denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }



    }

    protected synchronized void buildGoogleApiClient(){
        client= new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    public void onClick(View v){
        Object dataTransfer[]= new Object[4];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData(this);
        switch (v.getId()){
            case R.id.B_hotels:
                mMap.clear();
                String Hotels = "hotel";
                String KeywordH= "hotel";
                String url = getUrl(latitude, longitude, Hotels, KeywordH);
                dataTransfer[0]= mMap;
                dataTransfer[1]= url;
                dataTransfer[2]= latitude;
                dataTransfer[3]= longitude;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Hotel", Toast.LENGTH_LONG).show();
                break;
            case R.id.B_sites:
                mMap.clear();
                String Sites = "tourist-site";
                String KeywordT = "tourist";
                url = getUrl(latitude, longitude, Sites, KeywordT);
                dataTransfer[0]= mMap;
                dataTransfer[1]= url;
                dataTransfer[2]= latitude;
                dataTransfer[3]= longitude;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Tourist Sites", Toast.LENGTH_LONG).show();
                break;
            case R.id.B_history:
                mMap.clear();
                GetRouteWithAllData();
                break;
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace, String Keyword){
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&keyword="+Keyword);
        googlePlaceUrl.append("&key=AIzaSyCqtF9qLXyU4toR0SoFD_ngHxocM1KABtw");

        Log.d("MapsActivity", "url= "+googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    private void getCustomWindow(List ArrMarkers){
        for(int i=0; i< ArrMarkers.size(); i++){
            MarkerOptions ArrMarker = (MarkerOptions) ArrMarkers.get(i);
            Log.d("info", ArrMarker.toString());
            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(MapsActivity.this);
            mMap.setInfoWindowAdapter(adapter);
            mMap.addMarker(ArrMarker);
        }
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude= location.getLatitude();
        longitude= location.getLongitude();
        LastLocation= location;

        if(CurrentLocationMarker != null){
            CurrentLocationMarker.remove();
        }

        LatLng latLng= new LatLng(location.getLatitude(), location.getLongitude());
        ArrLatLng.add(latLng);
        MarkerOptions markerOptions= new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        mydb.insertdata(markerOptions.getTitle(), "", location.getLatitude(), location.getLongitude());
        CurrentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(client != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest= new LocationRequest();

        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }

    }

    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else{
            return false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void markerDone(List<MarkerOptions> arrMarker) {
        ArrMarker= arrMarker;
        Log.d("aldo", String.valueOf(ArrMarker));

        getCustomWindow(ArrMarker);

//        Toast.makeText(MapsActivity.this, "Showing Nearby Hotels", Toast.LENGTH_LONG).show();


    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        this.marker = marker;
        LatLng origin = new LatLng(latitude, longitude);
        LatLng dest = marker.getPosition();
//        Toast.makeText(getApplicationContext(),"hai, posisimu :"+LatLng, Toast.LENGTH_LONG).show();
        String url = getDirectionUrl(origin, dest);
        if (downloadTask!=null){
            downloadTask.removePolyline();
        }
        marker.showInfoWindow();
        //munculkan button
        View b_check_in = findViewById(R.id.B_check_in);
        View b_delete = findViewById(R.id.B_delete_data);
        b_check_in.setVisibility(View.VISIBLE);
        b_delete.setVisibility(View.GONE);
        b_check_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SavingMarker(marker);
            }
        });
        mMap.setOnInfoWindowClickListener(this);

        downloadTask = new DownloadTask(mMap, this);
        downloadTask.execute(url);

//        mMap.moveCamera(CameraUpdateFactory.newLatLng(dest));
//        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        return true;
    }

    private String getDirectionUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        String sensor = "sensor=false";

        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        String output = "json";


        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        Log.d("url", url);
        return url;
    }

    @Override
    public void getDistanceAndDuration(String duration, String distance, Double duration_total, Double distance_total) {
        if (!marker.getSnippet().contains("duration")){
            marker.setSnippet(marker.getSnippet()+"\ndistance: "+ distance + ", duration: " + duration);
            marker.showInfoWindow();
        }
        else{
            Toast.makeText(getApplicationContext(),"Duration Total : "+duration_total+" minute, Distance Total : "+distance_total+" km", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "info window clicked on "+marker.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private boolean SavingMarker(Marker marker){
        String detail = marker.getSnippet();
        String details[] = detail.split("\\r?\\n");
        LatLng latlng = marker.getPosition();

        String place_name = marker.getTitle();
        String place_vicinity = details[0];
        Double place_lat = latlng.latitude;
        Double place_lng = latlng.longitude;

        boolean isInserted = mydb.insertdata(place_name, place_vicinity, place_lat, place_lng);
        if(isInserted){
            Toast.makeText(getApplicationContext(), "youre check in on "+place_name, Toast.LENGTH_SHORT).show();
            return true;
        }
        else{
            return false;
        }
    }

    private void RemoveData(){
        mydb.deleteAlldata();
    }

    private void GetRouteWithAllData(){
        //munculkan button
        View b_check_in = findViewById(R.id.B_check_in);
        View b_delete = findViewById(R.id.B_delete_data);
        b_check_in.setVisibility(View.GONE);
        b_delete.setVisibility(View.VISIBLE);
        b_delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                RemoveData();
            }
        });

        Cursor res = mydb.GetAllData();
        if(res.getCount() < 2){
            Toast.makeText(getApplicationContext(), "Data Not Found", Toast.LENGTH_LONG).show();
            return;
        }


        while(res.moveToNext()){
            MarkerOptions markerOptions = new MarkerOptions();
            String title = res.getString(1);
            String details = res.getString(2);
            Double lat = res.getDouble(3);
            Double lng = res.getDouble(4);
            Log.d("marker", title+details+lat+lng);
            LatLng latlng = new LatLng(lat,lng);
            ArrLatLng.add(latlng);
            markerOptions.position(latlng);
            markerOptions.title(title);
            markerOptions.snippet(details);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

            MarkerDatas.add(markerOptions);
        }
        Log.d("markerdata", String.valueOf(MarkerDatas));
        getCustomWindow(MarkerDatas);

        LatLng first = ArrLatLng.get(0);
        String str_origin = "origin="+first.latitude+","+first.longitude;
        LatLng frst = new LatLng(first.latitude,first.longitude);
        ArrLatLng.remove(0);

        int size = ArrLatLng.size();
        LatLng last = ArrLatLng.get(size-1);
        ArrLatLng.remove(size-1);

        Log.d("haha", first+"&"+last+"&"+String.valueOf(ArrLatLng));

        String str_dest = "destination="+last.latitude+","+last.longitude;

        String sensor = "sensor=false";

        String waypoints="";

        for(int i=0; i< ArrLatLng.size(); i++){
            LatLng point = ArrLatLng.get(i);
            if(i<1) waypoints = "waypoints=";

            if(point.equals(frst)){}
            else if(point.longitude == frst.longitude && point.latitude == frst.latitude){}
            else waypoints += point.latitude+","+point.longitude+"|";

        }

        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        Log.d("url : ", url);
        downloadTask = new DownloadTask(mMap, this);
        downloadTask.execute(url);
    }


}
