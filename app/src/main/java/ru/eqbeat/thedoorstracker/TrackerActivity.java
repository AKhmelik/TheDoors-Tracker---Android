package ru.eqbeat.thedoorstracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import android.location.Location;
import android.location.LocationManager;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import studios.codelight.smartloginlibrary.manager.UserSessionManager;

public class TrackerActivity extends AppCompatActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private GoogleMap googleMap;
    LocationManager mLocationManager;
    static String imei = "";
    String apiRequest;
    Boolean isCented = false;
    double latitudeGlobal =50;
    double longitudeGlobal =36;
    private Marker globalMarker;
    public JSONArray jsonDataResponce;

    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    public Hashtable hashPoints = new Hashtable();

    private String endPointCoreLngDefault;
    private String endPointCoreLatDefault;
    private TrackerActivity currnetActivity;
    private UserApi currentUser;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(final Location location) {
            latitudeGlobal = (location.getLatitude());
            longitudeGlobal =  (location.getLongitude());
            CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(latitudeGlobal,longitudeGlobal));

            if(!isCented){
                googleMap.moveCamera(center);
                isCented=true;
            }

            try {
                globalMarker.remove();
            } catch (Exception e) {

            }

            globalMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitudeGlobal, longitudeGlobal)).title("Current APP"));

            new GetMarketsDataTask().execute();

        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.old_activity_main);
        startService(new Intent(this, SendCoordinatesService.class));
        imei = getImei();
        currnetActivity = this;
        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeLocationManager();
        LocationListener[] mLocationListeners = new LocationListener[]{
                new LocationListener(LocationManager.GPS_PROVIDER),
                new LocationListener(LocationManager.NETWORK_PROVIDER)
        };


        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            //Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            //Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
           // Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            //Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new GetMarketsDataTask().execute();
            }
        }, 0, 5000);



        //Настройка основных кнопок
        Button btExit = (Button) findViewById(R.id.btExit);
        btExit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                stopService(new Intent(currnetActivity, SendCoordinatesService.class));
                UserSessionManager sessionManager = new UserSessionManager();
                sessionManager.setUserSession(currnetActivity, null);
                finish();
                System.exit(0);
            }
        });
    }


    private void initilizeMap() {

        googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment)).getMap();

        // check if map is created successfully or not
        if (googleMap == null) {
            Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
        }

        (findViewById(R.id.mapFragment)).getViewTreeObserver().addOnGlobalLayoutListener(
                new android.view.ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        if (android.os.Build.VERSION.SDK_INT >= 16) {
                            (findViewById(R.id.mapFragment)).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            (findViewById(R.id.mapFragment)).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                });



        //set Default coordinates
        CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(50, 36));

        googleMap.moveCamera(center);

        CameraUpdate zoom=CameraUpdateFactory.zoomTo(17);

        googleMap.animateCamera(zoom);
    }

    //Получить IMEI устройства
    public String getImei()
    {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String result = "";
        result = telephonyManager.getDeviceId();
        //Если не возможно получить IMEI, то получить AndroidID
        if (result == null)
        {
            result = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        //Если не возможно получить AndroidID, то использовать WiFi
        if (result == null)
        {
            WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            result = wifiManager.getConnectionInfo().getMacAddress();
        }
        return result;
    }

    class GetMarketsDataTask extends AsyncTask<String, Void, Void> {

        public String reqUrl;


        public void invokeWS(){

            // Show Progress Dialog
            // Make RESTful webservice call using AsyncHttpClient object

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuffer sb = new StringBuffer();

            try {
                url = new URL(reqUrl);

                urlConnection = (HttpURLConnection) url
                        .openConnection();

                InputStream is = urlConnection.getInputStream();


                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine = "";
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                String result = sb.toString();
                jsonDataResponce = new JSONArray(new String(result));



            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace(); //If you want further info on failure...
                }
            }


        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Void doInBackground(String... params) {
            currentUser = UserSessionManager.getCurrentUser(currnetActivity);

            reqUrl =  "http://"+Config.API_URL+"/index.php/metric/getdata?mid="+imei
                    +"&latitude="+latitudeGlobal
                    +"&longitude="+longitudeGlobal
                    +"&hash="+currentUser.hash;
            invokeWS();
            return null;
        }



        @Override
        protected void onProgressUpdate(Void... values) {
        }


        @Override
        protected void onPostExecute(Void result) {
            new SetMarketsDataTask().execute();

        }



    }


    class SetMarketsDataTask extends AsyncTask<String, Void, Void> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Void doInBackground(String... params) {
            String test = "test";
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }


        @Override
        protected void onPostExecute(Void result) {


            try {
                setMarkers();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        public void setMarkers() throws JSONException {


            if(jsonDataResponce != null){

                for(int i = 0 ; i < jsonDataResponce.length(); i++){
                    JSONObject obj = jsonDataResponce.getJSONObject(i);

                    String request = (String) obj.get("request");
                    String endPointCoreLng = (String) obj.get("endPointCoreLng");
                    String endPointCoreLat = (String) obj.get("endPointCoreLat");


                    if(!endPointCoreLng.equals(endPointCoreLngDefault) || !endPointCoreLat.equals(endPointCoreLatDefault)){
                        googleMap.clear();
                        getLatLongFromAddress(obj);

                        apiRequest=request;
                        endPointCoreLngDefault = endPointCoreLng;
                        endPointCoreLatDefault = endPointCoreLat;


                    }

                    JSONArray point = (JSONArray) obj.get("point");

                    for(int ii = 0 ; ii < point.length(); ii++) {
                        JSONObject objMarket = point.getJSONObject(ii);

                        String pointLatitade = (String) objMarket.get("latitude");
                        String pointLongitude = (String) objMarket.get("longitude");
                        String id = (String) objMarket.get("id");
                        Double latitude = Double.parseDouble(pointLatitade);
                        Double longitude = Double.parseDouble(pointLongitude);




                        Hashtable hashPointsLocal = (Hashtable) hashPoints.get(id);

                        if(hashPointsLocal!=null) {

                            Marker markerOld = (Marker) hashPointsLocal.get("marker");
                            markerOld.remove();
                            hashPoints.remove(id);
                        }

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(id);

                        Marker marker = googleMap.addMarker(markerOptions);



                        Hashtable pointsAdvanced = new Hashtable();

                        pointsAdvanced.put("marker", marker);
                        hashPoints.put(id, pointsAdvanced);

                    }



                    // createCustomMarker(obj);
                }


            }
        }
    }

    public void getLatLongFromAddress( JSONObject youraddress) {

        try {
            String endPointCoreLat = (String)youraddress.get("endPointCoreLat");
            String endPointCoreLng = (String)youraddress.get("endPointCoreLng");
            String address = (String)youraddress.get("request");

            this.setTitle(address);
            if(endPointCoreLat!=null && endPointCoreLng!=null){


                Double latitude = Double.parseDouble(endPointCoreLat);
                Double longitude = Double.parseDouble(endPointCoreLng);

                if(!latitude.equals(0.0) && !longitude.equals(0.0)){


                    LatLng origin = new LatLng(latitudeGlobal, longitudeGlobal);
                    LatLng dest = new LatLng(latitude, longitude);


                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }




    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){


        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                //Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
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
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){

        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);
        }
    }
}
