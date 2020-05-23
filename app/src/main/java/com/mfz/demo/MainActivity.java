package com.mfz.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author mz
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        finish();
    }
}
