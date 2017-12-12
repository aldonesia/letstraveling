package com.aldonesia.letstraveling;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 27/11/17.
 */

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    String googlePlacesData;
    GoogleMap mMap;
    String url;
    Double latitude, longitude;
    private List<MarkerOptions> ArrMarker = new ArrayList<>();
    markerInterface intface;

    GetNearbyPlacesData(Context activity){
        intface = (markerInterface) activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        latitude = (Double)objects[2];
        longitude = (Double)objects[3];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlacesData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String, String>> nearbyPlaceList= null;
        DataParser parser= new DataParser();
        nearbyPlaceList= parser.parse(s);
        showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList){

        Location CurLoc = new Location("Current Location");
        CurLoc.setLatitude(latitude);
        CurLoc.setLongitude(longitude);
//        ArrMarker = new ArrayList<>();

        for(int i=0; i<nearbyPlaceList.size(); i++){
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            String placeName= googlePlace.get("place_name");
            String vicinity= googlePlace.get("vicinity");
            double lat= Double.parseDouble( googlePlace.get("lat"));
            double lng= Double.parseDouble( googlePlace.get("lng"));

            Location PlaceLoc = new Location("Place Location");
            PlaceLoc.setLatitude(lat);
            PlaceLoc.setLongitude(lng);

            NumberFormat formatter = NumberFormat.getNumberInstance();
            formatter.setMinimumFractionDigits(0);
            formatter.setMaximumFractionDigits(2);
            double dis = CurLoc.distanceTo(PlaceLoc)/1000;
            String distance = formatter.format(dis);
//            String reference= googlePlace.get("reference");

            String title= ""+placeName+"";
            String details= vicinity+"\nDistance from your location is " +distance+ " km";
//            String details= "Distance from your location is " +distance+ " km";

            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(title);
            markerOptions.snippet(details);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

            ArrMarker.add(markerOptions);

//            CustomInfoWindowAdapter adapter= new CustomInfoWindowAdapter();
//            mMap.setInfoWindowAdapter(adapter);
//
//            mMap.addMarker(markerOptions).showInfoWindow();

//            mMap.addMarker(markerOptions);
//            mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        }
        Log.d("show", String.valueOf(ArrMarker));
//        return ArrMarker;
        intface.markerDone(ArrMarker);
    }

    public List<MarkerOptions> getArrMarker() {
        Log.d("get", String.valueOf(ArrMarker));
        return ArrMarker;
    }

    public interface markerInterface{
        void markerDone(List<MarkerOptions> arrMarker);
    }


}

//masukin ke array semua marker nya

//parsing ke mapsActivity

//baru show marker