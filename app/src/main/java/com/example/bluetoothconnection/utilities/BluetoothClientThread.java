package com.example.bluetoothconnection.utilities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothconnection.activities.MainActivity;
import com.example.bluetoothconnection.listener.SocketStateListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BluetoothClientThread extends Thread {

    private final Context ctx;
    private static BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final String NAME = "DEVICE";
    private final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static String TAG = "BluetoothClientThread";
    private final BluetoothAdapter bluetoothAdapter;
    private static BluetoothClientThread client;
    private SocketStateListener listener;

    public static BluetoothClientThread getClientThread(Context ctx, BluetoothDevice device, BluetoothAdapter adapter){
        if(client == null){
            client = new BluetoothClientThread(ctx, device, adapter);
        }
        return client;
    }

    public static Boolean isExists(){
        return client != null;
    }

    public static BluetoothSocket getSocket(){
        return mmSocket;
    }
    public BluetoothClientThread(Context ctx, BluetoothDevice device, BluetoothAdapter adapter){
        this.ctx = ctx;
        mmDevice = device;
        this.bluetoothAdapter = adapter;
    }

    public void run() {
        if(ctx.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED){
            checkPermissions();
        }
        BluetoothSocket tmp;
        try {
            tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
            tmp = null;
        }
        mmSocket = tmp;
        bluetoothAdapter.cancelDiscovery();
        try {
            mmSocket.connect();
            isConnectionAccepted();
        } catch (IOException connectException) {
            Log.e(TAG, "Connection failed, trying to close the socket", connectException);
        }
    }

    private void isConnectionAccepted() {
        try {
            InputStream stream = mmSocket.getInputStream();
            int result = stream.read(); // think as if they are boolean, 1 for true, 0 for false
            if(result == 1){
                ((MainActivity)ctx).onClientListener();
            } else {
                ((MainActivity)ctx).runOnUiThread(() -> {
                    Toast.makeText(ctx, "Connection rejected", Toast.LENGTH_SHORT).show();
                });
                ((MainActivity)ctx).onFailClientListener(this);
            }
        } catch (IOException e) {
            ((MainActivity)ctx).onFailClientListener(this);
            throw new RuntimeException(e);
        }
    }
    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            if(mmSocket.isConnected()){
                mmSocket.close();
            }
            client = null;
            this.join();
        } catch (Exception e) {
            Log.e(TAG, "Could not close the client socket", e);
        }

    }

    private void checkPermissions(){
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
            if (ctx.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded = true;
                break;
            }
        }

        if (permissionNeeded) {
            if (ctx instanceof android.app.Activity) {
                ((android.app.Activity) ctx).requestPermissions(permissions, 1);
            } else {
                Log.e(TAG, "Context is not an instance of Activity. Cannot request permissions.");
            }
        }
    }

}
