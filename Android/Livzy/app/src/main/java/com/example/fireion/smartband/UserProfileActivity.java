package com.example.fireion.smartband;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = UserProfileActivity.class.getSimpleName();
    Spinner spinnerGender;
    Button buttonUserProfileFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        spinnerGender = findViewById(R.id.spinner_gender);
        buttonUserProfileFinish = findViewById(R.id.userProfile_finish);

        spinnerGender.setOnItemSelectedListener(this);
        List<String> genderType = new ArrayList<>();
        genderType.add("Select Gender");
        genderType.add("Male");
        genderType.add("Female");
        genderType.add("TransGender");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, genderType);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(arrayAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(this, item, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.e(TAG, "onNothingSelected: " + adapterView);
    }

    public void userProfileToMain(View view) {
        Intent intentToMainActivity = new Intent(this, MainActivity.class);
        startActivity(intentToMainActivity);
    }
}
