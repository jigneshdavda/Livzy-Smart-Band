package com.example.fireion.smartband;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by fireion on 6/10/17.
 */

public class StepsActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = StepsActivity.class.getSimpleName();
    TextView textView, textView1;
    SensorManager sensorManager;
    Sensor stepSensor;
    private long steps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            }
        }

        textView = (TextView) findViewById(R.id.textData);
        textView1 = (TextView) findViewById(R.id.textData1);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        float[] values = sensorEvent.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }

        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
//            Log.d(TAG, "onSensorChanged: " + steps);
            steps = ++steps;
            getDistanceRun(steps);
            textView.setText(String.valueOf(steps));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, stepSensor);
    }

    public void getDistanceRun(long steps) {
        Log.d(TAG, "getDistanceRun: " + steps);
        float distance = (float) (steps * 78) / (float) 100000;
//        float distance = (float) (steps) / (float) 2000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            distance = Float.parseFloat(new DecimalFormat("##.##").format(distance));
        }
        Log.d(TAG, "getDistanceRun: " + distance);
        textView1.setText(String.valueOf(distance));
    }

    public void getCalories() {

    }
}
