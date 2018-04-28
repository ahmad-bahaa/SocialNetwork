package com.ninjageeksco.socialnetwork;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postlist;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private CircleImageView navProfileImage;
    private TextView navProfileUsername;
    //Firebase
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference,mPostsRef;
    // user
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mPostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //navigationView
        navigationView = findViewById(R.id.navigation_view);
        View navview = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = navview.findViewById(R.id.nav_profile_image);
        navProfileUsername = navview.findViewById(R.id.nav_user_full_name);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendUserToPostActivity();
                }
            });


        postlist = findViewById(R.id.all_users_post_list);
        postlist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postlist.setLayoutManager(linearLayoutManager);

       //setting the username and profile image to the navigation
        mDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("fullname").exists()){
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    navProfileUsername.setText(fullname);
                }
                if (dataSnapshot.child("profielimage").exists()){
                    String image = dataSnapshot.child("profielimage").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenueSelector(item);
                return false;
            }
        });
        postlist.setAdapter(adapter);
        postlist.setLayoutManager(linearLayoutManager);

    }

        Query query = FirebaseDatabase.getInstance()
                .getReference().child("Posts").limitToLast(50);
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(query, Posts.class)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            public PostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_post_layout, parent, false);
                return new PostsViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(PostsViewHolder holder, int position, Posts model) {
                final String postKey = getRef(position).getKey();
                // Bind the Chat object to the ChatHolder
                // ...
                holder.setFullname(model.getFullname());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setprfileimage(model.getPrfileimage());
                holder.setDescribtion(model.getDescribtion());
                holder.setPostimage(model.getPostimage());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this,EditPost.class);
                        intent.putExtra("postKey",postKey);
                        startActivity(intent);
                    }
                });
            }
        };


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        FirebaseUser currFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (currFirebaseUser == null){
            SendUserToLoginActivity();
        }else {
            ChechUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void ChechUserExistance() {
            final String CURRENT_USER_ID = mFirebaseAuth.getCurrentUser().getUid();
            mDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(CURRENT_USER_ID)){
                        SendUserToSetupActivity();
                        finish();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item))
        {return true;}
        return super.onOptionsItemSelected(item);
    }
    private void UserMenueSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
            case R.id.nav_home:
                Toast.makeText(this,"Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this,"Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                Toast.makeText(this,"Find Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this,"Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_logout:
                mFirebaseAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUserToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void SendUserToProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void SendUserToPostActivity(){
        Intent logintIntent = new Intent(this, PostActivity.class);
        startActivity(logintIntent);
    }
    public void SendUserToLoginActivity(){
        Intent logintIntent = new Intent(this, LoginActivity.class);
        logintIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logintIntent);
        finish();
    }
    private void SendUserToSetupActivity() {
        Intent setupActivity = new Intent(this, SetupActivity.class);
        setupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupActivity);
        finish();
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setTime(String time) {
            TextView mtime = mView.findViewById(R.id.post_profile_time);
            mtime.setText(" at " + time);
        }
        public void setDate(String date) {
            TextView mdate = mView.findViewById(R.id.post_profile_date);
            mdate.setText(": " + date);
        }
        public void setFullname(String fullname) {
            TextView tfullname = mView.findViewById(R.id.post_profile_name);
            tfullname.setText(fullname);
        }
        public void setPostimage(String postimage) {
            ImageView mpostimage = mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).placeholder(R.drawable.select_image).into(mpostimage);
        }
        public void setDescribtion(String describtion) {
            TextView mdescribtion = mView.findViewById(R.id.post_profile_desc);
            mdescribtion.setText(describtion);
        }
        public void setprfileimage(String prfileimage) {
            CircleImageView zprofileimage = mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(prfileimage).placeholder(R.drawable.profile).into(zprofileimage);
        }
    }

}
