package com.example.bluetoothconnection.utilities.CUtility;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothconnection.activities.BluetoothChatActivity;
import com.example.bluetoothconnection.activities.MainActivity;
import com.example.bluetoothconnection.service.BluetoothService;
import com.example.bluetoothconnection.utilities.IUtility.IBluetoothConnection;

import java.io.IOException;
import java.util.UUID;

public class BluetoothClientThread extends Thread implements IBluetoothConnection {

    private final Context ctx;
    private BluetoothSocket mmSocket;
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
    public BluetoothClientThread(Context ctx, BluetoothDevice device, BluetoothAdapter adapter, BluetoothSocket socket){
        this.ctx = ctx;
        this.mmSocket = socket;
        mmDevice = device;
        this.bluetoothAdapter = adapter;

    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        if(ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(ctx, "Necessary permissions should be given", Toast.LENGTH_SHORT).show();
            checkPermissions();
            return;
        }
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            Toast.makeText(ctx, "nothing wrong about the connect function", Toast.LENGTH_LONG).show();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
        manageMyConnectedSocket(mmSocket);
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
                Toast.makeText(ctx, "Permissions cannot be requested.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {

        BluetoothService service = new BluetoothService();
    }

    @Override
    public void sendMessage() {

    }

    @Override
    public void receiveMessage() {

    }
}
