package com.ninjageeksco.socialnetwork;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mStatus,mGender,mRelationship,mFullname,mUsername,mCountry,mdob;
    private Button button;
    private CircleImageView pp;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pp = findViewById(R.id.settings_pp);
        mStatus = findViewById(R.id.settings_status);
        mUsername= findViewById(R.id.settings_username);
        mFullname = findViewById(R.id.settings_fullname);
        mCountry = findViewById(R.id.settings_country);
        mRelationship =  findViewById(R.id.settings_realstionship);
        mGender = findViewById(R.id.settings_gender);
        mdob =  findViewById(R.id.settings_dateofbirth);
        button = findViewById(R.id.settings_button);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    mUsername.setText(dataSnapshot.child("username").getValue().toString());
                    mFullname.setText(dataSnapshot.child("fullname").getValue().toString());
                    mCountry.setText(dataSnapshot.child("country").getValue().toString());
                    mdob.setText(dataSnapshot.child("dob").getValue().toString());
                    mRelationship.setText(dataSnapshot.child("relationshipstatus").getValue().toString());
                    mGender.setText(dataSnapshot.child("gender").getValue().toString());
                    mStatus.setText(dataSnapshot.child("status").getValue().toString());
                    Picasso.get().load(dataSnapshot.child("profielimage").getValue().toString())
                            .placeholder(R.drawable.profile).into(pp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
