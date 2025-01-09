package com.example.bluetoothconnection.listener;

import android.bluetooth.BluetoothSocket;

public interface SocketListener {
    void onSocketListener(BluetoothSocket socket);
}
