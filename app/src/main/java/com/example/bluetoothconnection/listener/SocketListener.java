package com.example.bluetoothconnection.listener;

import android.bluetooth.BluetoothSocket;

public interface SocketListener {
    void onServerListener(BluetoothSocket socket);
    void onClientListener(BluetoothSocket socket);
}
