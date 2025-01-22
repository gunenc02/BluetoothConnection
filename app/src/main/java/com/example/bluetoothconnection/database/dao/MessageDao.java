package com.example.bluetoothconnection.database.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bluetoothconnection.database.entity.Message;

import java.util.List;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM message WHERE chatname = :chatName ORDER BY time ASC")
    @Nullable
    List<Message> getChat(String chatName);

    @Insert
    void write(Message message);
}
