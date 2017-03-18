package com.example.android.forisio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }


    public void registerSensor(View v){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void interact(View v){
        Intent intent = new Intent(this,Communication.class);
        startActivity(intent);
    }
}
