package com.example.bluetoothconnection.listener;

import android.bluetooth.BluetoothSocket;

import com.example.bluetoothconnection.utilities.BluetoothClientThread;

public interface SocketStateListener {
    void onServerListener(BluetoothSocket socket);
    void onClientListener(BluetoothSocket socket);
    void onFailClientListener(BluetoothClientThread thread);
}
