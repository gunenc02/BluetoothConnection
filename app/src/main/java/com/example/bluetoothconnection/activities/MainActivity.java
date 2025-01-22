package com.example.bluetoothconnection.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.bluetoothconnection.R;
import com.example.bluetoothconnection.listener.SocketStateListener;
import com.example.bluetoothconnection.utilities.BluetoothClientThread;
import com.example.bluetoothconnection.utilities.BluetoothServerThread;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements SocketStateListener {
    private BluetoothAdapter bluetoothAdapter;
    private Button btnScanDevices;
    private Button btnMakeDetectable;
    private LinearLayout deviceContainer;
    BluetoothServerThread server;
    BluetoothClientThread client; // if it is null, should not activate stop function
    private final static String TAG = "MainActivity";
    private Boolean isPermissionsRequested = false;
    Intent discoverableIntent;
    Boolean isDetectable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isDetectable = false;
        btnScanDevices = findViewById(R.id.btn_scan_devices);
        deviceContainer = findViewById(R.id.device_container);
        btnMakeDetectable = findViewById(R.id.btn_device_enable);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        client = null;

        if (bluetoothAdapter == null) {
            btnScanDevices.setEnabled(false);
            onDestroy();
            return;
        }

        checkPermissions();
        btnScanDevices.setOnClickListener(v -> scanDevices());
        btnMakeDetectable.setOnClickListener(v -> makeDetectable());

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            makeVisible();
        }
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

    private void scanDevices() {
        if(checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED){
            checkPermissions();
        }

        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            isDetectable = false;
            Toast.makeText(this, "You need to make your device detectable", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isDetectable){
            Toast.makeText(this, "You need to make your device detectable", Toast.LENGTH_SHORT).show();
            return;
        }

        deviceContainer.removeAllViews();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        bluetoothAdapter.startDiscovery();
        if(!pairedDevices.isEmpty()){
            for (BluetoothDevice device : pairedDevices) {
                addDeviceButton(device, true);
            }
        }


    }

    private void makeDetectable(){
        isDetectable = true;
        makeVisible();
        if(server == null){
            startBluetoothServerThread();
        }
    }

    private void addDeviceButton (BluetoothDevice device, boolean isPaired){

        Button deviceButton = new Button(this);
        @SuppressLint("MissingPermission") String deviceInfo = (isPaired ? "Paired: " : "Detected: ") + device.getName() + " - " + device.getAddress();
        deviceButton.setText(deviceInfo);

        deviceButton.setOnClickListener(v -> connectToDevice(device));

        deviceContainer.addView(deviceButton);
    }

    private void connectToDevice (BluetoothDevice device){
        client = BluetoothClientThread.getClientThread(this, device, bluetoothAdapter);
        if(client.getState() == Thread.State.NEW){
            client.start();
        }
    }

    private void startBluetoothServerThread(){
        server = BluetoothServerThread.getBluetoothServerThread(this, bluetoothAdapter);
        if(server.getState() == Thread.State.NEW){
            server.start();
        }
        logServerState(server);
    }

    private void checkPermissions(){
        if(isPermissionsRequested){
            return;
        }

        String[] generalPermissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
        };
        String [] permissions;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissions = new String[6];
            System.arraycopy(generalPermissions, 0, permissions, 0, 6);
        } else {
            permissions = new String[3];
            System.arraycopy(generalPermissions, 0, permissions, 0, 3);
        }

        boolean permissionNeeded = false;

        for (String permission : permissions) {
            if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded = true;
                break;
            }
        }

        if (permissionNeeded) {
            this.requestPermissions(permissions, 1);
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
    public void onClientListener() {
        stopBluetoothServer();
        Intent intent = new Intent(this, BluetoothChatActivity.class);
        startActivity(intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onServerListener() {
        stopBluetoothClient();
        Intent intent = new Intent(this, BluetoothChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFailClientListener(BluetoothClientThread client) {
        client.cancel();
        this.client = null;
        onRestart();
    }

    private void makeVisible(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED){
            checkPermissions();
            Toast.makeText(this, "permission needed", Toast.LENGTH_SHORT).show();
            return;
        }
        discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    private void logServerState(BluetoothServerThread thread){
        Log.i(TAG, thread.getState().toString());
    }
}
