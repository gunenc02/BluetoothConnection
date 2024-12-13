package com.example.bluetoothconnection.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bluetoothconnection.R;

public class BluetoothChatActivity extends AppCompatActivity {

    private Button sendButton, backButton;
    private ListView listView;
    private EditText editText;
    private ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = findViewById(R.id.messages);
        sendButton = findViewById(R.id.button_send);
        backButton = findViewById(R.id.button_back);
        editText = findViewById(R.id.message_input);

        arrayAdapter = new ArrayAdapter<>(this, 0);


    }
}
