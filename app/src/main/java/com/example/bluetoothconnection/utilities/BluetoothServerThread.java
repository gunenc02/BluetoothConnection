package com.example.bluetoothconnection.utilities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssAntennaInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothconnection.activities.MainActivity;
import com.example.bluetoothconnection.listener.SocketListener;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class BluetoothServerThread extends Thread {
    private final CountDownLatch permissionLatch = new CountDownLatch(1);
    private BluetoothServerSocket mmServerSocket;
    private Context ctx;
    private final String NAME = "DEVICE";
    private final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter;
    private final static String TAG = "Error in BluetoothServerThread";
    private static BluetoothSocket socket;
    private static BluetoothServerThread server = null;
    private SocketListener listener;

    public static BluetoothServerThread getBluetoothServerThread(Context ctx, BluetoothAdapter adapter, SocketListener listener){
        if(server == null){
            return new BluetoothServerThread(ctx, adapter, listener);
        }
        return server;
    }

    public static Boolean isExists(){
        return server != null;
    }

    public static BluetoothSocket getSocket() {
        return socket;
    }
    public BluetoothServerThread(Context ctx, BluetoothAdapter adapter, SocketListener listener) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        this.bluetoothAdapter = adapter;
        this.ctx = ctx;
        this.listener = listener;
    }

    public void run() {
        BluetoothServerSocket tmp = null;

        // Wait for permissions
        if(ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            checkPermissions();
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
            listener.onServerListener(socket);
            if (socket != null) {
                // Handle the connected socket
                try {
                    mmServerSocket.close();
                    break;
                } catch (Exception ex) {
                    Log.e(TAG, "Error managing connected socket", ex);
                }
            }
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

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
