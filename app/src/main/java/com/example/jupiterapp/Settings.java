package com.example.jupiterapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class Settings extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private Uri mImageUri;

    private ImageView mProfileImg;
    private ImageView mChangeDpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = mStorage.getReference("profileImages");

        mProfileImg = findViewById(R.id.profileimg);
        mChangeDpBtn = findViewById(R.id.changedp);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new RoundedCorners(50));

        // retrieve user information from database and display profile image
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();
        mDatabase.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                if (profileImageUrl != null) {
                    Glide.with(Settings.this)
                            .load(profileImageUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .into(mProfileImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Settings", "Error retrieving user information: " + databaseError.getMessage());
            }
        });

        // open gallery to select image when change dp button is clicked
        mChangeDpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    // handle result of image selection from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            uploadImage();
        }
    }

    // upload selected image to Firebase storage
    private void uploadImage() {
        if (mImageUri != null) {
            uploadImageToFirebaseStorage(mImageUri);
        }
    }

    // upload selected image to Firebase storage
    private void uploadImageToFirebaseStorage(Uri imageUri) {
        // show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image...");
        progressDialog.show();

        // get reference to Firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // create filename for the image
        String filename = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child("images/" + filename);

        // upload image to Firebase storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // image uploaded successfully, get download url
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // download url retrieved, update user's profileImageUrl
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                String uid = currentUser.getUid();
                                mDatabase.child("users").child(uid).child("profileImageUrl").setValue(uri.toString())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // update successful, dismiss progress dialog
                                                progressDialog.dismiss();
                                                Toast.makeText(Settings.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // update failed, dismiss progress dialog and show error message
                                                progressDialog.dismiss();
                                                Toast.makeText(Settings.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // image upload failed, dismiss progress dialog and show error message
                        progressDialog.dismiss();
                        Toast.makeText(Settings.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}