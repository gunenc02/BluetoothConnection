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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.bluetoothconnection.R;
import com.example.bluetoothconnection.ViewModel.ChatViewModel;
import com.example.bluetoothconnection.listener.SocketClosingListener;
import com.example.bluetoothconnection.service.BluetoothService;
import com.example.bluetoothconnection.utilities.BluetoothClientThread;
import com.example.bluetoothconnection.utilities.BluetoothServerThread;

import java.util.List;

public class BluetoothChatActivity extends AppCompatActivity implements SocketClosingListener {

    private ArrayAdapter<String> adapter;
    private Button sendButton, backButton;
    private ListView listView;
    private TextView textView;
    private EditText editText;
    private BluetoothSocket socket;
    public static final String TAG = "BluetoothChatActivity";
    private ChatViewModel model;
    private MutableLiveData<List<String>> mutableMessageList;
    BluetoothService.ConnectedThread thread;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_screen);

        textView = findViewById(R.id.device_name);
        listView = findViewById(R.id.messages);
        sendButton = findViewById(R.id.button_send);
        backButton = findViewById(R.id.button_back);
        editText = findViewById(R.id.edit_text_message);

        model = new ViewModelProvider(this).get(ChatViewModel.class);
        model.initHandlerAndService();
        mutableMessageList = model.getMessageList();
        Log.i(TAG, mutableMessageList.getValue().toString());
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        model.setListener(this);
        socket = getSocket();
        textView.setText(socket.getRemoteDevice().getName());

        thread = model.getBluetoothService().startConnectedThread(socket, this);
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> back());
        model.warn();
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
        editText.setText("");
    }

    private void back(){
       try{
           socket.close();
       } catch (Exception e) {
           Log.e(TAG, "cannot close the socket");
       }
    }

    @Override
    public void onSocketCloseListener() {
        this.runOnUiThread(() -> Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show());
        finish();
    }

    public void warn(MutableLiveData<List<String>> messageList) {
        List<String> list = messageList.getValue();
        adapter.clear();
        adapter.addAll(list);
        adapter.notifyDataSetChanged();
    }
}
