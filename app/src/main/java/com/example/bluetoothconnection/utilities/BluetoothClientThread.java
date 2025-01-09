package com.example.bluetoothconnection.utilities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.IOException;
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

    public static BluetoothClientThread getClientThread(Context ctx, BluetoothDevice device, BluetoothAdapter adapter, BluetoothSocket socket){
        if(client == null){
            client = new BluetoothClientThread(ctx, device, adapter, socket);
        }
        return client;
    }

    public static Boolean isExists(){
        return client != null;
    }

    public static BluetoothSocket getSocket(){
        return mmSocket;
    }
    public BluetoothClientThread(Context ctx, BluetoothDevice device, BluetoothAdapter adapter, BluetoothSocket socket){
        this.ctx = ctx;
        mmSocket = socket;
        mmDevice = device;
        this.bluetoothAdapter = adapter;

    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        if(ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ctx.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED){
            checkPermissions();
        }
        bluetoothAdapter.cancelDiscovery();
        Boolean tmp = mmDevice.createBond();
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            Log.e(TAG, "Connection failed, trying to close the socket", connectException);
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
        }
    }
    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    private void checkPermissions(){
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
        };

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
