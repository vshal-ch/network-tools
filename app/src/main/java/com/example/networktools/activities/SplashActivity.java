package com.example.networktools.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

//todo
//change readme
//learn reverse engineering and learn how to get network devices macs
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        startActivity(new Intent(this, ScanActivity.class));
    //        setContentView(R.layout.splash_screen);
    //
    //        Intent I = new Intent(this, ScanActivity.class);
    //        new Handler().postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                startActivity(I);
    //            }
    //        }, 1000);
    }
}
