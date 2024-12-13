package com.example.trabalhofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class ListPlacesActivity extends AppCompatActivity implements LifecycleOwner {
    MutableLiveData<String> _PlacesInfo = new MutableLiveData<String>();
    MutableLiveData<String> _messageStatus = new MutableLiveData<String>();
    LiveData<String> PlacesInfo = _PlacesInfo;


    String key = "AIzaSyC0HIrjG6HwL5qqNBL-OpWyguZgn0HgwEg";
    StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
    StringBuilder LocalstringBuilder = new StringBuilder();
    double latitude;
    double longitude;
    String type;
    List<Double> lat_type = Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    List<Double> lng_type = Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listplaces);

        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        type = getIntent().getStringExtra("type");

        TextView TV_type = (TextView) findViewById(R.id.type);
        TV_type.setText(type);

        PlacesInfo.observe(ListPlacesActivity.this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                try {
                    showPlaces(s, type, latitude, longitude);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        for(int j = 0; j < 10; j++){
            String type_image = "type" + (j+1) + "_image";
            int resID_image = getResources().getIdentifier(type_image, "id", getPackageName());
            findViewById(resID_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String type_image = getResources().getResourceEntryName(v.getId());
                    String type = type_image.substring(4, type_image.length()-6);
                    GoogleMap mMap = MapsActivity.mMap;
                    mMap.clear();
                    LatLng local = new LatLng(lat_type.get(Integer.parseInt(type)-1), lng_type.get(Integer.parseInt(type)-1));
                    mMap.addMarker(new MarkerOptions().position(local).title("Local Mark"));
                    finish();
                }
            });
        }

        GetNearbyTypePlaces(type, latitude, longitude);
    }

    private void RequestPlaceQueue(String NewUrl){
        // Instantiate the RequestQueue.
        _messageStatus.setValue("getting places from server");
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringReq = new StringRequest(Request.Method.GET, NewUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        _PlacesInfo.setValue(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                _messageStatus.setValue("Could not get places information");
            }
        });
        queue.add(stringReq);
    }


    private void GetNearbyTypePlaces(String type, double latitude, double longitude){
        StringBuilder LocalSBuild = new StringBuilder(stringBuilder);
        LocalSBuild.append("location=" + latitude + "," + longitude);
        LocalSBuild.append("&radius=1000");
        LocalSBuild.append("&type="+type);
        LocalSBuild.append("&key="+key);
        String url = LocalSBuild.toString();
        LocalstringBuilder = LocalSBuild;
        RequestPlaceQueue(url);
    }


    private void showPlaces(String jsonPlaces, String type, double latitude, double longitude) throws JSONException {
        JSONObject obj = new JSONObject(jsonPlaces);
        JSONArray objResults = obj.getJSONArray("results");
        if(objResults.length() > 0) {
            for(int i = 0; i < 10; i++) {
                try {
                    JSONObject objPlace = objResults.getJSONObject(i);
                    String type_name = "type" + (i+1) + "_name";
                    int resID_name = getResources().getIdentifier(type_name, "id", getPackageName());
                    TextView TV = (TextView) findViewById(resID_name);
                    TV.setText(objPlace.getString("name"));
                    JSONObject objGeometry = objPlace.getJSONObject("geometry");
                    JSONObject objLocation = objGeometry.getJSONObject("location");
                    lat_type.set(i, objLocation.getDouble("lat"));
                    lng_type.set(i, objLocation.getDouble("lng"));
                    try {
                        JSONArray objPhotos = objPlace.getJSONArray("photos");
                        JSONObject objPhotoVariables = objPhotos.getJSONObject(0);
                        String photoReference = objPhotoVariables.getString("photo_reference");
                        int maxWidth = 324;
                        String url = "https://maps.googleapis.com/maps/api/place/photo?photoreference=" + photoReference + "&maxwidth=" + maxWidth + "&key=" + key;
                        String type_image = "type" + (i+1) + "_image";
                        int resID_image = getResources().getIdentifier(type_image, "id", getPackageName());
                        ImageButton IB = (ImageButton) findViewById(resID_image);
                        Glide.with(this).load(url).into(IB);
                    } catch (JSONException e) {
                        String url = objPlace.getString("icon");
                        String type_image = "type" + (i+1) + "_image";
                        int resID_image = getResources().getIdentifier(type_image, "id", getPackageName());
                        ImageButton IB = (ImageButton) findViewById(resID_image);
                        Glide.with(this).load(url).into(IB);
                    }
                } catch (JSONException e) {
                    String type_name = "type" + (i+1) + "_card";
                    int resID_card = getResources().getIdentifier(type_name, "id", getPackageName());
                    CardView CV = (CardView) findViewById(resID_card);
                    ViewGroup.LayoutParams params = CV.getLayoutParams();
                    params.height = 0;
                    CV.setLayoutParams(params);
                }
            }
        }
    }
}
