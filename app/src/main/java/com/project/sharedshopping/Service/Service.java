package com.project.sharedshopping.Service;

import android.app.Notification;
import android.app.PendingIntent;

import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.sharedshopping.Activities.MyProfileActivity;
import com.project.sharedshopping.R;

import java.util.HashMap;

public class Service extends android.app.Service {

    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    Uri mImageUri;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(this, MyProfileActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mImageUri=(Uri) intent.getParcelableExtra("picture");
        StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference mStorageReference= storageReference.child(user.getUid()+".jpg");
        mStorageReference.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            HashMap<String,Object> result= new HashMap<>();
                            result.put("photo",uri.toString());
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri).build();
                            user.updateProfile(profileUpdates);
                            Toast.makeText(getApplicationContext(), "The image was updated successfully", Toast.LENGTH_SHORT).show();
                            stopSelf();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("upload image","Could not upload an image: "+e.getMessage());
                            stopSelf();
                        }
                    });

                }
            }
        });


        Notification notification = new NotificationCompat.Builder(getApplicationContext(), AppNotification.CHANNEL_ID)
                .setContentTitle("Uploading an image")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
