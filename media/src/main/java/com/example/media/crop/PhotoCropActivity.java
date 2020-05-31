package com.example.media.crop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.media.R;


public class PhotoCropActivity extends AppCompatActivity {

    private CustomCropView customCropView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_crop);

        TextView tv_crop = findViewById(R.id.tv_crop);
        customCropView = findViewById(R.id.cropView);

        String path = getIntent().getStringExtra("path");
        customCropView.setBitmapForWidth(path, 1080);

        customCropView.setRadius(30);

        tv_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //目前在点击事件中设置，不能一开始getClipWidth
//                float redius = customCropView.getClipWidth() / 2;
//                customCropView.setRadius(redius);
                //裁剪
                BitmapBean.bitmap = customCropView.clip();
                Intent intent = new Intent(PhotoCropActivity.this, ClipResultActivity.class);
                startActivity(intent);
            }
        });
    }

}
