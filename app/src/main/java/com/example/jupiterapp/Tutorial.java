package com.example.jupiterapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Tutorial extends AppCompatActivity {

    private TextView lessonName, lessonNo, lessonDescription;
    private DatabaseReference databaseReference;
    private String lessonNoValue;
    private VideoView videoView;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        lessonName = findViewById(R.id.lessonname);
        lessonNo = findViewById(R.id.lessonno);
        lessonDescription = findViewById(R.id.description);
        webView = findViewById(R.id.tutorialvideo);
//        lessonNoValue = getIntent().getStringExtra("lessonNo");



        // Retrieve the relevant record from the tutorials collection
        databaseReference = FirebaseDatabase.getInstance().getReference("tutorials");
        databaseReference.orderByChild("lessonno").equalTo("1").addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // Get the first matching DatabaseReference
                    DataSnapshot firstMatch = dataSnapshot.getChildren().iterator().next();
                    DatabaseReference tutorialReference = firstMatch.getRef();

                    String topic = firstMatch.child("topic").getValue().toString();
                    String description = firstMatch.child("description").getValue().toString();
                    String video = firstMatch.child("video").getValue().toString();
                    String videoId = video.substring(video.indexOf("=")+1);
                    // Set the text of the lessonName, lessonNo and lessonDescription TextViews
                    lessonName.setText(topic);
                    lessonNo.setText("1");
                    lessonDescription.setText(description);


                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            // Load the YouTube video using the iframe API
                            view.loadUrl("javascript:player = new YT.Player('player', {videoId: '" + videoId + "', playerVars: { 'autoplay': 1 }});");
                        }
                    });
                    webView.loadUrl("https://www.youtube.com/embed/"+videoId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Tutorial", "Error retrieving tutorial from database", databaseError.toException());
            }
        });
    }


}
