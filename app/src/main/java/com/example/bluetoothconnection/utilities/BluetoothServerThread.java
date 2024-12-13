package com.example.bluetoothconnection.utilities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothconnection.activities.MainActivity;

import java.io.IOException;
import java.util.UUID;

public class BluetoothServerThread extends Thread {
    private BluetoothServerSocket mmServerSocket;
    private MainActivity ctx;
    private final String NAME = "DEVICE";
    private final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter;
    private final static String TAG = "Error in BluetoothThread";

    public BluetoothServerThread(MainActivity ctx, BluetoothAdapter adapter) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        this.bluetoothAdapter = adapter;
        this.ctx = ctx;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            if(ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(ctx, "You need to give necessary permissions", Toast.LENGTH_SHORT).show();
                ctx.checkPermissions();
                return;
            }
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                try{
                    manageMyConnectedSocket(socket);
                    mmServerSocket.close();
                    break;
                } catch (Exception ex){
                    Log.e(TAG, "In run function");
                }

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

    private void manageMyConnectedSocket(BluetoothSocket socket) {
    }

}
