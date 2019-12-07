package com.example.owais.csunplugged;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class UploadActivity extends AppCompatActivity {
    Button upload,play;
    Uri videoUri;




    FirebaseStorage storage;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        upload=findViewById(R.id.upload);
        play=findViewById(R.id.play);
        storage=FirebaseStorage.getInstance();

        database=FirebaseDatabase.getInstance();

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               if (ContextCompat.checkSelfPermission(UploadActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED ){
                selectVideo();
               }
               else {
                   ActivityCompat.requestPermissions(UploadActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
               }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UploadActivity.this,MainActivity.class);
                startActivity(intent
                );
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==9 && grantResults[0]==PackageManager.PERMISSION_GRANTED ){
            selectVideo();
        }
        else {
            Toast.makeText(UploadActivity.this,"Request not granted! Please grant required permission to proceed.",Toast.LENGTH_SHORT).show();
        }
    }

    private void selectVideo() {
        Intent intent=new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),186);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("asdfg",Integer.toString(requestCode));
        Log.d("asdfg",Integer.toString(resultCode));
        Log.d("asdfg",data.toString());
        if (requestCode==186 && resultCode==RESULT_OK  ){
            if(data!=null  ) {
                //Bundle extras=data.getExtras();

                videoUri = data.getData();
                String videopath = videoUri.getPath();
                File file = new File(videopath);
                Log.e("path",file.getAbsolutePath());

                //Log.d("mnbvc",videoUri.toString());
                uploadFile(videoUri);
            }
        }
        else {
            Toast.makeText(UploadActivity.this,"Please select a file",Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(Uri videoUri) {

        StorageReference storageReference=storage.getReference();
        final String filename=System.currentTimeMillis()+"";
        //following line stores selected video in firebase storage under "videos" folder with name of "filename"
        storageReference.child("videos").child(filename).putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();        //it get the url of the uploaded file
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String url=uri.toString();
                        Log.d("uploadedUrl",url);
                        DatabaseReference reference=database.getReference();
                        reference.child(filename).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(UploadActivity.this,"Video uploaded successfully",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(UploadActivity.this,"Video uploaded successfully",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int current=(int)(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());


            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                Toast.makeText(UploadActivity.this,"Video uploaded successfully",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
