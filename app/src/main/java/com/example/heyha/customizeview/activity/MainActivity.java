package com.example.heyha.customizeview.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.example.heyha.customizeview.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toCustomizeView(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.customize_view:
                intent.setClass(this,SecActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }
}
