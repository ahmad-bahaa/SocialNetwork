package com.ninjageeksco.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText username,fullName;
    private Button saveInformationButton;
    private CircleImageView mcircleImageView;
    private Spinner spinner;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    String currentUserId;
    private ProgressDialog loadingBar;
    final static int PROFILE_PICTURE_PICK = 1;
    private String downloadUrl, country;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

       username = findViewById(R.id.editText_username);
       fullName = findViewById(R.id.editText_fullName);
      // country = findViewById(R.id.editText_country);
       saveInformationButton = findViewById(R.id.button_save);
       mcircleImageView = findViewById(R.id.imageview_user);
        spinner = findViewById(R.id.spinner);

        loadingBar = new ProgressDialog(this);

        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (country.trim().length() > 0 && !countries.contains(country)) {
                countries.add(country);
            }
        }
        Collections.sort(countries);
        for (String country : countries) {
            // System.out.println(country);
        }
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the your spinner
        spinner.setAdapter(countryAdapter);
        country = spinner.getSelectedItem().toString();


        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        mcircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,PROFILE_PICTURE_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILE_PICTURE_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Preparing Your Picture");
                loadingBar.setMessage("Please Wait!");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                Uri resultUri = result.getUri();
                //mcircleImageView.setImageURI(resultUri);
                StorageReference filePath = mStorageReference.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SetupActivity.this, "Your Profile Picture Uploaded Successfully", Toast.LENGTH_SHORT).show();
                             downloadUrl = task.getResult().getDownloadUrl().toString();
                            mDatabaseReference.child("profielimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Picasso.get().load(downloadUrl).placeholder(R.drawable.profile).into(mcircleImageView);
                                        Toast.makeText(SetupActivity.this, "Profile Picture Stored", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, message , Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
                                    }
                                    }
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Image Can't Be Cropped Try Again!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }


    private void SaveAccountSetupInformation() {
        final String usernametext = username.getText().toString();
       final String fullnametext = fullName.getText().toString();
        final String countrytext = country;

        if (TextUtils.isEmpty(usernametext)){
            Toast.makeText(this, "Please Write a Valid UserName", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(fullnametext)){
            Toast.makeText(this, "Please Write Your Name", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(countrytext)){
            Toast.makeText(this, "Please Write Your Country", Toast.LENGTH_SHORT).show();
        }else if (downloadUrl == null){
            Toast.makeText(this, "Please Select a Profile Picture", Toast.LENGTH_SHORT).show();
        }else {
                loadingBar.setTitle("Saving Your Data");
                loadingBar.setMessage("Please Wait!");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                HashMap userMap = new HashMap();
                userMap.put("username",usernametext);
                userMap.put("fullname",fullnametext);
                userMap.put("country",countrytext);
                userMap.put("gender","null");
                userMap.put("dob","null");
                userMap.put("relationshipstatus","null");
                userMap.put("status","null");
                mDatabaseReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            SendUserToMainActivity();
                            Toast.makeText(SetupActivity.this, "Done", Toast.LENGTH_SHORT).show();
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }


    private void SendUserToMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
