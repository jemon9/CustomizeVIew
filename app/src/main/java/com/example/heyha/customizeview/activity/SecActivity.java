package com.example.heyha.customizeview.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.example.heyha.customizeview.R;

public class SecActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec);
    }

    public void toCustomizeListView(View view){
        Intent intent = new Intent(this,ThirdActivity.class);
        startActivity(intent);
    }
}
