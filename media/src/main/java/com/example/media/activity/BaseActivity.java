package com.example.media.activity;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.WindowManager;

import com.example.media.R;
import com.example.media.bean.MediaSelectorFile;
import com.example.media.permission.PermissionActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import utils.bean.ImageConfig;
import utils.task.CompressImageTask;

public abstract class BaseActivity extends PermissionActivity {

    protected SystemBarTintManager mSystemBarTintManager;
    private @ColorRes
    int mThemeColor = R.color.colorTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initPermission();

    }

    protected void initPermission() {
        initView();
        initData();
        initEvent();
    }

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initEvent();

    protected abstract int getLayoutId();


    protected void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    protected void unRegisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    protected void compressImage(List<MediaSelectorFile> mMediaFileData, CompressImageTask.OnImagesResult onImagesResult) {
        final List<ImageConfig> configData = new ArrayList<>();
        for (int i = 0; i < mMediaFileData.size(); i++) {
            configData.add(MediaSelectorFile.thisToDefaultImageConfig(mMediaFileData.get(i)));
        }

        CompressImageTask.get().compressImages(this,configData, onImagesResult);
    }

}
