package com.example.bluetoothconnection.utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.example.bluetoothconnection.activities.MainActivity;
import com.example.bluetoothconnection.listener.SocketStateListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class BluetoothServerThread extends Thread {
    private BluetoothServerSocket mmServerSocket;
    private final Context ctx;
    private final String NAME = "DEVICE";
    private final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter;
    private final static String TAG = "Error in BluetoothServerThread";
    private static BluetoothSocket socket;
    private static BluetoothServerThread server = null;

    public static BluetoothServerThread getBluetoothServerThread(Context ctx, BluetoothAdapter adapter){
        if(server == null){
            server = new BluetoothServerThread(ctx, adapter);
        }
        return server;
    }
/*
    public static Boolean isExists(){
        // isExists version of the BluetoothClientThread is used so this becomes unnecessary
        return server != null;
    }
 */

    public static BluetoothSocket getSocket() {
        return socket;
    }
    @SuppressLint("MissingPermission")
    public BluetoothServerThread(Context ctx, BluetoothAdapter adapter) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        this.bluetoothAdapter = adapter;
        this.ctx = ctx;
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
                answerConnectionRequest(socket);
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }
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

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
            if(socket.isConnected()){
                socket.close();
            }
            server = null;
            this.join();
        } catch (Exception e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }

    }

    public void answerConnectionRequest(BluetoothSocket socket){
        ((MainActivity) ctx).runOnUiThread(() -> {
            @SuppressLint("MissingPermission") String name = socket.getRemoteDevice().getName();
            String address = socket.getRemoteDevice().getAddress();
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("Connection Request");
            builder.setMessage("Device: " + name + "\nAddress: " + address + "\nAccept the connection?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                dialog.dismiss();
                try {
                    OutputStream stream = socket.getOutputStream();
                    stream.write(1);
                    ((MainActivity)ctx).onServerListener();
                    try {
                        mmServerSocket.close();
                    } catch (Exception ex) {
                        Log.e(TAG, "Error managing connected socket", ex);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
                try {
                    OutputStream stream = socket.getOutputStream();
                    stream.write(0);
                    socket.close();
                } catch (IOException e) {
                    Log.e("MainActivity", "Error closing socket", e);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }
}
