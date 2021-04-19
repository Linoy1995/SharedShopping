package com.project.sharedshopping.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.project.sharedshopping.R;
import com.project.sharedshopping.Service.Service;

import java.util.Objects;

public class MyProfileActivity extends AppCompatActivity {

    private ImageView back, profile_image;
    private TextView profileNameTop, profileEmailTop;
    private TextInputEditText fullName, profileEmailBottom, password;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private Uri uri;

    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE=400;

    private String storagePermissions[];
    private Boolean updateFlag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        // Views Initialization
        back= findViewById(R.id.backFromProfile);
        profile_image= findViewById(R.id.profile_image);
        profileNameTop= findViewById(R.id.profile_name);
        profileEmailTop= findViewById(R.id.profile_email);
        fullName= findViewById(R.id.full_name_profile);
        profileEmailBottom= findViewById(R.id.email_profile);
        password= findViewById(R.id.password_profile);
        progressBar= findViewById(R.id.updateUserPB);

        // get the current user
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();

        // back arrow implementation
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // load user photo if exists
        showPhoto();

        // Display user changes
        profileNameTop.setText(user.getDisplayName());
        profileEmailTop.setText(user.getEmail());
        fullName.setText(user.getDisplayName());
        profileEmailBottom.setText(user.getEmail());

        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkStoragePermission())
                    requestStoragePermission();
                else
                    pickFromGallery();
            }
        });

    }

    private void showPhoto(){
        uri = user.getPhotoUrl();
        if(uri==null)// If the user has logged in from email, set a default image
            profile_image.setImageResource(R.mipmap.ic_launcher_round);
        else
            Glide.with(this).load(uri).into(profile_image);
    }

    public void update_user(View view){

        progressBar.setVisibility(View.VISIBLE);

        String displayName = Objects.requireNonNull(fullName.getText()).toString().trim();
        String displayEmail = Objects.requireNonNull(profileEmailBottom.getText()).toString().trim();
        String displayPass = Objects.requireNonNull(password.getText()).toString().trim();

        // Validations before update
        if (TextUtils.isEmpty(displayName) && TextUtils.isEmpty(displayEmail) && TextUtils.isEmpty(displayPass)) {
            fullName.setError("Please enter username");
            profileEmailBottom.setError("Please enter your email");
            password.setError("Please enter your password");

            fullName.setFocusable(true);
            profileEmailBottom.setFocusable(true);
            password.setFocusable(true);

            progressBar.setVisibility(View.GONE);

            return;
        }

        if (TextUtils.isEmpty(displayName)) {
            fullName.setError("Please enter username");
            fullName.setFocusable(true);
            progressBar.setVisibility(View.GONE);

            return;
        }

        if (TextUtils.isEmpty(displayEmail)) {
            profileEmailBottom.setError("Please enter your email");
            profileEmailBottom.setFocusable(true);
            progressBar.setVisibility(View.GONE);

            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(displayEmail).matches()) {
            profileEmailBottom.setError("Invalid Email");
            profileEmailBottom.setFocusable(true);
            progressBar.setVisibility(View.GONE);

            return;
        }

        if (TextUtils.isEmpty(displayPass)) {
            password.setError("Please enter your password");
            password.setFocusable(true);
            progressBar.setVisibility(View.GONE);

            return;
        }

        if (displayPass.length() < 6) {
            password.setError("Your password must contain at least 6 characters");
            password.setFocusable(true);
            progressBar.setVisibility(View.GONE);

            return;
        }

      /*  if(!displayPass.matches("^[a-zA-Z0-9]+$"))
        {
            password_et.setError("Your password must contain at least one letter and one number");
            password_et.setFocusable(true);
            return;
        }*/

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName).build();

        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Log.d("Full name status", "Full name was updated.");
                else {
                    updateFlag = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyProfileActivity.this, "Full name was not updated: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        user.updateEmail(displayEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Log.d("Email status", "Email was updated.");
                else {
                    updateFlag = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyProfileActivity.this, "Email was not updated: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });


        user.updatePassword(displayPass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Log.d("Password status", "Password was updated.");
                else {
                    updateFlag = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyProfileActivity.this, "Password was not updated: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


            /*Delay of 6 seconds before successful message
            because there is a delay of a few seconds in Firebase if there is an error updating one of the fields*/
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (updateFlag) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyProfileActivity.this, "User was updated successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }, 6000);

        }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission()
    {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //check if camera and storage permissions allowed or not
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                //If the user allows permission -> Enable him to pick an image from device
                pickFromGallery();
            else
                Toast.makeText(this, "Please enable storage permissions", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void pickFromGallery() {
        Intent galleryIntent= new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK)
        {
            if(requestCode==IMAGE_PICK_GALLERY_REQUEST_CODE)
            {
                //Image is picked from gallery, get uri of image
                uri= data.getData();
                profile_image.setImageURI(uri);
                Intent serviceIntent= new Intent(MyProfileActivity.this, Service.class);
                serviceIntent.putExtra("inputExtra", "Please wait...");
                serviceIntent.putExtra("picture", uri);
                startService(serviceIntent);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

}
