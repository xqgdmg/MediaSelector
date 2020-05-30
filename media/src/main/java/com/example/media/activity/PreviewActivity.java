package com.example.media.activity;

import android.animation.AnimatorSet;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.media.MediaSelector;
import com.example.media.R;
import com.example.media.adapter.PreviewAdapter;
import com.example.media.bean.MediaSelectorFile;
import com.example.media.permission.PermissionActivity;
import com.example.media.resolver.Contast;
import com.example.media.utils.FileUtils;
import com.example.media.utils.ScreenUtils;
import com.example.media.weight.PreviewViewPager;
import com.example.media.weight.Toasts;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class PreviewActivity extends PermissionActivity {

    private PreviewViewPager mVpPreview;
    private TextView tv_back;
    private TextView tv_finish;
    private List<MediaSelectorFile> mMediaFileData;
    private PreviewAdapter mPreviewAdapter;
    private boolean isShowTitleView = true;
    private int mPreviewPosition;
    private View mLlBottom;
    private List<MediaSelectorFile> mCheckMediaData;
    private AnimatorSet mAnimatorSet;
    private MediaSelector.MediaOptions mOptions;

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

        mVpPreview = findViewById(R.id.vp_preview);
        ViewGroup.LayoutParams layoutParams = mVpPreview.getLayoutParams();
        layoutParams.width = ScreenUtils.screenWidth(this);
        layoutParams.height = ScreenUtils.screenHeight(this);
        mVpPreview.setLayoutParams(layoutParams);
        tv_finish = findViewById(R.id.tv_finish);
        tv_back = findViewById(R.id.tv_back);
        mLlBottom = findViewById(R.id.ll_bottom);
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
        setTitleViewSureText();
        mPreviewAdapter = new PreviewAdapter(mMediaFileData);
        mVpPreview.setAdapter(mPreviewAdapter);
        mVpPreview.setCurrentItem(mPreviewPosition, true);
        mVpPreview.setPageTransformer(true, new PreviewAdapter.PreviewPageTransformer());

        initAdapterEvent();
    }

    private void setTitleViewSureText() {
        if (mCheckMediaData.size() > 0) {
            tv_finish.setText(getString(R.string.complete_count, String.valueOf(mCheckMediaData.size()), String.valueOf(mOptions.maxChooseMedia)));
        } else {
            tv_finish.setText(R.string.sure);
        }
    }

    private void initAdapterEvent() {
        mPreviewAdapter.setOnPreviewViewClickListener(new PreviewAdapter.OnPreviewViewClickListener() {
            @Override
            public void onPreviewView(View view) {
                if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
                    mAnimatorSet.end();
                }
                isShowTitleView = !isShowTitleView;
            }
        });
        mPreviewAdapter.setOnPreviewVideoClickListener(new PreviewAdapter.OnPreviewVideoClickListener() {
            @Override
            public void onClickVideo(View view, int position) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(mMediaFileData.get(position).filePath), "video/*");
                startActivityForResult(intent, Contast.CODE_REQUEST_PRIVIEW_VIDEO);
            }
        });
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
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAnimatorSet != null && mAnimatorSet.isRunning() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAnimatorSet.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAnimatorSet != null) {
            mAnimatorSet.end();
        }
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 0:
                if (requestCode == Contast.CODE_REQUEST_PRIVIEW_VIDEO && mPreviewAdapter.mCbPlay != null) {
                    mPreviewAdapter.mCbPlay.setChecked(false);
                    mPreviewAdapter.notifyDataSetChanged();
                }
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
