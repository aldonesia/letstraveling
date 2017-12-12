package com.aldonesia.letstraveling;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Integer.parseInt;

/**
 * Created by root on 5/12/17.
 */

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    private GoogleMap mMap;
    Polyline polyline = null;
    private String duration;
    private String distance;
    private Double duration_total = 0.0;
    private Double distance_total = 0.0;
    ParserInterface parserInterface;

    ParserTask(GoogleMap map, Context activity){
        mMap = map;
        parserInterface = (ParserInterface) activity;
    }
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try{
            jObject = new JSONObject(jsonData[0]);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            routes= parser.parse(jObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();
        distance = "";
        duration = "";

        if(result.size()<1){
            //no points
            return;
        }

        for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();
            List<HashMap<String, String>> path = result.get(i);

            for(int j=0;j<path.size();j++){
                HashMap<String, String> point = path.get(j);

                if(j==0){
                    distance = (String)point.get("distance");
                    String[] distances = distance.split("\\s+");
                    Double distance_double = Double.parseDouble(distances[0]);
                    Log.d("a",distance_double.toString());
                    if(distance_double > 0) distance_total += distance_double;
                    continue;
                }
                else if(j==1){
                    duration = (String)point.get("duration");
                    String[] durations = duration.split("\\s+");
                    Double duration_double = Double.parseDouble(durations[0]);
                    Log.d("a",duration_double.toString());
                    if(duration_double > 0) duration_total += duration_double;
                    continue;
                }
                Log.w("lat", point.get("lat") +"tes"+ point.get("lng"));

                if(point.get("lat") == null || point.get("lng") == null){}
                else{
                    Double lat = Double.parseDouble(point.get("lat"));
                    Double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);
                    points.add(position);
                }

            }
            lineOptions.addAll(points);
            lineOptions.width(3);
            lineOptions.color(Color.RED);
        }
        //kalo mau set text duration + distance disini

        parserInterface.getDistanceAndDuration(duration, distance, duration_total, distance_total);

        polyline = mMap.addPolyline(lineOptions);
        Log.d("apainiiiii", distance + "    " + duration);
    }
    public void removePolyline(){
        try{
            polyline.remove();
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
    }

    public interface  ParserInterface{
        void getDistanceAndDuration(String duration, String distance, Double duration_total, Double distance_total);

    }
}
