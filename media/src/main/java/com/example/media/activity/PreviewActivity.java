package com.example.media.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.media.MediaSelector;
import com.example.media.OnRecyclerItemClickListener;
import com.example.media.R;
import com.example.media.adapter.MediaCheckAdapter;
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

public class PreviewActivity extends PermissionActivity {

    private PreviewViewPager mVpPreview;
    private TextView tv_select;
    private TextView tv_back_bottom;
    private TextView tv_back;
    private TextView tv_finish;
    private List<MediaSelectorFile> mMediaFileData;
    private PreviewAdapter mPreviewAdapter;
    private boolean isShowTitleView = true;
    private int mPreviewPosition;
    private RecyclerView mRvCheckMedia;
    private View mLlBottom;
    private List<MediaSelectorFile> mCheckMediaData;
    private MediaCheckAdapter mCheckAdapter;
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
        mRvCheckMedia = findViewById(R.id.rv_check_media);
        mRvCheckMedia.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        tv_back_bottom = findViewById(R.id.tv_back_bottom);
        tv_select = findViewById(R.id.tv_select);
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
        tv_back.setText(getString(R.string.count_sum_count, String.valueOf(mPreviewPosition + 1), String.valueOf(mMediaFileData.size())));
        setTitleViewSureText();
        tv_select.setCompoundDrawablesWithIntrinsicBounds(mMediaFileData.get(mPreviewPosition).isCheck ? R.mipmap.icon_preview_check : R.mipmap.icon_preview_uncheck, 0, 0, 0);
        mPreviewAdapter = new PreviewAdapter(mMediaFileData);
        mVpPreview.setAdapter(mPreviewAdapter);
        mVpPreview.setCurrentItem(mPreviewPosition, true);
        mVpPreview.setPageTransformer(true, new PreviewAdapter.PreviewPageTransformer());

        mCheckAdapter = new MediaCheckAdapter(this, mCheckMediaData);
        mRvCheckMedia.setAdapter(mCheckAdapter);
        mCheckAdapter.notifyCheckData(mMediaFileData.get(mPreviewPosition));
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
        mVpPreview.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mPreviewPosition = i;
                tv_back.setText(getString(R.string.count_sum_count, String.valueOf(i + 1), String.valueOf(mMediaFileData.size())));
                tv_select.setCompoundDrawablesWithIntrinsicBounds(mMediaFileData.get(i).isCheck ? R.mipmap.icon_preview_check : R.mipmap.icon_preview_uncheck, 0, 0, 0);
                mCheckAdapter.notifyCheckData(mMediaFileData.get(mPreviewPosition));
                if (mCheckMediaData.contains(mMediaFileData.get(mPreviewPosition))) {
                    mRvCheckMedia.scrollToPosition(mCheckMediaData.indexOf(mMediaFileData.get(mPreviewPosition)));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
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
        mCheckAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void itemClick(@NonNull View view, int position) {
                if (mMediaFileData.contains(mCheckMediaData.get(position))) {
                    mVpPreview.setCurrentItem(mMediaFileData.indexOf(mCheckMediaData.get(position)), true);
                    mCheckAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    protected void initEvent() {

        tv_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCheckMediaData.size() < mOptions.maxChooseMedia || (mCheckMediaData.size() == mOptions.maxChooseMedia && mMediaFileData.get(mPreviewPosition).isCheck)) {
                    mMediaFileData.get(mPreviewPosition).isCheck = !mMediaFileData.get(mPreviewPosition).isCheck;
                    tv_select.setCompoundDrawablesWithIntrinsicBounds(mMediaFileData.get(mPreviewPosition).isCheck ? R.mipmap.icon_preview_check : R.mipmap.icon_preview_uncheck, 0, 0, 0);
                    EventBus.getDefault().post(mMediaFileData.get(mPreviewPosition));
                    if (mCheckAdapter != null) {
                        if (mMediaFileData.get(mPreviewPosition).isCheck) {
                            mCheckAdapter.addItemNotifyData(mMediaFileData.get(mPreviewPosition));
                            mRvCheckMedia.scrollToPosition(mCheckMediaData.indexOf(mMediaFileData.get(mPreviewPosition)));

                        } else {
                            if (mCheckMediaData.contains(mMediaFileData.get(mPreviewPosition))) {
                                mCheckAdapter.removeItemNotifyData(mCheckMediaData.indexOf(mMediaFileData.get(mPreviewPosition)));
                                mRvCheckMedia.scrollToPosition(mCheckMediaData.size() - 1);
                            }
                        }
                    }
                    //设置完成的数量
                    setTitleViewSureText();
                } else {
                    Toasts.with().showToast(PreviewActivity.this, R.string.max_choose_media, String.valueOf(mOptions.maxChooseMedia));
                }
            }
        });
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
}
