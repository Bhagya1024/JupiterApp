package com.example.jupiterapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SelectRoom extends AppCompatActivity {

    private TextView textusername, lvlnumber;
    private ImageView profileimg;

    private LinearLayout roomList;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_room);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        textusername = findViewById(R.id.textusername);
        lvlnumber = findViewById(R.id.lvlnumber);
        profileimg = findViewById(R.id.profileimg);
        roomList = findViewById(R.id.room_list);


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
                Glide.with(SelectRoom.this)
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


        FirebaseDatabase.getInstance().getReference("rooms").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @NonNull String s) {
                // get room information
                final String roomId = dataSnapshot.child("roomId").getValue().toString();

                // retrieve number of connected users in the room from usersInRoom collection
                FirebaseDatabase.getInstance().getReference("usersInRooms").orderByChild("roomId").equalTo(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int connectedUsers = 0;
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.child("connected").getValue().toString().equals("1")) {
                                connectedUsers++;
                            }
                        }

                        // inflate room box layout
                        LinearLayout roomBoxView = (LinearLayout) LayoutInflater.from(SelectRoom.this).inflate(R.layout.roombox, null);

                        // set room box text views
                        TextView roomIdTextView = roomBoxView.findViewById(R.id.roomid);
                        TextView connectedNoTextView = roomBoxView.findViewById(R.id.connectedno);
                        roomIdTextView.setText(roomId);
                        connectedNoTextView.setText(String.valueOf(connectedUsers));

                        // add click listener to room box view
                        roomBoxView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(SelectRoom.this, LiveChat.class);
                                intent.putExtra("roomId", roomId);
                                startActivity(intent);
                            }
                        });

                        // add room box view to room list linear layout
                        roomList.addView(roomBoxView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // handle cancel event
                    }


                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @NonNull String s) {
                // handle child changed event
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @NonNull String s) {
                // handle child moved event
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // handle child removed event
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle cancel event
            }
        });

    }
}



