package com.example.bluetoothconnection.activities;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bluetoothconnection.R;
import com.example.bluetoothconnection.service.BluetoothService;
import com.example.bluetoothconnection.utilities.BluetoothClientThread;
import com.example.bluetoothconnection.utilities.BluetoothServerThread;

public class BluetoothChatActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private Button sendButton, backButton;
    private ListView listView;
    private EditText editText;
    private ArrayAdapter<ListView> arrayAdapter;
    private BluetoothService service;

    public static final String TAG = "BluetoothChatActivity";
    BluetoothService.ConnectedThread thread;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0: // MESSAGE_READ
                    byte[] readBuffer = (byte[]) msg.obj;
                    int bytesRead = msg.arg1;
                    String receivedMessage = new String(readBuffer, 0, bytesRead);
                    adapter.add(receivedMessage);
                    break;
                case 1: // MESSAGE_WRITE

                case 2: // MESSAGE_TOAST
                    // Show toast
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_screen);
        listView = findViewById(R.id.messages);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        sendButton = findViewById(R.id.button_send);
        backButton = findViewById(R.id.button_back);
        editText = findViewById(R.id.edit_text_message);
        arrayAdapter = new ArrayAdapter<>(this, 0);
        service = new BluetoothService(mHandler);
        thread = service.startConnectedThread(getSocket());
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private BluetoothSocket getSocket(){
        if(BluetoothClientThread.isExists()){
            return BluetoothClientThread.getSocket();
        }
        return BluetoothServerThread.getSocket();
    }

    private void sendMessage(){
        String message = editText.getText().toString();
        thread.write(message);
    }
}
