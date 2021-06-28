package com.example.succour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.Timer;
import java.util.TimerTask;

public class AdditionalFeature extends FragmentActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private GoogleMap mMap;
    Location mLastLocation;
    Button search,cancel,f;
    String s;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    LatLng pickUp;
    String userId;
    String userToken;
    private int radius = 1;
    private Boolean helperFound =false;
    private  String helperFoundId;
    private Marker helpMarker;
    LatLng helperLatLng;
    boolean flag=false;
    Button distance,doctor,motor,blood,search1;
    Button call;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    ArrayList<String> arr = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_feature);
        doctor = (Button)findViewById(R.id.btnDoctor);
        motor = (Button)findViewById(R.id.btnMechanic);
        blood = (Button)findViewById(R.id.button10);
        search = (Button)findViewById(R.id.button7);
        search.setVisibility(View.INVISIBLE);
        search1 = (Button)findViewById(R.id.button15);
        search1.setVisibility(View.INVISIBLE);
        f = (Button)findViewById(R.id.button3);
        f.setVisibility(View.INVISIBLE);
        cancel = (Button)findViewById(R.id.button);
        cancel.setVisibility(View.INVISIBLE);
        call = (Button)findViewById(R.id.imageButton);
        call.setVisibility(View.INVISIBLE);
        distance = (Button)findViewById(R.id.button8);
        distance.setVisibility(View.INVISIBLE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        DatabaseReference refer = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("helping");
        refer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String helpingg = snapshot.getValue(String.class);
                    assert helpingg != null;
                    if(helpingg.equals("true")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("keyForHelp");
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                String help = snapshot.getValue(String.class);
                                doctor.setVisibility(View.INVISIBLE);
                                motor.setVisibility(View.INVISIBLE);
                                blood.setVisibility(View.INVISIBLE);
                                if(help.equals("Motor Mechanic"))
                                    getHelperLocation("Motor Mechanic");
                                else
                                    if(help.equals(("Doctor")))getHelperLocation("Doctor");
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
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
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
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
        pickUp=latLng;
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        // mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Needer");
        GeoFire geofire = new GeoFire(ref);
        geofire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    public void Logout(View view)
    {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
    public void Search(View view){
        doctor.setVisibility(View.INVISIBLE);
        motor.setVisibility(View.INVISIBLE);
        blood.setVisibility(View.INVISIBLE);
        search1.setVisibility(View.VISIBLE);
        getHelper("Motor Mechanic");
    }
    public  void Doctor(View view){
        doctor.setVisibility(View.INVISIBLE);
        motor.setVisibility(View.INVISIBLE);
        search1.setVisibility(View.VISIBLE);
        blood.setVisibility(View.INVISIBLE);
        getHelper("Doctor");
    }
int count=0;
    private void getHelper(String key1){
        DatabaseReference keys =FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("keyForHelp");
        keys.setValue(key1);
        DatabaseReference helperReference = FirebaseDatabase.getInstance().getReference().child(key1);
        GeoFire geofire = new GeoFire(helperReference);
        GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(pickUp.latitude, pickUp.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(radius>5){
                   search1.setText("No User Found");
                    helperFound=true;
                }
                DatabaseReference h = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId);
                HashMap map1 = new HashMap();
                map1.put("helping","false");
                h.updateChildren(map1);
                if(!helperFound && !key.equals(userId) && !arr.contains(key)){
                    helperFound= true;
                    helperFoundId=key;
                    if(key1.equals("A+")||key1.equals("B+")||key1.equals("B-")
                    ||key1.equals("A-")||key1.equals("O+")||key1.equals("O-")
                    ||key1.equals("AB-")||key1.equals("AB+")){
                       // count++;
                        helperFound=false;
                        DatabaseReference blood = FirebaseDatabase.getInstance().getReference().child("User Info").child(key);
                        blood.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Spinner s = (Spinner)findViewById(R.id.spinner1);
                                    s.setVisibility(View.INVISIBLE);
                                    call.setVisibility(View.VISIBLE);
                                    search1.setVisibility(View.INVISIBLE);
                                    call.setClickable(false);
                                    String name = snapshot.child("name").getValue(String.class);
                                    String con = snapshot.child("phone").getValue(String.class);
                                    if(count==0)call.setText(name+" "+con);
                                    else call.setText(call.getText().toString()+"\n"+name+" "+con);
                                    arr.add(key);
                                    radius--;
                                    count++;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                        if(radius>5){
                            helperFound=true;
                            search1.setText("No User Found");
                        }
                    }
                    else{
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(key).child("token");
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){
                                userToken = dataSnapshot.getValue(String.class);
                                 sendNotification();
                                //Toast.makeText(getApplicationContext(),userToken,Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User Info").child(key);

                    Toast.makeText(getApplicationContext(),key,Toast.LENGTH_LONG).show();
                    HashMap map = new HashMap();
                    map.put("needer id",userId);
                    reference.updateChildren(map);
                    search1.setText("found and waiting for response");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            DatabaseReference refer = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("helping");
                            refer.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        String s=snapshot.getValue(String.class);
                                        if(s.equals("true")) {
                                            DatabaseReference helper =FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("helper");
                                            helper.setValue(helperFoundId);

                                            DatabaseReference help = FirebaseDatabase.getInstance().getReference().child("User Info").child(key);
                                            help.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        search1.setVisibility(View.INVISIBLE);
                                                        String name = snapshot.child("name").getValue(String.class);
                                                        search.setText(name);
                                                        call.setVisibility(View.VISIBLE);

                                                    }

                                                }


                                                @Override
                                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                }
                                            });

                                            getHelperLocation(key1);
                                        }else{
                                            helperReference.removeEventListener(this);
                                            searchAgain(key1);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });
                        }
                    },20000);

                }}
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!helperFound && radius<5 ){
                    radius++;
                    if(radius==4)flag = true;
                    getHelper(key1);
                }
                else{
                    if(flag)
                    search1.setText("No User Found");
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private void sendNotification() {
        FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken,"Emergency","I am in emergency please help", getApplicationContext(), AdditionalFeature.this);
        fcmNotificationsSender.SendNotifications();
    }

    private void getHelperLocation(String key1){
        DatabaseReference refer1 = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("helper");
        refer1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    helperFoundId = snapshot.getValue(String.class);

        DatabaseReference help = FirebaseDatabase.getInstance().getReference().child("User Info").child(helperFoundId);
        help.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String name = snapshot.child("name").getValue(String.class);
                    search.setVisibility(View.INVISIBLE);
                    search1.setVisibility(View.INVISIBLE);
                    String phone = snapshot.child("phone").getValue(String.class);
                    search.setVisibility(View.VISIBLE);
                    search.setText(name);
                    call.setVisibility(View.VISIBLE);

                }

            }


            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        DatabaseReference refHelper = FirebaseDatabase.getInstance().getReference().child(key1).child(helperFoundId).child("l");
        refHelper.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                  //  Toast.makeText(getApplicationContext(),"king2",Toast.LENGTH_LONG).show();
                    List<Object> map = (List<Object>)snapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0)!= null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!= null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    helperLatLng = new LatLng(locationLat,locationLng);
                    Toast.makeText(getApplicationContext(),String.valueOf(locationLat),Toast.LENGTH_LONG).show();
                    if(helpMarker!=null){
                        helpMarker.remove();
                    }
                    helpMarker= mMap.addMarker(new MarkerOptions().position(helperLatLng).title("your helper here"));
                    getRouteToMarker(pickUp);
              }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });
    }
    private void getRouteToMarker(LatLng pick){

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(pick,helperLatLng)
                .key("AIzaSyDGA6Y29R7ZmpYjmEiku_UJE2vPrHvfiMc")  //also define your api key here.
                .build();
        routing.execute();

    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
        f.setVisibility(View.VISIBLE);
        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
            distance.setVisibility(View.VISIBLE);
            Integer r = new Integer(route.get(i).getDistanceValue());
            float dist = r.floatValue();
            dist = dist/1000;
            cancel.setVisibility(View.VISIBLE);
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
    private void erasePolyLines(){
        for(Polyline line : polylines){
            line.remove();

        }
        polylines.clear();
    }
    public void calling(View view){
        DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("User Info").child(helperFoundId).child("phone");
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
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
    public void bloodDonor(View view){
        doctor.setVisibility(View.INVISIBLE);
        motor.setVisibility(View.INVISIBLE);
        //blood.setVisibility(View.INVISIBLE);

        Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);

        // Spinner Drop down elements
        List<String> categories1 = new ArrayList<String>();
        categories1.add(0,"Choose Blood Group");
        categories1.add("A+");
        categories1.add("A-");
        categories1.add("B+");
        categories1.add("B-");
        categories1.add("AB+");
        categories1.add("AB-");
        categories1.add("O+");
        categories1.add("O-");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories1);

        // Drop down layout style - list view with radio button
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner1.setAdapter(dataAdapter1);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item1 = parent.getItemAtPosition(position).toString();
                if(!item1.equals("Choose Blood Group"))
                    search1.setVisibility(View.VISIBLE);
                   // spinner1.setVisibility(View.INVISIBLE);
                    search.setText("");
                    search.setVisibility(View.INVISIBLE);
                    getHelper(item1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void finished(){
        final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Does Helper Reached?");
        passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
            undo();
        });
        passwordResetDialog.setNegativeButton("No", (dialog, which) -> {

        });
        passwordResetDialog.show();
    }
    public void undo(){
        Toast.makeText(getApplicationContext(),"Thanks for using our app",Toast.LENGTH_SHORT).show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("helper");
        ref.removeValue();
        //ref.child("helping");
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("helping");
        ref1.removeValue();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
    public void cancel(View view){
        final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Do you want to cancel the help request");
        passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
            cancel1();
        });
        passwordResetDialog.setNegativeButton("No", (dialog, which) -> {

        });
        passwordResetDialog.show();
    }
    public void cancel1(){
        sendNotification1();
        DatabaseReference ref  = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("helping");
        ref.removeValue();
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("helper");
        ref1.removeValue();
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("keyForHelp");
        ref2.removeValue();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
    public void sendNotification1(){
        FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken,"Emergency","User cancel the help request", getApplicationContext(), AdditionalFeature.this);
        fcmNotificationsSender.SendNotifications();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(helperFoundId).child("ready");
        ref.removeValue();
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("User Info").child(helperFoundId).child("needer id");
        ref1.removeValue();
    }
    public void sendNotification1(String x){
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("User Info").child(helperFoundId).child("needer id");
        ref1.removeValue();
    }
    public void searchAgain(String key1){
        sendNotification1("s");
        //search.setText("again go");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("helping");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String t = snapshot.getValue(String.class);
                    if(t.equals("false")){
                        Toast.makeText(getApplicationContext(), "again", Toast.LENGTH_LONG).show();
                        arr.add(helperFoundId);
                        radius--;
                        helperFound = false;
                        getHelper(key1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }
    public void sd(View view){
        finished();
    }
}