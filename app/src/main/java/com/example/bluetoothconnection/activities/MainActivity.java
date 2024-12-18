package com.example.bluetoothconnection.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bluetoothconnection.R;
import com.example.bluetoothconnection.utilities.CUtility.BluetoothClientThread;
import com.example.bluetoothconnection.utilities.CUtility.BluetoothServerThread;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private TextView tvStatus;
    private Button btnScanDevices;
    private LinearLayout deviceContainer;
    BluetoothServerThread server;
    BluetoothClientThread client; // if it is null, should not activate stop function



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        btnScanDevices = findViewById(R.id.btn_scan_devices);
        deviceContainer = findViewById(R.id.device_container);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        client = null;
        if (bluetoothAdapter == null) {
            tvStatus.setText("No Bluetooth Support");
            btnScanDevices.setEnabled(false);
            onDestroy();
        }
        checkPermissions();
        updateBluetoothStatus();
        btnScanDevices.setOnClickListener(v -> scanDevices());
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
        server = new BluetoothServerThread(this, bluetoothAdapter);
        server.start();
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(checkSelfPermission(android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                checkPermissions();
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addDeviceButton(device, false);  // Add newly detected devices as buttons
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Scan complete!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void updateBluetoothStatus() {
        if (bluetoothAdapter.isEnabled()) {
            tvStatus.setText("Bluetooth enabled");
        } else {
            tvStatus.setText("Bluetooth disabled");
        }
    }

    private void scanDevices() {
        if(checkSelfPermission(android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "You need to give necessary permissions", Toast.LENGTH_LONG).show();
            checkPermissions();
        }

        deviceContainer.removeAllViews();


        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.i("Entered", "Here");
        if(pairedDevices.size() > 0){
            if (pairedDevices != null && pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    addDeviceButton(device, true);  // Add paired devices as buttons
                }
            }
        } else {
            Toast.makeText(this, "No device Found", Toast.LENGTH_SHORT);
        }

        bluetoothAdapter.startDiscovery();
    }

    private void addDeviceButton (BluetoothDevice device, boolean isPaired){
        if(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "permission required", Toast.LENGTH_LONG).show();
        }
        Button deviceButton = new Button(this);
        String deviceInfo = (isPaired ? "Paired: " : "Detected: ") + device.getName() + " - " + device.getAddress();
        deviceButton.setText(deviceInfo);

        // Set click listener for each button
        deviceButton.setOnClickListener(v -> connectToDevice(device));

        // Add button to the layout
        deviceContainer.addView(deviceButton);
    }

    private void connectToDevice (BluetoothDevice device){
        if(checkSelfPermission(android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
            checkPermissions();
        }
            client = new BluetoothClientThread(this, device, bluetoothAdapter);
            client.start();
    }

    public void checkPermissions(){
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                    }, 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bluetoothReceiver == null){
            unregisterReceiver(bluetoothReceiver);
        }
    }

    public void stopBluetoothServer() {
        server.cancel();
    }

    public void stopBluetoothClient(){
        if(client == null){
            return;
        }
        client.cancel();
    }
}
