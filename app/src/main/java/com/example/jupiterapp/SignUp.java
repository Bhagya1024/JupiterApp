package com.example.jupiterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class SignUp extends AppCompatActivity {
    private EditText mUsername, mEmail, mPassword;
    private Button mSignUpButton;
    private FirebaseAuth mAuth;

    public class User {
        public String username;
        public String email;
        public String password;
        public String profileImageUrl;
        public int level;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.level=1;
            this.profileImageUrl="https://firebasestorage.googleapis.com/v0/b/jupiter-ba5b1.appspot.com/o/default.jpg?alt=media&token=8a45a462-2239-4d48-9cc3-a60f4f9107e1";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mUsername = findViewById(R.id.editTexUsername2);
        mEmail = findViewById(R.id.editTextemail);
        mPassword = findViewById(R.id.editTextPassword2);
        mSignUpButton = findViewById(R.id.signupbtn);

        mAuth = FirebaseAuth.getInstance();

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (username.isEmpty()) {
                    mUsername.setError("Username is required");
                    mUsername.requestFocus();
                    return;
                }

                if (email.isEmpty()) {
                    mEmail.setError("Email is required");
                    mEmail.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    mPassword.setError("Password is required");
                    mPassword.requestFocus();
                    return;
                }

                // Check if the username already exists
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Username already exists, show error message
                            mUsername.setError("Username already exists");
                            mUsername.requestFocus();
                        } else {
                            // Username is available, create the user
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Get the user's ID
                                                String userID = mAuth.getCurrentUser().getUid();

                                                // Save the user's information to the database
                                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userID);
                                                User user = new User(username, email, password);
                                                databaseReference.setValue(user);

                                                // Sign up success, update UI with the signed-in user's information
                                                Toast.makeText(SignUp.this, "Registration successful", Toast.LENGTH_LONG).show();

                                                // Redirect to SignIn activity
                                                Intent intent = new Intent(SignUp.this, SignIn.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // If sign up fails, display a message to the user.
                                                Toast.makeText(SignUp.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle error
                    }
                });
            }
        });

    }
}