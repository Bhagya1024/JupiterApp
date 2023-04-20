package com.example.jupiterapp;

import android.speech.tts.TextToSpeech;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Words extends AppCompatActivity {
    private TextToSpeech textToSpeech;

    private static final String TAG = "WordsActivity";
    private TextView wordTextView;
    private TextView meaningTextView;
    private TextView pronunciationTextView,wordNoTxtView;
    private LinearLayout gotItButton;
    private LinearLayout knewItButton;
    private List<DataSnapshot> wordList = new ArrayList<>();
    private int currentWordIndex = 0;

    private ProgressBar wordprogressBar;
    double progress=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        // Get references to the TextViews and Buttons in the layout
        wordTextView = findViewById(R.id.wordtxt);
        meaningTextView = findViewById(R.id.wordmeaningtxt);
        pronunciationTextView = findViewById(R.id.wordpronuntxt);
        gotItButton = findViewById(R.id.gotitbtn);
        wordNoTxtView = findViewById(R.id.wordno);
        wordprogressBar=findViewById(R.id.wordprogressBar);


        // Get a reference to the words collection in the database
        DatabaseReference wordsRef = FirebaseDatabase.getInstance().getReference("words");

        // Generate a random value to use for ordering the words
        int randomValue = new Random().nextInt(5) + 1;

        wordprogressBar.setProgress((int) progress);
        // Attach a listener to retrieve 3 random words with their attributes
        wordsRef.orderByChild("random_order").startAt(randomValue).limitToFirst(3)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot wordSnapshot : dataSnapshot.getChildren()) {
                            wordList.add(wordSnapshot);
                        }
                        showCurrentWord();
                        wordNoTxtView.setText(String.valueOf(currentWordIndex+1));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "onCancelled", databaseError.toException());
                    }
                });

        // Attach click listeners to the buttons to navigate through the word list
        gotItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentWordIndex < wordList.size() - 1) {
                    currentWordIndex++;
                    showCurrentWord();
                    wordNoTxtView.setText(String.valueOf(currentWordIndex+1));

                    progress += 33.33;
                    wordprogressBar.setProgress((int) progress);

                    if(currentWordIndex==2)
                    {
                        gotItButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                        // Redirect to the home activity
                                        Intent intent = new Intent(Words.this, popup.class);
                                        startActivity(intent);

                            }
                        });

                    }

                }
            }
        });
// Initialize TextToSpeech engine
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set language of TextToSpeech engine to the default locale
                    int result = textToSpeech.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "Language not supported");
                    } else {
                        // Enable soundbtn when TextToSpeech engine is initialized
                        ImageButton soundBtn = findViewById(R.id.soundbtn);
                        soundBtn.setEnabled(true);
                        soundBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                speakWord();
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "TextToSpeech initialization failed");
                }
            }
        });

// Disable soundbtn until TextToSpeech engine is initialized
//        ImageButton soundBtn = findViewById(R.id.soundbtn);
//        soundBtn.setEnabled(false);

    }
    private void speakWord() {
        if (currentWordIndex < wordList.size()) {
            DataSnapshot currentWordSnapshot = wordList.get(currentWordIndex);
            String word = currentWordSnapshot.child("word").getValue(String.class);
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void showCurrentWord() {
        if (currentWordIndex < wordList.size()) {
            DataSnapshot currentWordSnapshot = wordList.get(currentWordIndex);
            String word = currentWordSnapshot.child("word").getValue(String.class);
            String meaning = currentWordSnapshot.child("meaning").getValue(String.class);
            String pronunciation = currentWordSnapshot.child("pronunciation").getValue(String.class);

            wordTextView.setText(word);
            meaningTextView.setText(meaning);
            pronunciationTextView.setText(pronunciation);
        }
    }


}
