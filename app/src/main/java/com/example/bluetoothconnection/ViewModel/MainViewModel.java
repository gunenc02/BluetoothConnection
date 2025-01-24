package com.example.bluetoothconnection.ViewModel;

import android.os.CountDownTimer;
import android.util.Log;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private final String TAG = "MainViewModel";
    public Boolean isDetectable;
    private CountDownTimer timer;

    public void init(){
        isDetectable = false;
    }

    public Boolean getIsDetectable(){
        return isDetectable;
    }

    public void setTrue(){

        if(isDetectable){
            timer.cancel();
        }

        isDetectable = true;
        CountDownTimer timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "Time is running out");
            }

            @Override
            public void onFinish() {
                isDetectable = false;
            }
        };
        timer.start();
    }

    public void setFalse() {
        isDetectable = false;
    }
}
