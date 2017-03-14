package com.example.heyha.customizeview.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.example.heyha.customizeview.R;

import java.util.ArrayList;
import java.util.List;

import imageloader.zzf.com.viewlib.ImageLoopFrameLayout;

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
            Toast.makeText(getApplicationContext(),"pos:" + index,Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewGroup = (ImageLoopFrameLayout) findViewById(R.id.image_group);
        viewGroup.setImageLoopFrameLayoutClickListener(listener);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;

        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < ids.length; i++){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),ids[i]);
            bitmaps.add(bitmap);
        }
        viewGroup.addBitmaps(bitmaps);

//        for (int i = 0; i < ids.length; i++){
//            ImageView imageView = new ImageView(this);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
//            imageView.setImageResource(ids[i]);
//            viewGroup.addView(imageView);
//        }
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
//        startActivity(intent);
    }
}
