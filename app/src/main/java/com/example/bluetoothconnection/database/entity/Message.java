package com.example.bluetoothconnection.database.entity;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

@Entity(tableName = "message")
public class Message {
    @PrimaryKey
    @ColumnInfo(name = "chatName")
    private String chatName;

    @ColumnInfo(name = "isReceived")
    private Boolean isReceived;

    @ColumnInfo(name="time")
    private Timestamp time;

    @ColumnInfo(name="message")
    private String message;


}
