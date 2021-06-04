package com.example.succour;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private GpsTracker gpsTracker;
    String userToken;
    String[] permissions = new String[]{
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};
    private TextView tvLatitude,tvLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get new FCM registration token
                        userToken = task.getResult();


                        //    Toast.makeText(MainActivity.this, userToken, Toast.LENGTH_SHORT).show();
                    }
                });
        tvLatitude = (TextView)findViewById(R.id.latitude);
        tvLongitude = (TextView)findViewById(R.id.longitude);
       hasPermissions();
    }

    private void hasPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS},101);

        }

    }

    public void getLocation(View view){
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            tvLatitude.setText(String.valueOf(latitude));
            tvLongitude.setText(String.valueOf(longitude));
            FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken,"Emergency","I am in emergency please help", getApplicationContext(),MainActivity.this);
            fcmNotificationsSender.SendNotifications();
           // startActivity(new Intent(getApplicationContext(),Alert.class));
        }else{
            gpsTracker.showSettingsAlert();
        }
    }
    public void onMap(View view)
    {
        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
        finish();
    }
    public void onEmergency(View view)
    {

        sendmessage();
            startActivity(new Intent(getApplicationContext(),Needer.class));
            finish();
    }
    private void sendmessage() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refer = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("contact");
        refer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    String email = dataSnapshot.getValue(String.class);
                    Intent intent = new Intent(getApplicationContext(),Needer.class);
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                    //Get the SmsManager instance and call the sendTextMessage method to send message
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(email, null,"I am in emergency ,I need help", pi, null);
                    Toast.makeText(getApplicationContext(),email,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }


}