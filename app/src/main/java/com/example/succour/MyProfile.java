package com.example.succour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class MyProfile extends AppCompatActivity {

    EditText mFullName,mEmail,mPassword,mPhone,mContact;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    String fname,email,password,phone,contact;
    ProgressBar progressBar;
    DatabaseReference reference;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = fAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_my_profile);
        mFullName   = findViewById(R.id.fullName);
        if(user==null)Toast.makeText(getApplicationContext(),"kgjksf",Toast.LENGTH_SHORT).show();
        mEmail      = findViewById(R.id.Email);
        mPassword   = findViewById(R.id.password);
        mPhone      = findViewById(R.id.phone);
        mRegisterBtn= findViewById(R.id.registerBtn);
        mLoginBtn   = findViewById(R.id.createText);
        mContact = findViewById(R.id.Ephone);
        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        reference= FirebaseDatabase.getInstance().getReference().child("User Info").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    fname = snapshot.child("name").getValue(String.class);
                    email = snapshot.child("email").getValue(String.class);
                    contact = snapshot.child("contact").getValue(String.class);
                    phone = snapshot.child("phone").getValue(String.class);
                    mFullName.setText(fname);
                    mEmail.setText(email);
                    mContact.setText(contact);
                    mPhone.setText(phone);

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }
    public void updateDetails(View view) {
        progressBar.setVisibility(View.VISIBLE);
        String cname,cemail,ccontact,cphone;
        cname = mFullName.getText().toString();
        cemail = mEmail.getText().toString();
        ccontact = mContact.getText().toString();
        cphone = mPhone.getText().toString();
        if(TextUtils.isEmpty(cphone)){
            mPhone.setError("Phone no. is Required.");
            return;
        }
        if(TextUtils.isEmpty(ccontact)){
            mContact.setError("Emergency Contact is Required.");
            return;
        }
        if(cphone.length()!=10){
            mPhone.setError("Contact no. must be equal to 10 digits");
            return;
        }
        if(ccontact.length()!=10){
            mContact.setError("Contact no. must be equal to 10 digits");
            return;
        }
        if(TextUtils.isEmpty(cemail)){
            mEmail.setError("Email is Required.");
            return;
        }
        if(TextUtils.isEmpty(cname)){
            mFullName.setError("Name is required");
            return;
        }
        reference = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId);
        reference.child("contact").setValue(ccontact);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fname = snapshot.child("name").getValue(String.class);
                    email = snapshot.child("email").getValue(String.class);

                    phone = snapshot.child("phone").getValue(String.class);
                    if(!fname.equals(cname)){
                        reference.child("name").setValue(cname);
                    }
                    if(!contact.equals(ccontact)){

                    }
                    if(!email.equals(cemail)){

                      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                      DatabaseReference de  = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("password");
                      de.addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                              String password = snapshot.getValue(String.class);
                              AuthCredential credential = EmailAuthProvider
                                      .getCredential(email, password);
                              user.reauthenticate(credential)
                                      .addOnCompleteListener(task -> {
                                          FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
                                          user1.updateEmail(cemail)
                                                  .addOnCompleteListener(task1 -> {
                                                      if (task1.isSuccessful()) {
                                                          //   Log.d(TAG, "User email address updated.");
                                                          reference.child("email").setValue(cemail);
                                                      }
                                                  });
                                          //----------------------------------------------------------\\
                                      });
                          }

                          @Override
                          public void onCancelled(@NonNull @NotNull DatabaseError error) {

                          }
                      });
                     // Toast.makeText(getApplicationContext(),password,Toast.LENGTH_SHORT).show();

                    }
                    if(!phone.equals(cphone)){
                        reference.child("phone").setValue(cphone);
                    }
                    mFullName.setText(cname);
                    mEmail.setText(cemail);
                    mContact.setText(ccontact);
                    mPhone.setText(cphone);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }

        });
        Toast.makeText(getApplicationContext(),"Update Success",Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void backMain(View view){
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
    public void changePassword(View view){
        FirebaseUser user = fAuth.getInstance().getCurrentUser();

        final EditText resetPassword = new EditText(view.getContext());
        final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
        passwordResetDialog.setTitle("Reset Password");
        passwordResetDialog.setMessage("Enter the new password > 6 character long.");
        passwordResetDialog.setView(resetPassword);
        passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
            String newPassword = resetPassword.getText().toString();
            user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getApplicationContext(),"Password Reset Success",Toast.LENGTH_SHORT).show();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User Info").child(userId).child("password");
                    ref.setValue(newPassword);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(getApplicationContext(),"Password Reset Failed",Toast.LENGTH_SHORT).show();
                }
            });
        });
        passwordResetDialog.setNegativeButton("No", (dialog, which) -> {

        });
        passwordResetDialog.show();
    }
}