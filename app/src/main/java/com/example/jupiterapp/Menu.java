package com.example.jupiterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;

public class Menu extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        ImageView backbtn = findViewById(R.id.backbtn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Menu.this, Home.class);
                startActivity(intent);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        // Get the LinearLayout view for logout
        LinearLayout logoutLayout = findViewById(R.id.logoutbtn);

        // Set click listener for logout
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign the user out
                firebaseAuth.signOut();
                // Redirect the user to MainActivity
                startActivity(new Intent(Menu.this, MainActivity.class));
                finish();
            }
        });
    }
}