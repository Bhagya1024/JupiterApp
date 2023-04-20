package com.example.jupiterapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Home extends AppCompatActivity {


    private TextView textusername, lvlnumber,tutorialNameTextView,lessonNoTextView,tutorialDescriptionTextView;
    private ImageView profileimg;

    private LinearLayout tutorialBox;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        textusername = findViewById(R.id.textusername);
        lvlnumber = findViewById(R.id.lvlnumber);
        profileimg = findViewById(R.id.profileimg);
        tutorialNameTextView = findViewById(R.id.tutorialname);
        lessonNoTextView = findViewById(R.id.lessonno);
        tutorialDescriptionTextView = findViewById(R.id.tutorialdescription);
        tutorialBox = findViewById(R.id.tutorialbox);



        // retrieve user information from database
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();
        mDatabase.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                String level = dataSnapshot.child("level").getValue().toString();
                String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                textusername.setText(username);
                lvlnumber.setText(level);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new RoundedCorners(50)); // set rounded corners to 16dp


                // load profile image into image view using Glide
                Glide.with(Home.this)
                        .load(profileImageUrl)
                        .apply(requestOptions.circleCropTransform())
                        .into(profileimg);

                profileimg = findViewById(R.id.profileimg);
                profileimg.setScaleType(ImageView.ScaleType.FIT_CENTER);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


// Check if the username already exists
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tutorials");
        databaseReference.orderByChild("lessonno").equalTo("1").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the first matching DatabaseReference
                    DataSnapshot firstMatch = dataSnapshot.getChildren().iterator().next();
                    DatabaseReference tutorialReference = firstMatch.getRef();

                    String topic = firstMatch.child("topic").getValue().toString();
                    String description = firstMatch.child("description").getValue().toString();



                    // Set the text of the TextViews

                    tutorialNameTextView.setText(topic);


                    lessonNoTextView.setText("1");


                    tutorialDescriptionTextView.setText(description);


                    tutorialBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Create an intent to start Tutorial activity
                            Intent tutorialIntent = new Intent(Home.this, Tutorial.class);
                            // Pass the lessonNo as an extra
                            tutorialIntent.putExtra("lessonNo", 1);
                            startActivity(tutorialIntent);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });




        LinearLayout wordsBtn = findViewById(R.id.wordsbtn);
        wordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Words.class);
                startActivity(intent);
            }
        });


        ImageView settingsBtn = findViewById(R.id.settingsbtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Settings.class);
                startActivity(intent);
            }
        });



        ImageView profileBtn = findViewById(R.id.profilebtn);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, profile.class);
                startActivity(intent);
            }
        });


        ImageView chatBtn = findViewById(R.id.chatbtn);
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, ChatBot.class);
                startActivity(intent);
            }
        });


        LinearLayout livechatbtn = findViewById(R.id.livechatbtn);
        livechatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, SelectRoom.class);
                startActivity(intent);
            }
        });
    }
}