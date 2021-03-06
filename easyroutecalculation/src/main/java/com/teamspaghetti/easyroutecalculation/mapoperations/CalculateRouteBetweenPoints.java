package com.teamspaghetti.easyroutecalculation.mapoperations;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.teamspaghetti.easyroutecalculation.TravelMode;
import com.teamspaghetti.easyroutecalculation.listeners.RouteCalculationFinishedListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * Created by Salih on 23.08.2016.
 */
public class CalculateRouteBetweenPoints {

    /*
    *       In 'prepareAddress' we are creating a url to download our information for the route.
    *       In 'getDirectionsUrl' we are triggering an asynctask to download a json file for route.
    *
    * */

    GoogleMap googleMap;
    LatLng origin,dest;
    Context context;
    PolylineOptions polylineOptions;
    String mode;
    Boolean isLast;
    RouteCalculationFinishedListener listener;

    public CalculateRouteBetweenPoints(LatLng origin, LatLng dest, Context context, String mode, Boolean isLast, RouteCalculationFinishedListener listener){
        this.origin = origin;
        this.dest = dest;
        this.context = context;
        this.mode = mode;
        this.isLast = isLast;
        this.listener = listener;
    }
    public CalculateRouteBetweenPoints(GoogleMap map, LatLng origin, LatLng dest, Context context, int lineColor, int lineWidth,String mode,Boolean isLast, RouteCalculationFinishedListener listener){
        this.googleMap = map;
        this.origin = origin;
        this.dest = dest;
        this.context = context;
        this.mode = mode;
        this.isLast = isLast;
        this.listener = listener;
        polylineOptions = new PolylineOptions();
        polylineOptions.width(lineWidth);
        polylineOptions.color(lineColor);
    }

    public void getDirectionsUrl() throws IOException {

        String url = prepareAddress();

        DownloadTask downloadTask = new DownloadTask();

        downloadTask.execute(url);
    }

    public String prepareAddress(){

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&mode="+mode;

        return url;
    }

    // Fetches data from url passed
    public class DownloadTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);

            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }

            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserData parserData = new ParserData(result,context,polylineOptions,isLast,listener);
            parserData.execute(googleMap);
        }
    }
    public String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("ex", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
