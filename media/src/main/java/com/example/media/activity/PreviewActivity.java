package com.example.media.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.media.PhotoSelector;
import com.example.media.R;
import com.example.media.bean.MediaSelectorFile;
import com.example.media.permission.PermissionActivity;
import com.example.media.resolver.Contast;
import com.example.media.utils.FileUtils;
import com.example.media.weight.Toasts;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class PreviewActivity extends PermissionActivity {

    private TextView tv_finish;
    private List<MediaSelectorFile> mMediaFileData;
    private int mPreviewPosition;
    private List<MediaSelectorFile> mCheckMediaData;
    private PhotoSelector.MediaOptions mOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        init();
    }

    private void init() {
        initView();
        initData();
        initEvent();
    }

    protected void initView() {
        tv_finish = findViewById(R.id.tv_finish);
    }

    protected void initData() {
        Intent intent = getIntent();
        mCheckMediaData = intent.getParcelableArrayListExtra(Contast.KEY_PREVIEW_CHECK_MEDIA);
        if (mCheckMediaData == null) {
            mCheckMediaData = new ArrayList<>();
        }
        mMediaFileData = intent.getParcelableArrayListExtra(Contast.KEY_PREVIEW_DATA_MEDIA);
        mPreviewPosition = intent.getIntExtra(Contast.KEY_PREVIEW_POSITION, 0);
        mOptions = intent.getParcelableExtra(Contast.KEY_OPEN_MEDIA);
        if (mMediaFileData == null || mMediaFileData.size() == 0) {
            Toasts.with().showToast(this, "没有预览媒体库文件");
            finish();
            return;
        }
        if (mMediaFileData.get(0).isShowCamera && mMediaFileData.get(0).filePath == null) {
            mMediaFileData.remove(0);
            mPreviewPosition--;
        }

        if (mCheckMediaData != null && mCheckMediaData.size() > 0) {
            for (int i = 0; i < mCheckMediaData.size(); i++) {
                if (!mMediaFileData.contains(mCheckMediaData.get(i))) {
                    mMediaFileData.add(mCheckMediaData.get(i));
                }
            }
        }
    }

    protected void initEvent() {

        tv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCheckMediaData.size() <= 0) {
                    mMediaFileData.get(mPreviewPosition).isCheck = true;
                    mCheckMediaData.add(mMediaFileData.get(mPreviewPosition));
                }
                sureData();
            }
        });
    }

    // 跳转裁剪
    private void sureData() {
        if (mOptions.isCrop && mOptions.maxChooseMedia == 1) {
            if (!mCheckMediaData.get(0).isVideo) {
                UCrop.Options options = new UCrop.Options();
                options.setCompressionQuality(100);
                options.setToolbarColor(ContextCompat.getColor(this, mOptions.themeColor));
                options.setStatusBarColor(ContextCompat.getColor(this, mOptions.themeColor));
                options.setLogoColor(ContextCompat.getColor(this, mOptions.themeColor));
                options.setActiveWidgetColor(ContextCompat.getColor(this, mOptions.themeColor));
                UCrop.of(Uri.fromFile(new File(mCheckMediaData.get(0).filePath)), Uri.fromFile(FileUtils.resultImageFile(this, "Crop")))
                        .withAspectRatio(mOptions.scaleX, mOptions.scaleY)
                        .withMaxResultSize(mOptions.cropWidth, mOptions.cropHeight)
                        .withOptions(options)
                        .start(this);
            } else {
                Toasts.with().showToast(this, R.string.video_not_crop);
            }
        } else {
            EventBus.getDefault().post(mCheckMediaData);
            finish();
        }
    }

    /*
     * 裁剪返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == UCrop.REQUEST_CROP) {
                    if (data == null) {
                        return;
                    }
                    final Uri resultUri = UCrop.getOutput(data);
                    if (resultUri != null && resultUri.getPath() != null) {
                        mCheckMediaData.clear();
                        File file = new File(resultUri.getPath());
                        if (FileUtils.existsFile(file.getAbsolutePath())) {
                            mCheckMediaData.add(MediaSelectorFile.checkFileToThis(file));
                            EventBus.getDefault().post(mCheckMediaData);
                            finish();
                        } else {
                            Toasts.with().showToast(this, R.string.file_not_exit, Toast.LENGTH_SHORT);
                        }
                    }

                }
                break;
            case UCrop.RESULT_ERROR:
                if (requestCode == UCrop.REQUEST_CROP) {
                    Toasts.with().showToast(this, R.string.crop_image_fail);
                }
                break;
            default:
                break;
        }
    }


}
