package com.example.jupiterapp;

import static android.content.ContentValues.TAG;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveChat extends AppCompatActivity {
    private static final String TAG = "LiveChat";

    private TextView roomidtxt;
    private String username;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUsersInRoomsRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_chat);

        // retrieve roomId from intent
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("roomId");

        // find and set roomidtxt TextView
        roomidtxt = findViewById(R.id.roomidtxt);
        roomidtxt.setText(roomId);


        // retrieve user information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();



        mUsersInRoomsRef = mDatabase.getReference("usersInRooms");

        // set up joinbtn and leavebtn
        Button joinbtn = findViewById(R.id.joinbtn);
        Button leavebtn = findViewById(R.id.leavebtn);
        leavebtn.setVisibility(View.GONE); // hide leavebtn initially


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersInRoomsRef = database.getReference("usersInRooms");

        usersInRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                LinearLayout userlist = findViewById(R.id.userlist);
                userlist.removeAllViews(); // clear previous userboxes

                LinearLayout row = null;
                int count = 0;


                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String roomId = userSnapshot.child("roomId").getValue(String.class);
                    int connected = userSnapshot.child("connected").getValue(Integer.class);

                    if (roomId.equals(roomId) && connected == 1) {
                        String username = userSnapshot.child("username").getValue(String.class);

                        // create a new userbox
                        View userbox = LayoutInflater.from(LiveChat.this).inflate(R.layout.usersroom, null);

                        TextView usernameView = userbox.findViewById(R.id.username);
                        TextView lvlnumber = userbox.findViewById(R.id.lvlnumber);
                        ImageView userimage = userbox.findViewById(R.id.userimage);

                        // set the user's username and image
                        usernameView.setText(username);

                        DatabaseReference usersRef = database.getReference("users");

                        Query query = usersRef.orderByChild("username").equalTo(username);

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                                    String profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String.class);
                                    int level = userSnapshot.child("level").getValue(Integer.class);

                                    // Set level value in lvlnumber TextView
                                    lvlnumber.setText(String.valueOf(level));

//                                     Load image from Firebase Storage
                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                    StorageReference imageRef = storageRef.child(profileImageUrl);
                                    Glide.with(LiveChat.this).load(imageRef).into(userimage);

                                    RequestOptions requestOptions = new RequestOptions();
                                    requestOptions = requestOptions.transforms(new RoundedCorners(50));

                                    // load profile image into image view using Glide
                                    Glide.with(LiveChat.this)
                                            .load(profileImageUrl)
                                            .apply(requestOptions.circleCropTransform())
                                            .into(userimage);

                                    userimage.setScaleType(ImageView.ScaleType.FIT_CENTER);


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w(TAG, "onCancelled", databaseError.toException());
                            }


                        });


                        // create a new row after every 2 userboxes
                        if (count % 2 == 0) {
                            row = new LinearLayout(LiveChat.this);
                            row.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            userlist.addView(row);
                        }

                        // add the userbox to the current row
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.weight = 1;
                        userbox.setLayoutParams(params);
                        row.addView(userbox);

                        count++;
                    }
                }

                // create a new row for any remaining user if the last row contains an odd number of userboxes
                if (count % 2 != 0) {
                    row = new LinearLayout(LiveChat.this);
                    row.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    userlist.addView(row);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });



        // add onClickListener to leavebtn
        leavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String uid = currentUser.getUid();

                DatabaseReference userInRoomRef = mDatabase.getReference("usersInRooms").child(uid);
                userInRoomRef.child("connected").setValue(0);

                Intent intent = new Intent(LiveChat.this, Home.class);
                startActivity(intent);
            }
        });



        // add onClickListener to joinbtn
        joinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserToRoom(roomId);
            }

        });
    }


    private void addUserToRoom(String roomId) {

        // retrieve user information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        DatabaseReference userRef = mUsersInRoomsRef.child(uid);
        // set up joinbtn and leavebtn
        Button joinbtn = findViewById(R.id.joinbtn);
        Button leavebtn = findViewById(R.id.leavebtn);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("connected", 1);
        userMap.put("roomId", roomId);

        userRef.setValue(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        joinbtn.setVisibility(View.GONE);
                        leavebtn.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "addUserToRoom:onFailure", e);
                        Toast.makeText(LiveChat.this, "Failed to join room", Toast.LENGTH_SHORT).show();
                    }
                });



    }

}






