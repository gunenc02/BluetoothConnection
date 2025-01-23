package com.example.bluetoothconnection.listener;
import com.example.bluetoothconnection.utilities.BluetoothClientThread;

public interface SocketStateListener {
    void onServerListener();
    void onClientListener();
    void onFailClientListener(BluetoothClientThread thread);
}
