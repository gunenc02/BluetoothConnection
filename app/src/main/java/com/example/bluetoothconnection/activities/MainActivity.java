package com.example.bluetoothconnection.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import com.example.bluetoothconnection.listener.SocketStateListener;
import com.example.bluetoothconnection.utilities.BluetoothClientThread;
import com.example.bluetoothconnection.utilities.BluetoothServerThread;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SocketStateListener {
    private BluetoothAdapter bluetoothAdapter;
    private TextView tvStatus;
    private Button btnScanDevices;
    private LinearLayout deviceContainer;
    BluetoothServerThread server;
    BluetoothClientThread client; // if it is null, should not activate stop function
    private final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static String TAG = "MainActivity";
    BluetoothSocket socket;
    private Boolean isPermissionsRequested = false;



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
            return;
        }
        updateBluetoothStatus();
        checkPermissions();
        btnScanDevices.setOnClickListener(v -> scanDevices());
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
        startBluetoothServerThread();
        makeVisible();
    }

    @Override
    public void onRestart(){

        super.onRestart();
        if(client != null){
            client.cancel();
            client = null;
        }
        if(server != null){
            server.cancel();
            server = null;
        }
        startBluetoothServerThread();
    }

    private void startBluetoothServerThread(){
        server = BluetoothServerThread.getBluetoothServerThread(this, bluetoothAdapter, this);
        server.start();
    }
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED){
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
        if(checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED){
            checkPermissions();
        }

        deviceContainer.removeAllViews();


        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        bluetoothAdapter.startDiscovery();
        if(pairedDevices.size() > 0){
            if (pairedDevices != null && pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    addDeviceButton(device, true);  // Add paired devices as buttons
                }
            }
        }


    }

    private void addDeviceButton (BluetoothDevice device, boolean isPaired){

        Button deviceButton = new Button(this);
        @SuppressLint("MissingPermission") String deviceInfo = (isPaired ? "Paired: " : "Detected: ") + device.getName() + " - " + device.getAddress();
        deviceButton.setText(deviceInfo);

        // Set click listener for each button
        deviceButton.setOnClickListener(v -> connectToDevice(device));

        // Add button to the layout
        deviceContainer.addView(deviceButton);
    }

    private void connectToDevice (BluetoothDevice device){
        if(checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED){
            checkPermissions();
        }

        BluetoothSocket tmp;
        try {
            if(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
                checkPermissions();
            }
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
            tmp = null;
        }
        socket = tmp;
        client = BluetoothClientThread.getClientThread(this, device, bluetoothAdapter, socket, this);
        client.start();
    }


    private void checkPermissions(){

        if(isPermissionsRequested){
            return;
        }
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
        };

        boolean permissionNeeded = false;

        for (String permission : permissions) {
            if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded = true;
                break;
            }
        }

        if (permissionNeeded) {
            if (this instanceof Activity) {
                this.requestPermissions(permissions, 1);
            } else {
                Log.e(TAG, "Context is not an instance of Activity. Cannot request permissions.");
            }
        }
        isPermissionsRequested = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bluetoothReceiver == null){
            unregisterReceiver(bluetoothReceiver);
        }
    }

    public void stopBluetoothServer() {
        if(server == null){
            return;
        }
        server.cancel();
        server = null;
    }

    public void stopBluetoothClient(){
        if(client == null){
            return;
        }
        client.cancel();
        client = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onClientListener(BluetoothSocket socket) {
        stopBluetoothServer();
        Intent intent = new Intent(this, BluetoothChatActivity.class);
        intent.putExtra("device_name", socket.getRemoteDevice().getName());
        intent.putExtra("device_address", socket.getRemoteDevice().getAddress());
        startActivity(intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onServerListener(BluetoothSocket socket) {
        stopBluetoothClient();
        Intent intent = new Intent(this, BluetoothChatActivity.class);
        intent.putExtra("device_name", socket.getRemoteDevice().getName());
        intent.putExtra("device_address", socket.getRemoteDevice().getAddress());
        startActivity(intent);
    }

    @Override
    public void onFailClientListener(BluetoothClientThread thread) {
        thread.cancel();
    }

    private void makeVisible(){
        if(checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED){
            checkPermissions();
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); // 5 minutes
        startActivity(discoverableIntent);
    }

}
