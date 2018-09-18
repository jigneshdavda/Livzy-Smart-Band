package com.example.fireion.smartband;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity {
    TextView textViewRegistertoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textViewRegistertoLogin = findViewById(R.id.login_register);
    }

    public void registerToLogin(View view) {
        Intent intentRegistertoLogin = new Intent(this, LoginActivity.class);
        intentRegistertoLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentRegistertoLogin);
        finish();
    }
}
