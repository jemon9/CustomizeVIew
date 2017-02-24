package com.example.heyha.customizeview.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.example.heyha.customizeview.R;
import com.example.heyha.customizeview.ui.ScrollZoomListView;

public class ThirdActivity extends AppCompatActivity {

    private ScrollZoomListView listView;
    private ImageView mHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        listView = (ScrollZoomListView) findViewById(R.id.scroll_zoom_listview);
        View header = View.inflate(this, R.layout.listview_header,null);
        ImageView headerImage = (ImageView) header.findViewById(R.id.iv_header);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                new String[]{
                        "星期一  约见马云",
                        "星期二  和奥巴马见面",
                        "星期三  和习近平喝茶",
                        "星期四  和姚明去打球",
                        "星期五  访问韩国",
                        "星期六  出席人大代表会",
                        "星期日  回家睡觉",
                        "星期一  约见马云",
                        "星期二  和奥巴马见面",
                        "星期三  和习近平喝茶",
                        "星期四  和姚明去打球",
                        "星期五  访问韩国",
                        "星期六  出席人大代表会",
                        "星期日  回家睡觉"
                });

        listView.setZoomImage(headerImage);
        listView.addHeaderView(header);
        listView.setAdapter(adapter);

    }
}
