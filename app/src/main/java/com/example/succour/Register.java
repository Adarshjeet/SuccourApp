package com.example.succour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;


public class Register extends AppCompatActivity {

    EditText mFullName,mEmail,mPassword,mPhone,mContact;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    FirebaseDatabase rootnode;
    DatabaseReference reference;
    String userToken;
    ProgressBar progressBar;
    String item="others";
    String item1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            Toast.makeText(Register.this, "error", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get new FCM registration token
                        userToken = task.getResult();


                        //    Toast.makeText(MainActivity.this, userToken, Toast.LENGTH_SHORT).show();
                    }
                });
        mFullName   = findViewById(R.id.fullName);
        mEmail      = findViewById(R.id.Email);
        mPassword   = findViewById(R.id.password);
        mPhone      = findViewById(R.id.phone);
        mRegisterBtn= findViewById(R.id.registerBtn);
        mLoginBtn   = findViewById(R.id.createText);
        mContact = findViewById(R.id.Ephone);
        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }


        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String name = mFullName.getText().toString().trim();
                String phone = mPhone.getText().toString().trim();
                String contact = mContact.getText().toString().trim();
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }
                if(TextUtils.isEmpty(phone)){
                    mPhone.setError("Phone no. is Required.");
                    return;
                }
                if(TextUtils.isEmpty(name)){
                    mFullName.setError("Name is required");
                    return;
                }
                if(TextUtils.isEmpty(contact)){
                    mContact.setError("Emergency Contact is Required.");
                    return;
                }
                if(phone.length()!=10){
                    mPhone.setError("Contact no. must be equal to 10 digits");
                    return;
                }
                if(contact.length()!=10){
                    mContact.setError("Contact no. must be equal to 10 digits");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // register the user in firebase

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            rootnode = FirebaseDatabase.getInstance();
                            reference = rootnode.getReference("User Info");

                            Helper helper = new Helper(name,email,password,phone,userToken,contact,item,item1);
                            String userId =FirebaseAuth.getInstance().getCurrentUser().getUid();
                            reference.child(userId).setValue(helper);
                            Toast.makeText(Register.this, "Register Successfull", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                            finish();
                        }else {
                            Toast.makeText(Register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });



        mLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),Login.class));
            finish();
        });
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add(0,"Choose Occupation");
        categories.add("Others");
        categories.add("Doctor");
        categories.add("Motor Mechanic");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item = parent.getItemAtPosition(position).toString();
                if(item.equals("Choose Occupation"))item="others";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                item1 = parent.getItemAtPosition(position).toString();
                if(item1.equals("Choose Blood Group"))item1="others";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

}
