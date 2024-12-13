package com.example.bluetoothconnection.utilities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class BluetoothThread extends Thread {
    private final BluetoothServerSocket serverSocket;
    private final BluetoothSocket clientSocket;
    private final BluetoothDevice device;
    private final BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "BluetoothThread";
    private final boolean isServer;
    private final Context context;

    // UUID for the Bluetooth service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "BluetoothService";


    // Server Constructor
    public BluetoothThread(Context ctx, BluetoothAdapter adapter, boolean serverMode) {
        bluetoothAdapter = adapter;
        device = null;
        clientSocket = null;
        BluetoothServerSocket tmp = null;
        isServer = serverMode;
        context = ctx;

        if (serverMode && checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Server socket creation failed", e);
            }
        }
        serverSocket = tmp;
    }

    // Client Constructor
    public BluetoothThread(Context ctx, BluetoothAdapter adapter, BluetoothDevice device) {
        bluetoothAdapter = adapter;
        this.device = device;
        serverSocket = null;
        isServer = false;
        BluetoothSocket tmp = null;
        context = ctx;

        if (checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Client socket creation failed", e);
            }
        }
        clientSocket = tmp;
    }

    @Override
    public void run() {
        if (isServer) {
            runServerMode();
        } else {
            runClientMode();
        }
    }

    private void runServerMode() {
        BluetoothSocket socket;

        while (true) {
            try {
                Log.d(TAG, "Waiting for a connection...");
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Server accept() failed", e);
                break;
            }

            if (socket != null) {
                Log.d(TAG, "Connection accepted!");
                manageMyConnectedSocket(socket);
                cancel();
                break;
            }
        }
    }

    private void runClientMode() {
        if (!checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.e(TAG, "Bluetooth connect permission not granted");
            return;
        }

        bluetoothAdapter.cancelDiscovery();

        try {
            clientSocket.connect();
            Log.d(TAG, "Connected to server!");
            manageMyConnectedSocket(clientSocket);
        } catch (IOException connectException) {
            Log.e(TAG, "Unable to connect; closing the socket.", connectException);
            try {
                clientSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket socket) {
        Log.d(TAG, "Managing connected socket...");
        // Handle the socket communication
    }

    public void cancel() {
        try {
            if (isServer && serverSocket != null) {
                serverSocket.close();
            } else if (!isServer && clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not close the socket", e);
        }
    }

    private boolean checkPermission(String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

}
