package com.example.succour;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;

import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {
    Button search,search1,distance,fin;
    Button call;;
    private GoogleMap mMap;
    Location mLastLocation;
    Marker helpMarker;
    LatLng helperLatlng;
    String neederId;
    LatLng pick;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        search = (Button)findViewById(R.id.help);
        //search.setVisibility(View.INVISIBLE);
        search1 = (Button)findViewById(R.id.button8);
        search1.setVisibility(View.INVISIBLE);
        call = (Button)findViewById(R.id.imageButton);
        call.setVisibility(View.INVISIBLE);
        fin = (Button)findViewById(R.id.button2);
        fin.setVisibility(View.INVISIBLE);
        distance = (Button)findViewById(R.id.button7);
        distance.setVisibility(View.INVISIBLE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("ready");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String ready = snapshot.getValue(String.class);
                    if (ready.equals("true")) Need();

                } else
                    search.setText("No needer requested you");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        helperLatlng = latLng;
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Helper Available");
        GeoFire geofire = new GeoFire(ref);
        geofire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
        //stop location updates
        DatabaseReference occ = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("occupation");
        occ.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String occupation=snapshot.getValue(String.class);
                    DatabaseReference refer = FirebaseDatabase.getInstance().getReference().child(occupation);
                    GeoFire geofire1 = new GeoFire(refer);
                    geofire1.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //stop location updates
        DatabaseReference blood = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("blood");
        blood.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String occupation=snapshot.getValue(String.class);
                    DatabaseReference refer = FirebaseDatabase.getInstance().getReference().child(occupation);
                    GeoFire geofire1 = new GeoFire(refer);
                    geofire1.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void Need() {

        DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("needer id");
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    neederId = snapshot.getValue(String.class);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(neederId);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {

                                String name = snapshot.child("name").getValue(String.class);
                                search.setVisibility(View.INVISIBLE);
                                search1.setVisibility(View.VISIBLE);
                                search1.setText(name);
                                fin.setVisibility(View.VISIBLE);
                                call.setVisibility(View.VISIBLE);

                            }

                        }


                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User Info").child(neederId);
                    HashMap map = new HashMap();
                    map.put("helping", "true");
                    reference.updateChildren(map);
                    DatabaseReference needRef = FirebaseDatabase.getInstance().getReference().child("Needer").child(neederId).child("l");
                    needRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                List<Object> map = (List<Object>) snapshot.getValue();
                                double locationLat = 0;
                                double locationLng = 0;
                                if (map.get(0) != null) {
                                    locationLat = Double.parseDouble(map.get(0).toString());
                                }
                                if (map.get(1) != null) {
                                    locationLng = Double.parseDouble(map.get(1).toString());
                                }
                                pick = new LatLng(locationLat, locationLng);

                                if (helpMarker != null) {
                                    helpMarker.remove();
                                }
                                helpMarker = mMap.addMarker(new MarkerOptions().position(pick).title("I want help"));
                                getRouteToMarker(pick);
                            } else {
                                Toast.makeText(getApplicationContext(), "Currently no needer requesting you", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });


                } else {
                    Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    private void getRouteToMarker(LatLng pick) {
        Toast.makeText(getApplicationContext(), String.valueOf(pick.longitude), Toast.LENGTH_LONG).show();
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(helperLatlng, pick)
                .key("AIzaSyDGA6Y29R7ZmpYjmEiku_UJE2vPrHvfiMc")  //also define your api key here.
                .build();
        routing.execute();

    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
        //Toast.makeText(this,"here start",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
       /* if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }*/
        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;
            distance.setVisibility(View.VISIBLE);
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Integer r = new Integer(route.get(i).getDistanceValue());
            float dist = r.floatValue();
            dist = dist/1000;
            distance.setText( "distance -"+" " + String.valueOf(dist)+ " Km\nduration - "+ String.valueOf(route.get(i).getDurationValue()/60+" Minute"));
            // Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
            if(dist<100)
                finished();
            break;
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void erasePolyLines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    public void callingPhone(View view) {
        DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("needer id");
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    neederId = snapshot.getValue(String.class);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User Info").child(neederId).child("phone");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String contact = snapshot.getValue(String.class);
                                Toast.makeText(getApplicationContext(),contact,Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:"+contact));
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }

            ;
        });
    }
    public void finished(){
        final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Have You Reached?");
        passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
            undo();
        });
        passwordResetDialog.setNegativeButton("No", (dialog, which) -> {

        });
        passwordResetDialog.show();
    }
    public void finishe(View view){
        finished();
    }
    public void undo(){
        Toast.makeText(getApplicationContext(),"Thanks for using our app",Toast.LENGTH_SHORT).show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("needer id");
        ref.removeValue();
        //ref.child("helping");
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("ready");
        ref1.removeValue();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
    public void mainPage(View view){
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}