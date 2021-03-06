package com.example.succour;
import android.Manifest;
import android.app.Activity;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String userToken;
    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        DatabaseReference halper = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("needer id");
        halper.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    DatabaseReference ready = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("ready");
                    ready.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){

                            }
                            else startActivity(new Intent(getApplicationContext(),Alert.class));
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
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                           // Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get new FCM registration token
                        userToken = task.getResult();
                        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("token");
                        ref.setValue(userToken);
                    }
                });

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }


    }
    public void onEmergency(View view)
    {

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                 sendmessage();
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS},101);
            }
            startActivity(new Intent(getApplicationContext(),Needer.class));
    }
    private void sendmessage() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);}
        else {
            DatabaseReference refer = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("contact");
            refer.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        String email = dataSnapshot.getValue(String.class);
                        Intent intent = new Intent(getApplicationContext(), Needer.class);
                        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                        //Get the SmsManager instance and call the sendTextMessage method to send message
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(email, null, "I am in emergency ,I need help", pi, null);
                        Toast.makeText(getApplicationContext(), email, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }

    }
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
         // sendmessage();
        }
        else{
            Toast.makeText(getApplicationContext(),"Permission deny",Toast.LENGTH_LONG).show();
        }
    }
    public void Profile(View view){
        startActivity(new Intent(getApplicationContext(),MyProfile.class));
    }
    public void Logout(View view ){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
    public void Additional(View view){
        startActivity(new Intent(getApplicationContext(),AdditionalFeature.class));
    }
    public void ClickMenu(View view){
        openDrawer(drawerLayout);
    }

    private static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }
    public static void closeDrawer(DrawerLayout drawerLayout) {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }
    public void HelperPage(View view){
        startActivity(new Intent(getApplicationContext(),MapsActivity.class));
    }
}