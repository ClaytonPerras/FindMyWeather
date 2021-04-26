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

    EditText location;
    TextView cityText, minTempText, unitTemp;
    Button getCityID, weatherByID;
    String locationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        location = findViewById(R.id.location);
        cityText = findViewById(R.id.cityText);
        minTempText = findViewById(R.id.temp);
        unitTemp = findViewById(R.id.unittemp);
        unitTemp.setText("C");
        getCityID = findViewById(R.id.getCityID);
        weatherByID = findViewById(R.id.weatherByID);

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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
                String latt_long = "";
                try {
                    // Retrieves city ID to be used to grab weather info
                    JSONObject cityInfo = response.getJSONObject(0);
                    cityID = cityInfo.getString("woeid");
                    locationID = cityID;
                    // Retrieves lat long to be used with Google Maps
                    JSONObject latt_longInfo = response.getJSONObject(0);
                    latt_long = latt_longInfo.getString("latt_long");
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
                            JSONArray consolidated_weather_list = response.getJSONArray("consolidated_weather");

                            //Get first item
                            JSONObject first_day = (JSONObject) consolidated_weather_list.get(0);
                            String min_temp = first_day.getString("min_temp");
                            float floatMinTemp = Float.parseFloat(min_temp);
                            float roundMinTemp = Math.round((floatMinTemp*100)/100);
                            String finalMinTemp = Float.toString(roundMinTemp);
                            cityText.setText(location.getText().toString());
                            minTempText.setText(finalMinTemp);
                            Toast.makeText(MapsActivity.this, finalMinTemp, Toast.LENGTH_SHORT).show();
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
    }
}