package com.example.bluetoothconnection.utilities.CUtility;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothconnection.activities.MainActivity;
import com.example.bluetoothconnection.utilities.IUtility.IBluetoothConnection;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class BluetoothServerThread extends Thread implements IBluetoothConnection {
    private final CountDownLatch permissionLatch = new CountDownLatch(1);
    private BluetoothServerSocket mmServerSocket;
    private Context ctx;
    private final String NAME = "DEVICE";
    private final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter;
    private final static String TAG = "Error in BluetoothThread";

    public BluetoothServerThread(MainActivity ctx, BluetoothAdapter adapter) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        this.bluetoothAdapter = adapter;
        this.ctx = ctx;
    }

    public void run() {
        BluetoothSocket socket;
        BluetoothServerSocket tmp = null;

        // Wait for permissions
        if(ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            waitForPermissions();
        }

        try {
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
            return;
        }
        mmServerSocket = tmp;

        // Keep listening until a socket is accepted or an exception occurs
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // Handle the connected socket
                try {
                    manageMyConnectedSocket(socket);
                    mmServerSocket.close();
                    break;
                } catch (Exception ex) {
                    Log.e(TAG, "Error managing connected socket", ex);
                }
            }
        }
    }

    private void waitForPermissions() {
        new Thread(() -> {
            while (ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Permission wait interrupted", e);
                }
            }
            permissionLatch.countDown();
        }).start();

        try {
            permissionLatch.await(); 
        } catch (InterruptedException e) {
            Log.e(TAG, "Permission latch interrupted", e);
        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // API 31+
            String[] permissions = {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

            requestPermissionsIfNeeded(permissions);
        } else {
            String[] permissions = {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

            requestPermissionsIfNeeded(permissions);
        }
    }

    private void requestPermissionsIfNeeded(String[] permissions) {
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
                Toast.makeText(ctx, "Permissions cannot be requested.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket socket) {
    }

    @Override
    public void sendMessage() {

    }

    @Override
    public void receiveMessage() {

    }
}
