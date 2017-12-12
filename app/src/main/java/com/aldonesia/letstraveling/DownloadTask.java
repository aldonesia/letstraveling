package com.aldonesia.letstraveling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by root on 5/12/17.
 */

public class DownloadTask extends AsyncTask<String, Void, String> {
    GoogleMap map;
    Context activity;
    private ParserTask parserTask;

    DownloadTask(GoogleMap map, Context activity){
        this.map = map;
        this.activity = activity;
    }
    @Override
    protected String doInBackground(String... url) {

        String data="";

        try {
            data=downloadUrl(url[0]);
        } catch (Exception e){
            Log.d("Background Task",e.toString());
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        parserTask = new ParserTask(map, activity);
        parserTask.execute(s);
    }


    @SuppressLint("LongLogTag")
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";

            while((line = br.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();

            br.close();
        } catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public void removePolyline() {
        parserTask.removePolyline();
    }
}
