package com.example.bluetoothconnection.listener;

import android.bluetooth.BluetoothSocket;

import com.example.bluetoothconnection.utilities.BluetoothClientThread;
import com.example.bluetoothconnection.utilities.BluetoothServerThread;

public interface SocketListener {
    void onServerListener(BluetoothSocket socket);
    void onClientListener(BluetoothSocket socket);
    void onFailClientListener(BluetoothClientThread thread);
}
