package com.example.media.crop;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;


import com.example.media.R;

import androidx.appcompat.app.AppCompatActivity;

public class ClipResultActivity extends AppCompatActivity {
    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_result);
        initView();
    }

    private void initView() {
        iv=findViewById(R.id.iv);
        iv.setImageBitmap(BitmapBean.bitmap);
        Log.i("jenson","====="+ BitmapBean.bitmap.getWidth());
        Log.i("jenson","====="+ BitmapBean.bitmap.getHeight());
    }
}
