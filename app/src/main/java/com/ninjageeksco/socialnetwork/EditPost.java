package com.ninjageeksco.socialnetwork;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class EditPost extends AppCompatActivity {

    private Button editPost,deletePost;
    private ImageView postImageView;
    private TextView post;
    private String postKey,currentUserId,postUserID,postDcr;
    private DatabaseReference clickPost;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postKey = getIntent().getExtras().getString("postKey");

        postImageView = findViewById(R.id.post_image_view);
        post = findViewById(R.id.post_describ);
        editPost = findViewById(R.id.post_edit);
        editPost.setVisibility(View.INVISIBLE);
        deletePost = findViewById(R.id.post_delete);
        deletePost.setVisibility(View.INVISIBLE);
        clickPost = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);

        clickPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                     postDcr = dataSnapshot.child("describtion").getValue().toString();
                    String imageUrl = dataSnapshot.child("postimage").getValue().toString();
                    postUserID = dataSnapshot.child("uid").getValue().toString();
                    post.setText(postDcr);
                    Picasso.get().load(imageUrl).into(postImageView);
                    if (currentUserId.equals(postUserID)){
                        deletePost.setVisibility(View.VISIBLE);
                        editPost.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeletePost();
            }
        });
        editPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditCurrentPost(postDcr);
            }
        });
    }

    private void EditCurrentPost(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Post");
        final EditText inpustField = new EditText(this);
        inpustField.setText(str);
        builder.setView(inpustField);
        builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            clickPost.child("describtion").setValue(inpustField.getText().toString());
                Toast.makeText(EditPost.this, "Post Has Been Updated", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeletePost() {
        clickPost.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Post Has Been Deleted", Toast.LENGTH_SHORT).show();
    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
