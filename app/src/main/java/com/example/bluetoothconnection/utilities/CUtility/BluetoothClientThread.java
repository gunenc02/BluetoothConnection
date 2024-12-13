package com.example.bluetoothconnection.utilities.CUtility;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothconnection.activities.MainActivity;
import com.example.bluetoothconnection.service.BluetoothService;
import com.example.bluetoothconnection.utilities.IUtility.IBluetoothConnection;

import java.io.IOException;
import java.util.UUID;

public class BluetoothClientThread extends Thread implements IBluetoothConnection {

    private final MainActivity ctx;
    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final String NAME = "DEVICE";
    private final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static String TAG = "BluetoothClientThread";
    private final BluetoothAdapter bluetoothAdapter;
    public BluetoothClientThread(MainActivity ctx, BluetoothDevice device, BluetoothAdapter adapter){
        this.ctx = ctx;
        BluetoothSocket tmpSocket = null;
        mmDevice = device;
        this.bluetoothAdapter = adapter;

        try {
            if(ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(ctx, "Necessary permissions should be given", Toast.LENGTH_SHORT).show();
                ctx.checkPermissions();
                return;
            }
            tmpSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
            tmpSocket = null;
        }
        mmSocket = tmpSocket;

    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        if(ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(ctx, "Necessary permissions should be given", Toast.LENGTH_SHORT).show();
            ctx.checkPermissions();
            return;
        }
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        Toast.makeText(ctx, "Connection successful", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Connection established successfully");
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

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {

        ctx.stopBluetoothServer();
        BluetoothService service = new BluetoothService();
    }

    @Override
    public void sendMessage() {

    }

    @Override
    public void receiveMessage() {

    }
}
