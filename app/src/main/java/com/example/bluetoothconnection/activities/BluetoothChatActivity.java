package com.example.bluetoothconnection.activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bluetoothconnection.R;
import com.example.bluetoothconnection.listener.SocketClosingListener;
import com.example.bluetoothconnection.service.BluetoothService;
import com.example.bluetoothconnection.utilities.BluetoothClientThread;
import com.example.bluetoothconnection.utilities.BluetoothServerThread;

import java.util.ArrayList;
import java.util.List;

public class BluetoothChatActivity extends AppCompatActivity implements SocketClosingListener {

    private ArrayAdapter<String> adapter;
    private Button sendButton, backButton;
    private ListView listView;
    private TextView textView;
    private EditText editText;
    private BluetoothService service;
    private BluetoothSocket socket;
    private List<String> messageList;
    public static final String TAG = "BluetoothChatActivity";
    BluetoothService.ConnectedThread thread;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            byte[] readBuffer = (byte[]) msg.obj;
            int bytesRead = msg.arg1;
            String message = new String(readBuffer, 0, bytesRead);
            switch (msg.what) {
                case 0: // MESSAGE_READ
                    message = "Received: " + message;
                    messageList.add(message);
                    adapter.notifyDataSetChanged();
                    break;
                case 1: // MESSAGE_WRITE
                    message = "Sent: "+ message;
                    messageList.add(message);
                    adapter.notifyDataSetChanged();
                    editText.setText("");
                    break;
                case 2: // MESSAGE_TOAST
                    // Show toast
                    break;
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_screen);
        textView = findViewById(R.id.device_name);
        listView = findViewById(R.id.messages);
        messageList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,messageList);
        listView.setAdapter(adapter);
        sendButton = findViewById(R.id.button_send);
        backButton = findViewById(R.id.button_back);
        editText = findViewById(R.id.edit_text_message);
        service = new BluetoothService(mHandler);
        socket = getSocket();
        textView.setText(socket.getRemoteDevice().getName());
        thread = service.startConnectedThread(socket, this);
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> back());
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

    private void back(){
       try{
           socket.close();
       } catch (Exception e) {
           Log.e(TAG, "cannot close the socket");
       }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("messageList", new ArrayList<>(messageList));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> savedMessages = savedInstanceState.getStringArrayList("messageList");
        if (savedMessages != null) {
            messageList.addAll(savedMessages);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSocketCloseListener() {
        this.runOnUiThread(() -> Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show());
        finish();
    }

    protected void onDestroy(){
        super.onDestroy();
        messageList.clear();
    }
}
