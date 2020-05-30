package com.hu.xiaobai.photoselector;

import android.content.Intent;
import androidx.annotation.NonNull;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.media.MediaSelector;
import com.example.media.OnRecyclerItemClickListener;
import com.example.media.bean.MediaSelectorFile;
import com.example.media.resolver.Contast;
import com.example.media.utils.GlideUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {


//    private List<MediaSelectorFile> mData;
    private ImageView iv_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();

    }


    private void initData() {
    }

    private void initView() {
        iv_add = findViewById(R.id.iv_add);

    }

    private void initEvent() {
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaSelector.MediaOptions mediaOptions = new MediaSelector.MediaOptions();
                mediaOptions.isShowCamera = true;
                mediaOptions.isShowVideo = true;
                mediaOptions.isCompress = true;
                mediaOptions.maxChooseMedia = 1;
                mediaOptions.isCrop = true;
                MediaSelector.with(MainActivity.this).setMediaOptions(mediaOptions).openMediaActivity();
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

        if (resultCode == Contast.CODE_RESULT_MEDIA && requestCode == Contast.CODE_REQUEST_MEDIA) {
            List<MediaSelectorFile> mediaList = MediaSelector.resultMediaFile(data);
            if (mediaList != null && mediaList.size() > 0) {
                GlideUtils.loadImage(MainActivity.this,mediaList.get(0).filePath,iv_add);

            }
        }
    }

}
