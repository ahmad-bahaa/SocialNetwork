package com.ninjageeksco.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText userEmail, userpassword, userConfirmPassword;
    private Button registerButton;

    private ProgressDialog loadingBar;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();

        userEmail = findViewById(R.id.register_email);
        userpassword = findViewById(R.id.register_password);
        userConfirmPassword = findViewById(R.id.register_confrim_password);
        registerButton = findViewById(R.id.register_create_account);

        loadingBar = new ProgressDialog(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = userEmail.getText().toString();
        String password = userpassword.getText().toString();
        String confirmpasswrod = userConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please Write a valid Email",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please Write a Password",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(confirmpasswrod)){
            Toast.makeText(this,"Please Confirm Your Password",Toast.LENGTH_SHORT).show();
        }else if (!password.equals(confirmpasswrod)){
            Toast.makeText(this,"Make Sure you Confirmed Your Password Correctly",Toast.LENGTH_SHORT).show();
        }else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait!");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mFirebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                SendUserToSetupActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully <3 :*", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }else {
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
    protected void onStart() {
        super.onStart();
        FirebaseUser currFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (currFirebaseUser != null) {
            SendUserToMainActivity();
        }
    }

    private void SendUserToSetupActivity() {
        Intent intent = new Intent(this, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void SendUserToMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
