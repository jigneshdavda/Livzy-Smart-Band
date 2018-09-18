package com.example.fireion.smartband;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    TextView textViewLoginToRegister;
    Button buttonValidateAndLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textViewLoginToRegister = findViewById(R.id.createAccount_login);
        buttonValidateAndLogin = findViewById(R.id.signIn_login);
    }

    public void loginToRegister(View view) {
        Intent intentLogintoRegister = new Intent(this, RegisterActivity.class);
        startActivity(intentLogintoRegister);
        finish();
    }

    public void validateAndLogin(View view) {
        Intent intentUserProfile = new Intent(this, UserProfileActivity.class);
        startActivity(intentUserProfile);
    }
}