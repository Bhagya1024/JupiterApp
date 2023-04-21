package com.example.jupiterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Button signInBtn;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if the user is already authenticated
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already authenticated, redirect to Home activity
            startActivity(new Intent(MainActivity.this, Home.class));
            finish();
        } else {
            // User is not authenticated, display the current activity
            setContentView(R.layout.activity_main);

        }

        signInBtn = findViewById(R.id.signinbtn1);

        // Setting onClickListener for Sign In button
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignIn.class);
                startActivity(intent);
            }
        });

        TextView signUpBtn = findViewById(R.id.signupbtn1);

        // Setting onClickListener for Sign Up text view
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });


    }
}
