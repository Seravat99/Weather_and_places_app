package com.example.trabalhofinal;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.GeoDataClient;
import com.google.android.libraries.places.compat.Place;
import com.google.android.libraries.places.compat.PlaceBufferResponse;
import com.google.android.libraries.places.compat.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    SupportMapFragment mapFragment;
    SearchView SearchBar;
    String[] permissions;
    String search_location;
    FusedLocationProviderClient fusedLocationClient;
    Boolean empty_search = true;
    FloatingActionButton Home_button;
    FloatingActionButton Places_button;
    FirebaseAuth firebaseAuth;
    private ListView settings_list;

    MutableLiveData<String> _PlacesInfo = new MutableLiveData<String>();
    MutableLiveData<String> _messageStatus = new MutableLiveData<String>();
    LiveData<String> PlacesInfo = _PlacesInfo;
    LiveData<String> messageStatus = _messageStatus;

    GeoDataClient geoDataClient;

    String key = "AIzaSyC0HIrjG6HwL5qqNBL-OpWyguZgn0HgwEg";
    StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
    StringBuilder LocalstringBuilder = new StringBuilder();
    LatLng localization;


    DrawerLayout drawerLayout;
    NavigationView navView;
    FloatingActionButton settingsButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        firebaseAuth = firebaseAuth.getInstance();

        messageStatus.observe(MapsActivity.this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(MapsActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });

        Home_button = findViewById(R.id.home_button);
        Places_button = findViewById(R.id.places_button);
        SearchBar = findViewById(R.id.search_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //drawerLayout = findViewById(R.id.drawerLayout);
        settingsButton = findViewById(R.id.settingsButton);

        geoDataClient = Places.getGeoDataClient(this);
        //navView = findViewById(R.id.navView);

        //settings_list = (ListView) findViewById(R.menu.setting_list);

        SearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search_location = SearchBar.getQuery().toString();
                List<Address> addressList = null;
                if (search_location != null || !search_location.equals("")) {
                    empty_search = false;
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(search_location, 1);
                    } catch (IOException e) {
                        empty_search = true;
                    }
                    if (!addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        LatLng local = new LatLng(address.getLatitude(), address.getLongitude());
                        GetNearbyPlaces(local);
                        localization = local;
                        if (mMap != null) {
                            mMap.clear();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(local, 13.5f));
                            mMap.addMarker(new MarkerOptions().position(local).title("Local Mark"));
                            mMap.getUiSettings().setAllGesturesEnabled(true);
                        }
                    }
                } else {
                    empty_search = true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        if (empty_search == true) {
            getLocation();
        }

        Home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        Places_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent places = new Intent(MapsActivity.this, ListPlaceTypeActivity.class);
                places.putExtra("latitude", localization.latitude);
                places.putExtra("longitude", localization.longitude);
                startActivity(places);
            }
        });

        mapFragment.getMapAsync(this);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent places = new Intent(MapsActivity.this, EditProfileActivity.class);
                places.putExtra("latitude", localization.latitude);
                places.putExtra("longitude", localization.longitude);
                startActivity(places);
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnPoiClickListener(poi -> {
            String placeId = poi.placeId;

            // Get the details of the place using the place ID
            final Task<PlaceBufferResponse> placeResult = geoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Place place = task.getResult().get(0);
                    LatLng local = place.getLatLng();
                    // Place contains the details of the place, including its name and types
                    Log.i(TAG, "Place name: " + place.getName());
                    Log.i(TAG, "Place types: " + place.getPlaceTypes());
                    // Add a custom marker to the map at the clicked location
                    GetNearbyPlaces(local);
                    if (mMap != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(local).title("My marker"));
                        localization = local;
                    }
                }
            });
        });
    }



    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MapsActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(MapsActivity.this, permissions, 0);
            mMap.setMyLocationEnabled(true);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng local = new LatLng(location.getLatitude(), location.getLongitude());
                        if (mMap != null) {
                            mMap.clear();
                            GetNearbyPlaces(local);
                            mMap.addMarker(new MarkerOptions().position(local).title("Local Mark"));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(local, 13.5f));
                            mMap.getUiSettings().setAllGesturesEnabled(true);
                            localization = local;
                        }
                    }
                }
            });
        }
    }

    private void GetNearbyPlaces(LatLng local) {
        StringBuilder LocalSBuild = new StringBuilder(stringBuilder);
        LocalSBuild.append("location=" + local.latitude + "," + local.longitude);
        LocalSBuild.append("&radius=1000");
        LocalSBuild.append("&key=" + key);

        String url = LocalSBuild.toString();
        LocalstringBuilder = LocalSBuild;
        RequestPlaceQueue(url);
    }

    private void RequestPlaceQueue(String NewUrl) {
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
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Toast.makeText(getApplicationContext(), "Tocaste", Toast.LENGTH_SHORT).show();
        //noinspection SimplifiableIfStatement
        // if (id == R.id.setting_list) {
        //return true;
        //}

        return super.onOptionsItemSelected(item);
    }*/

    /*private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.profile:

                startActivity(new Intent(MapsActivity.this, EditProfileActivity.class));
                _messageStatus.setValue("Edit Profile");
                break;
            case R.id.summary:


                //startActivity(new Intent(MapsActivity.this, SummaryActivity.class));
                _messageStatus.setValue("Summary");
                break;
            case R.id.logout:
                Toast.makeText(MapsActivity.this, "Log Out", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                Intent mainActivity = new Intent (MapsActivity.this, LoginActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainActivity);
                finish();  //fez-se desta maneira de modo a que o utilizador nao consiga voltar atras /////////////////////////////////////////////////7
                //startActivity(new Intent(MapsActivity.this, LoginActivity.class)); ////////////////////////////////////////////////////////// basta np+e
                _messageStatus.setValue("Log Out");
                break;
            default:
                _messageStatus.setValue("Invalido");
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            //ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        FragmentContainerView mapView = (FragmentContainerView) findViewById(R.id.map);
        mapView.bringToFront();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.END);


    }*/


    //@SuppressWarnings("StatementWithEmptyBody")
    //@Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//
//        //calling the method displayselectedscreen and passing the id of selected menu
//        displaySelectedScreen(item.getItemId());
//        //make this method blank
//        return true;
//    }
//
}



