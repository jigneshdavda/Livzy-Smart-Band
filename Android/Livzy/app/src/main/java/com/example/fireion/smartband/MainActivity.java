package com.example.fireion.smartband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    boolean deviceConnected = false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;
    TextView textViewStepsCount, textViewDistance, textViewHeartRate, textViewCalories;
    CardView cardViewBluetoothTurnOff;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewStepsCount = findViewById(R.id.stepsCount);
        textViewDistance = findViewById(R.id.distance);
        textViewHeartRate = findViewById(R.id.heartRate);
        textViewCalories = findViewById(R.id.calories);

        cardViewBluetoothTurnOff = findViewById(R.id.bluetoothTurnOff);

        if (BTinit()) {
            if (BTconnect()) {
                deviceConnected = true;
                beginListenForData();
                Toast.makeText(this, "Receiving Data", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Bluetooth not Initialized", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't Support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = null;
        if (bluetoothAdapter != null) {
            bondedDevices = bluetoothAdapter.getBondedDevices();
        }
        if (bondedDevices != null) {
            if (bondedDevices.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please Pair device in bluetooth settings", Toast.LENGTH_SHORT).show();
            } else {
                for (BluetoothDevice iterator : bondedDevices) {
                    String DEVICE_ADDR = "00:21:13:03:84:2B";
                    Log.d(TAG, "BTinit: " + iterator.getName() + " " + iterator.getAddress());
                    if (iterator.getAddress().equals(DEVICE_ADDR)) {
                        device = iterator;
                        found = true;
                        break;
                    } else {
                        Toast.makeText(this, "No such device found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            Toast.makeText(this, "No Bonded devices found", Toast.LENGTH_SHORT).show();
        }
        return found;
    }

//    private void sendDataToArduino(final String s) {
//        final Handler handler = new Handler();
//        stopThread = false;
//        Thread thread = new Thread(new Runnable() {
//            public void run() {
//                while (!Thread.currentThread().isInterrupted() && !stopThread) {
//                    handler.post(new Runnable() {
//                        public void run() {
//                            try {
//                                outputStream.write(s.getBytes());
//                                Log.d(TAG, "run: " + s);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                stopThread = true;
//                            }
//                        }
//                    });
//                }
//            }
//        });
//
//        thread.start();
//    }

    public boolean BTconnect() {
        boolean connected = true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        if (connected) {
            try {
                outputStream = socket.getOutputStream();
//                sendDataToArduino("1");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream = socket.getInputStream();
                cardViewBluetoothTurnOff.setVisibility(View.GONE);
                Toast.makeText(this, "Connected to: " + device.getName(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "No such Device found", Toast.LENGTH_SHORT).show();
        }

        return connected;
    }

//    public void onClickStart(View view) {
//        if (BTinit()) {
//            if (BTconnect()) {
////                setUiEnabled(true);
//                deviceConnected = true;
//                beginListenForData();
////                textView.append("\nConnection Opened!\n");
//                Toast.makeText(this, "Connection Opened", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    void beginListenForData() {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");
                            handler.post(new Runnable() {
                                public void run() {
                                    sendDataForSegregation(string);
//                                    Log.d(TAG, "run: Result: " + string);
                                }
                            });
                        }
                    } catch (IOException ex) {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    private void sendDataForSegregation(String string) {

        Log.d(TAG, "sendDataForSegregation: String: " + string);

        String calories;
        String[] split = string.split(";");
//        Log.d(TAG, "sendDataForSegregation: Array " + Arrays.toString(split));

        if (split.length > 0) {
//            String heartRate = split[0].trim();
            String steps = split[0].trim();
            String distance = split[1].trim();
            String heartRate = split[2].trim();

//            textViewHeartRate.setText(heartRate);
            textViewStepsCount.setText(steps);
            if (Integer.parseInt(distance) > 1000) {
                textViewDistance.setText(distance + " km");
            } else {
                textViewDistance.setText(distance + " m");
            }
            calories = String.valueOf(Math.round(Integer.parseInt(steps) * 0.05));
            textViewCalories.setText(calories + " cal");
            textViewHeartRate.setText(heartRate);

            if (split.length >= 4) {
                String trigger = split[3].trim();
                if (trigger.equals("s")) {
                    sendEmergencyAlertMessage(trigger);
                } else {
                    Log.d(TAG, "sendDataForSegregation: null data");
                }
            } else {
                Log.d(TAG, "sendDataForSegregation: Split length is not 3");
            }
        } else {
            Log.d(TAG, "sendDataForSegregation: Empty array");
        }
    }

    private void sendEmergencyAlertMessage(String trigger) {

        GPSTracker gpsTracker = new GPSTracker(this);
        String latitude = String.valueOf(gpsTracker.getLatitude());
        String longitude = String.valueOf(gpsTracker.getLongitude());

        String[] stringsPhoneNumber = {"9619337636", "8693812076", "9773340195"};
        String message = "I am in an emergency. My location is (http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + ")";
        Log.d(TAG, "sendEmergencyAlertMessage: Location: " + gpsTracker.getLatitude() + "," + gpsTracker.getLongitude());

        if (trigger.equals("s")) {
            SmsManager smsManager = SmsManager.getDefault();

            for (String aStringsPhoneNumber : stringsPhoneNumber) {
                smsManager.sendTextMessage(aStringsPhoneNumber, null, message, null, null);
            }
        } else {
            Toast.makeText(this, "Error while sending message", Toast.LENGTH_SHORT).show();
        }
    }

//    public void onClickSend(View view) {
//        String string = editText.getText().toString();
//        string.concat("\n");
//        try {
//            outputStream.write(string.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        textView.append("\nSent Data:" + string + "\n");
//
//    }

//    public void onClickStop(View view) throws IOException {
//        stopThread = true;
//        outputStream.close();
//        inputStream.close();
//        socket.close();
//        deviceConnected = false;
////        textView.append("\nConnection Closed!\n");
//        Toast.makeText(this, "Connection Closed", Toast.LENGTH_SHORT).show();
//    }

//    public void onClickClear(View view) {
//        textView.setText("");
//    }
}