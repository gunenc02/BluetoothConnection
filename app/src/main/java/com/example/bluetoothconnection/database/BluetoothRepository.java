package com.example.bluetoothconnection.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.bluetoothconnection.database.dao.MessageDao;
import com.example.bluetoothconnection.database.entity.Message;

@Database(entities = {Message.class}, version = 1)
public abstract class BluetoothRepository extends RoomDatabase {
    public abstract MessageDao messageDao();
}
