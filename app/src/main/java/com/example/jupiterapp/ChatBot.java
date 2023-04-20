package com.example.jupiterapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.os.Build;
import java.util.Locale;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

    public class ChatBot extends AppCompatActivity {


        private EditText msgbox;
        private ImageView sendbtn;
        private DatabaseReference chatRef;
        private ScrollView chatscroll;
        private LinearLayout msgList;
        private TextToSpeech tts;
        // Instantiate the RequestQueue.
        RequestQueue queue;
        String url = "https://asia-south1-jupitergcp.cloudfunctions.net/jypnew";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat_bot);

            // Initialize the UI elements
            msgbox = findViewById(R.id.msgbox);
            sendbtn = findViewById(R.id.sendbtn);

            // Set up the chatscroll ScrollView
            chatscroll = findViewById(R.id.chatscroll);

            // Scroll to the bottom of the chatscroll ScrollView
            chatscroll.post(() -> chatscroll.fullScroll(View.FOCUS_DOWN));

            // Initialize the Firebase Realtime Database reference
            chatRef = FirebaseDatabase.getInstance().getReference().child("chat");

            // Instantiate the RequestQueue
            queue = Volley.newRequestQueue(this);

            // Set the click listener for the send button
            sendbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the current user ID
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Get the current date and time
                    Date currentDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a d MMMM", Locale.getDefault());
                    String datetime = dateFormat.format(currentDate);

                    // Get the user's message from the EditText
                    String message = msgbox.getText().toString();

                    // Save the message to the database
                    DatabaseReference newMessageRef = chatRef.push();
                    newMessageRef.child("userId").setValue(userId);
                    newMessageRef.child("datetime").setValue(datetime);
                    newMessageRef.child("message").setValue(message);
                    newMessageRef.child("type").setValue("user");

                    // Save the user ID to the database
                    DatabaseReference userIdRef = chatRef.child("users").child(userId);
                    userIdRef.setValue(true);

                    // Clear the EditText
                    msgbox.setText("");


                    // Send the message to the AI model
                    JSONObject requestObject = new JSONObject();
                    try {
                        requestObject.put("msg", message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        // Get the "msg" value from the response JSON object
                                        String aiResponse = response.getString("msg");

                                        // Print the AI response to the logcat
                                        Log.d("ChatBot", aiResponse);

                                        // Save the AI response to the database
                                        DatabaseReference aiMessageRef = chatRef.push();
                                        aiMessageRef.child("userId").setValue(userId);
                                        aiMessageRef.child("datetime").setValue(datetime);
                                        aiMessageRef.child("message").setValue(aiResponse);
                                        aiMessageRef.child("type").setValue("bot");

                                    } catch (JSONException e) {
                                        Log.e("ChatBot", "Error parsing response: " + e.getMessage());
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle Volley errors here
                            Log.e("ChatBot", "Error: " + error.getMessage());
                        }
                    });

                    // Add the request to the RequestQueue.
                    queue.add(jsonObjectRequest);
                }
            });



            chatRef.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            LinearLayout msglist = findViewById(R.id.msglist);
                            msglist.removeAllViews();

                            for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                                String type = chatSnapshot.child("type").getValue(String.class);

                                if (type != null && type.equals("bot")) {
                                    String message = chatSnapshot.child("message").getValue(String.class);
                                    String datetime = chatSnapshot.child("datetime").getValue(String.class);

                                    // Inflate botmsg.xml to create a new botmsg LinearLayout
                                    LinearLayout botmsg = (LinearLayout) getLayoutInflater().inflate(R.layout.botmsg, null);

                                    // Set the message and datetime values in the botmsg LinearLayout's TextViews
                                    TextView botmsgtxt = botmsg.findViewById(R.id.botmsgtxt);
                                    botmsgtxt.setText(message);

                                    TextView botmsgtime = botmsg.findViewById(R.id.botmsgtime);
                                    botmsgtime.setText(datetime);

                                    // Get the sound ImageView and set its click listener to speak the message
                                    ImageView botsound = botmsg.findViewById(R.id.botsound);
                                    botsound.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                                        }
                                    });

                                    // Add the botmsg LinearLayout to the msglist LinearLayout
                                    msglist.addView(botmsg);
                                } else if (type != null && type.equals("user")) {
                                    String message = chatSnapshot.child("message").getValue(String.class);
                                    String datetime = chatSnapshot.child("datetime").getValue(String.class);

                                    // Inflate usermsg.xml to create a new usermsg LinearLayout
                                    LinearLayout usermsg = (LinearLayout) getLayoutInflater().inflate(R.layout.usermsg, null);

                                    // Set the message and datetime values in the usermsg LinearLayout's TextViews
                                    TextView usermsgtxt = usermsg.findViewById(R.id.usermsgtxt);
                                    usermsgtxt.setText(message);

                                    TextView usermsgtime = usermsg.findViewById(R.id.usermsgtime);
                                    usermsgtime.setText(datetime);

                                    // Add the usermsg LinearLayout to the msglist LinearLayout
                                    msglist.addView(usermsg);
                                }
                            }
                            // Scroll to the bottom of the chatscroll ScrollView
                            chatscroll.post(() -> chatscroll.fullScroll(View.FOCUS_DOWN));

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("ChatBot", "Database error: " + error.getMessage());
                        }
                    });

            tts = new TextToSpeech(ChatBot.this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        // Set the language to US English
                        int result = tts.setLanguage(Locale.US);
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("ChatBot", "Language not supported");
                        }
                    } else {
                        Log.e("ChatBot", "Initialization failed");
                    }
                }
            });


        }
    }
