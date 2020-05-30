package com.hu.xiaobai.photoselector;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;

import com.example.media.PhotoSelector;
import com.example.media.bean.MediaSelectorFile;
import com.example.media.resolver.Contast;
import com.example.media.utils.GlideUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();

    }

    private void initView() {
        iv_add = findViewById(R.id.iv_add);

    }

    private void initEvent() {
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoSelector.MediaOptions mediaOptions = new PhotoSelector.MediaOptions();
                mediaOptions.isShowCamera = true;
                mediaOptions.isShowVideo = true;
                mediaOptions.isCompress = true;
                mediaOptions.maxChooseMedia = 1;
                mediaOptions.isCrop = true;
                PhotoSelector.with(MainActivity.this).setMediaOptions(mediaOptions).openPhotoListActivity();
            }
        });
    }

    /**
     * 已选择图片结果回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Contast.CODE_RESULT_PHOTO_LIST && requestCode == Contast.CODE_REQUEST_PHOTO_LIST) {
            List<MediaSelectorFile> mediaList = PhotoSelector.resultMediaFile(data);
            if (mediaList != null && mediaList.size() > 0) {
                GlideUtils.loadImage(MainActivity.this,mediaList.get(0).filePath,iv_add);

            }
        }
    }

}
