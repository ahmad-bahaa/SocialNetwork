package com.ninjageeksco.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private static final int PICTURE_PICK = 1 ;
    //views
    private Toolbar mToolbar;
    private ImageButton imageButton;
    private EditText mEditText;
    private Button mButton;
    private ProgressDialog loadingBar;
    //res
    private Uri imageUri;
    //firebase
    private DatabaseReference mDatabaseReference,mPostRef;
    private StorageReference mStorageReference;
    private FirebaseAuth mfFirebaseAuth;
    //strings
    private String post,sCurrentTime,sCurrentDate,postRandomName, downloadUrl, currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        //firebase
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mPostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mfFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mfFirebaseAuth.getCurrentUser().getUid();
        //Action Bar
        mToolbar = findViewById(R.id.post_back_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");
        //Views
        imageButton = findViewById(R.id.post_activity_add_pic);
        mEditText = findViewById(R.id.post_activity_edit_text);
        mButton = findViewById(R.id.post_activity_button);
        loadingBar = new ProgressDialog(this);
        // On Click Listener
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGalery();
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
         post = mEditText.getText().toString();
        if (TextUtils.isEmpty(post)) {
            Toast.makeText(PostActivity.this, "Please Write Something.!", Toast.LENGTH_SHORT).show();
        }else if (imageUri == null){
            Toast.makeText(PostActivity.this, "Please Select Post Image.!", Toast.LENGTH_SHORT).show();
        }else {
            StoringImageToStorage();
            loadingBar.setTitle("Preparing Your Image");
            loadingBar.setMessage("Please Wait!");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
        }
    }

    private void StoringImageToStorage() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMMM-dd-yyyy");
        sCurrentDate = currentDate.format(calendar.getTime());
        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        sCurrentTime = currentTime.format(calendar1.getTime());
        postRandomName = sCurrentDate + sCurrentTime;

        StorageReference imagePath = mStorageReference.child("Post Images")
                .child(imageUri.getLastPathSegment()+postRandomName+".jpg");
        imagePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        downloadUrl = task.getResult().getDownloadUrl().toString();
                        Toast.makeText(PostActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        SavingPostInformationToDatabase();
                    }else{
                        String message = task.getException().getMessage().toString();
                        Toast.makeText(PostActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }

    private void SavingPostInformationToDatabase() {
        mDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profielimage").getValue().toString();
                    HashMap postMap = new HashMap();
                    postMap.put("uid",currentUserId);
                    postMap.put("date",sCurrentDate);
                    postMap.put("time",sCurrentTime);
                    postMap.put("describtion",post);
                    postMap.put("postimage",downloadUrl);
                    postMap.put("prfileimage",userProfileImage);
                    postMap.put("fullname",userFullName);
                    mPostRef.child(currentUserId+postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                SendUserToMainActivity();
                                loadingBar.dismiss();
                                Toast.makeText(PostActivity.this, "Post Updated Successfully", Toast.LENGTH_SHORT).show();
                            }else {
                                String message = task.getException().getMessage().toString();
                                Toast.makeText(PostActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void OpenGalery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,PICTURE_PICK);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_PICK && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            imageButton.setImageURI(imageUri);

        }
    }


    private void SendUserToMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
