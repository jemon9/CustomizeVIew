package com.example.heyha.customizeview.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.heyha.customizeview.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import imageloader.zzf.com.viewlib.ImageLoopFrameLayout;

/**
 * 测试轮播视图功能
 */
public class MainActivity extends AppCompatActivity {

    private ImageLoopFrameLayout viewGroup;
    private int[] ids = new int[]{
            R.mipmap.ban1,
            R.mipmap.ban2,
            R.mipmap.ban3
    };

    private ImageLoopFrameLayout.ImageLoopFrameLayoutListener listener = new ImageLoopFrameLayout.ImageLoopFrameLayoutListener() {
        @Override
        public void clickImageIndex(int index) {
            Toast.makeText(getApplicationContext(), "pos:" + index, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewGroup = (ImageLoopFrameLayout) findViewById(R.id.image_group);
        viewGroup.setImageLoopFrameLayoutClickListener(listener);
        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ids[i]);
            bitmaps.add(bitmap);
        }
        viewGroup.addBitmaps(bitmaps);

        HashMap<String, Integer> hashMap = new HashMap<>();
        Map<Integer, String> map = new HashMap<>();
    }


    public void toCustomizeView(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.customize_view:
                intent.setClass(this, SecActivity.class);
                break;
            case R.id.photo_view:
                intent.setClass(this, PhotoViewActivity.class);
            default:
                break;
        }
        startActivity(intent);
    }
}
