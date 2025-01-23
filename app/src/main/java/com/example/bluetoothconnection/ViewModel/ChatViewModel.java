package com.example.bluetoothconnection.ViewModel;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bluetoothconnection.activities.BluetoothChatActivity;
import com.example.bluetoothconnection.service.BluetoothService;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {
    public BluetoothService service;
    public MutableLiveData<List<String>> messageList = new MutableLiveData<>(new ArrayList<>());
    public Handler handler;
    public ArrayAdapter<String> adapter;
    private BluetoothChatActivity act;

    public void initHandlerAndService(){
        if(handler == null){
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    List<String> currentMessages = messageList.getValue();
                    if(currentMessages == null){
                        currentMessages = new ArrayList<>();
                    }
                    byte[] readBuffer = (byte[]) msg.obj;
                    int bytesRead = msg.arg1;
                    String message = new String(readBuffer, 0, bytesRead);
                    switch (msg.what) {
                        case 0: // MESSAGE_READ
                            message = "Received: " + message;
                            currentMessages.add(message);
                            break;
                        case 1: // MESSAGE_WRITE
                            message = "Sent: "+ message;
                            currentMessages.add(message);
                            break;
                    }
                    messageList.setValue(currentMessages);
                    adapter.notifyDataSetChanged();
                    act.warn(message);
                }
            };
        }

        if (service == null){
            service = new BluetoothService(handler);
        }
    }

    public BluetoothService getBluetoothService(){
        return service;
    }

    public MutableLiveData<List<String>> getMessageList(){
        return messageList;
    }

    public void setAdapter(ArrayAdapter<String> adapter) {
        this.adapter = adapter;
    }

    public void setListener(BluetoothChatActivity bluetoothChatActivity) {
        this.act = bluetoothChatActivity;
    }
}
