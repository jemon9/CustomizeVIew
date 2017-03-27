package com.example.heyha.customizeview.activity;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.heyha.customizeview.R;

import imageloader.zzf.com.viewlib.photoview.PhotoView;

public class PhotoViewActivity extends AppCompatActivity {

    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setZoomEnabled(true);

        Drawable bitmap = ContextCompat.getDrawable(this,R.drawable.wallpaper);
        photoView.setImageDrawable(bitmap);
    }
}
