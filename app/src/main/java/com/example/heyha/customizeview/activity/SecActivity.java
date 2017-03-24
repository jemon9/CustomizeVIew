package com.example.heyha.customizeview.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.heyha.customizeview.R;

public class SecActivity extends AppCompatActivity {
    Button mFloatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec);
        addwindow();
    }

    private void addwindow() {
        mFloatButton = new Button(this);
        mFloatButton.setText("悬浮按钮");
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0,
                PixelFormat.TRANSPARENT);
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams
                .FLAG_SHOW_WHEN_LOCKED;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        lp.x = 100;
        lp.y = 300;

        getWindowManager().addView(mFloatButton,lp);
    }

    public void toCustomizeListView(View view) {
        Intent intent = new Intent(this, ThirdActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        getWindowManager().removeViewImmediate(mFloatButton);
        super.onDestroy();
    }
}
