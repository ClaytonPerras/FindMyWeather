package com.example.testmap;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double gLatt, gLong;
    EditText location;
    TextView cityText, minTempText, unitTemp, tempHigh, weather_state, unitTemp2;
    Button getCityID, weatherByID;
    String locationID;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        location = findViewById(R.id.location);
        cityText = findViewById(R.id.cityText);
        minTempText = findViewById(R.id.temp);
        unitTemp = findViewById(R.id.unittemp);
        unitTemp.setText("C");
        getCityID = findViewById(R.id.getCityID);
        weatherByID = findViewById(R.id.weatherByID);
        tempHigh = findViewById(R.id.tempHigh);
        weather_state = findViewById(R.id.weatherState);
        unitTemp2 = findViewById(R.id.unittemp2);
        unitTemp2.setText("C");

        getCityID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });

        weatherByID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeather();
            }
        });

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

        // Add a marker in Sydney and move the camera
        LatLng cityLattLong = new LatLng(gLatt, gLong);
        mMap.addMarker(new MarkerOptions().position(cityLattLong).title("Your Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cityLattLong));
    }

    public void getData() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
        String url ="https://www.metaweather.com/api/location/search/?query="+location.getText().toString();

        // Request a string response from the provided URL.
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                String cityID = "";
                try {
                    // Retrieves city ID to be used to grab weather info
                    JSONObject cityInfo = response.getJSONObject(0);
                    cityID = cityInfo.getString("woeid");
                    locationID = cityID;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MapsActivity.this, "City ID: "+cityID, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Failed in getData()", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }

    public void getWeather() {

        RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
        String url ="https://www.metaweather.com/api/location/"+locationID;

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Grabbing JSON array of weaather
                            JSONArray consolidated_weather_list = response.getJSONArray("consolidated_weather");
                            //Grabbing latt-long of city for Google Maps
                            String latt_long = response.get("latt_long").toString();
                            String lattC = latt_long.substring( 0, latt_long.indexOf(","));
                            String longC = latt_long.substring(latt_long.indexOf(",")+1, latt_long.length());
                            gLatt = Double.parseDouble(lattC);
                            gLong = Double.parseDouble(longC);
                            System.out.println(gLatt);
                            System.out.println(gLong);
                            //Get first weather day item
                            JSONObject first_day = (JSONObject) consolidated_weather_list.get(0);
                            String min_temp = first_day.getString("min_temp");
                            String max_temp = first_day.getString("max_temp");
                            // Convert high and low temp to float for rounding then back to string
                            float floatMaxTemp = Float.parseFloat(max_temp);
                            float roundMaxTemp = Math.round((floatMaxTemp*100)/100);
                            String finalMaxTemp = Float.toString(roundMaxTemp);
                            String weatherState = first_day.getString("weather_state_name");
                            float floatMinTemp = Float.parseFloat(min_temp);
                            float roundMinTemp = Math.round((floatMinTemp*100)/100);
                            String finalMinTemp = Float.toString(roundMinTemp);
                            // Set text views with new data
                            cityText.setText(location.getText().toString());
                            minTempText.setText("Low: "+finalMinTemp);
                            tempHigh.setText("High: "+finalMaxTemp);
                            weather_state.setText(weatherState);
                            mapFragment.getMapAsync(MapsActivity.this::onMapReady);
                            Toast.makeText(MapsActivity.this, longC, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(MapsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(request);
        System.out.println(gLatt);
        System.out.println(gLong);
    }
}