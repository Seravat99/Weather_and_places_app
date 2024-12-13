package com.example.trabalhofinal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhofinal.databinding.EditprofileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    @NonNull
    EditprofileBinding binding;

    ProgressDialog progressDialog;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    double latitude;
    double longitude;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EditprofileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);

        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);


        binding.save.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                String email = binding.newName.getText().toString().trim();
                String password = binding.newPassword.getText().toString().trim();
                progressDialog.show();


                user.updatePassword(password);
                user.updateEmail(email);

                Toast.makeText(EditProfileActivity.this, "Password and Email changed succesfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditProfileActivity.this, MapsActivity.class));
            }


        });
        binding.ActiveWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent places = new Intent(EditProfileActivity.this, WeatherActivity.class);
                places.putExtra("latitude", latitude);
                places.putExtra("longitude", longitude);
                startActivity(places);
            }
        });
        binding.signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                Intent mainActivity = new Intent (EditProfileActivity.this, LoginActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainActivity);
                finish();
                Toast.makeText(getApplicationContext(), "Log Out", Toast.LENGTH_SHORT).show();
            }


        });

    }
}
