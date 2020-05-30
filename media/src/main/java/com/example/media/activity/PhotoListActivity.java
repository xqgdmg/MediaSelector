package com.example.media.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.media.MediaSelector;
import com.example.media.OnRecyclerItemClickListener;
import com.example.media.R;
import com.example.media.adapter.PhotoListAdapter;
import com.example.media.bean.MediaSelectorFile;
import com.example.media.bean.SelectorFolderPhoto;
import com.example.media.permission.PermissionActivity;
import com.example.media.permission.imp.OnPermissionsResult;
import com.example.media.resolver.Contast;
import com.example.media.resolver.ILoadMediaResult;
import com.example.media.resolver.MediaQueryHelper;
import com.example.media.utils.FileUtils;
import com.example.media.weight.DialogHelper;
import com.example.media.weight.FolderWindow;
import com.example.media.weight.Toasts;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoListActivity extends PermissionActivity {

    private RecyclerView mRecyclerView;
    private PhotoListAdapter mPhotoListAdapter;
    private List<MediaSelectorFile> mAdapterList;
    private List<SelectorFolderPhoto> mMediaFolderData;
    private FolderWindow mFolderWindow;
    private List<MediaSelectorFile> mSelectPhotoList;//already choose
    private MediaSelector.MediaOptions mOptions;
    private File mCameraFile;
    private AlertDialog mCameraPermissionDialog;
    private TextView tv_back;
    private TextView tv_all;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        requestExternalStoragePermission();
    }

    private void init() {
        initView();
        initData();
        initEvent();
    }


    private void requestExternalStoragePermission() {
        requestPermission(new OnPermissionsResult() {
            @Override
            public void onAllow(List<String> list) {
                init();
            }

            @Override
            public void onNoAllow(List<String> list) {
                AlertDialog dialog = DialogHelper.with().createDialog(PhotoListActivity.this, getString(R.string.hint), getString(R.string.what_permission_is_must, getString(R.string.memory_card)),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestExternalStoragePermission();
                            }
                        });
                dialog.show();
            }

            @Override
            public void onForbid(List<String> list) {
                Toast.makeText(PhotoListActivity.this,"permission deny",Toast.LENGTH_SHORT).show();
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void showNoCameraAllowDialog(Context context, String title, String message) {
        if (mCameraPermissionDialog == null) {
            mCameraPermissionDialog = DialogHelper.with().createDialog(context, title, message, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mCameraPermissionDialog.dismiss();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openCamera();
                }
            });
        }
        if (!mCameraPermissionDialog.isShowing()) {
            mCameraPermissionDialog.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Glide.with(this).onStart();
    }

    @Override
    protected void onStop() {

        super.onStop();

        Glide.with(this).onStop();
    }

    @Override
    protected void onDestroy() {
        unRegisterEventBus();
        super.onDestroy();
    }

    protected void initView() {
        registerEventBus();
        tv_back = findViewById(R.id.tv_back);
        tv_all = findViewById(R.id.tv_all);
        mRecyclerView = findViewById(R.id.ry_data);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
    }

    protected void initData() {
        initIntent();
        MediaQueryHelper mediaQueryHelper = new MediaQueryHelper(this);
        mSelectPhotoList = new ArrayList<>();

        if (mPhotoListAdapter == null) {

            mPhotoListAdapter = new PhotoListAdapter(this, mAdapterList, mOptions);
            mRecyclerView.setAdapter(mPhotoListAdapter);
        }
        mediaQueryHelper.loadMedia(mOptions.isShowCamera, mOptions.isShowVideo, new ILoadMediaResult() {
            @Override
            public void mediaResult(List<SelectorFolderPhoto> data) {
                if (data != null && data.size() > 0) {
                    mAdapterList.addAll(data.get(0).fileData);

                    if (mMediaFolderData == null) {// TODO: 2020/5/31
                        mMediaFolderData = data;
                    } else {
                        mMediaFolderData.addAll(data);
                    }

                    mPhotoListAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    private void initIntent() {
        mAdapterList = new ArrayList<>();
        Intent intent = getIntent();
        mOptions = intent.getParcelableExtra(Contast.KEY_OPEN_MEDIA);
        if (mOptions == null) {
            mOptions = MediaSelector.getDefaultOptions();
        } else {
            if (mOptions.maxChooseMedia <= 0) {
                mOptions.maxChooseMedia = 1;
            }
        }

    }

    private void resultMediaData() {
        if (mSelectPhotoList.size() > 0) {
            if (mOptions.isCompress && !mOptions.isShowVideo) {

            } else {
                resultMediaIntent();
            }

        }
    }

    private void resultMediaIntent() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Contast.KEY_REQUEST_MEDIA_DATA, (ArrayList<? extends Parcelable>) mSelectPhotoList);
        setResult(Contast.CODE_RESULT_MEDIA, intent);
        finish();
    }

    protected void initEvent() {
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMediaFolderWindows(view);
            }
        });

        mPhotoListAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void itemClick(@NonNull View view, int position) {
                if (mAdapterList.get(position).isShowCamera) {
                    openCamera();
                } else {
                    if (mOptions.isCrop && mOptions.maxChooseMedia == 1 && mOptions.isShowVideo && mAdapterList.get(position).isVideo) {
                        Toasts.with().showToast(PhotoListActivity.this, R.string.video_not_crop, Toast.LENGTH_SHORT);
                    } else {
                        toPreviewActivity(position, mAdapterList, mSelectPhotoList);
                    }
                }
            }
        });


        mPhotoListAdapter.setOnCheckMediaListener(new PhotoListAdapter.OnCheckMediaListener() {
            @Override
            public void onChecked(boolean isCheck, int position) {
                if (isCheck) {
                    mAdapterList.get(position).isCheck = false;
                    mSelectPhotoList.remove(mAdapterList.get(position));
                } else {
                    if (mSelectPhotoList.size() < mOptions.maxChooseMedia) {
                        mAdapterList.get(position).isCheck = true;
                        mSelectPhotoList.add(mAdapterList.get(position));
                    } else {
                        Toasts.with().showToast(PhotoListActivity.this, getString(R.string.max_choose_media, String.valueOf(mOptions.maxChooseMedia)));
                    }
                }
                mPhotoListAdapter.notifyItemChanged(position);
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
                    Glide.with(PhotoListActivity.this).resumeRequests();
                } else {
                    Glide.with(PhotoListActivity.this).pauseRequests();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        requestPermission(new OnPermissionsResult() {
            @Override
            public void onAllow(List<String> list) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
              //  cameraIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    mCameraFile = FileUtils.resultImageFile(PhotoListActivity.this);
                    Uri cameraUri = FileUtils.fileToUri(PhotoListActivity.this, mCameraFile, cameraIntent);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                    startActivityForResult(cameraIntent, Contast.REQUEST_CAMERA_CODE);
                }
            }

            @Override
            public void onNoAllow(List<String> list) {
                showNoCameraAllowDialog(PhotoListActivity.this, getString(R.string.hint), getString(R.string.what_permission_is_must, getString(R.string.camera)));
            }

            @Override
            public void onForbid(List<String> list) {

                Toast.makeText(PhotoListActivity.this,"permission deny",Toast.LENGTH_SHORT).show();

            }
        }, Manifest.permission.CAMERA);
    }

    private void toPreviewActivity(int position, @NonNull List<MediaSelectorFile> data, @NonNull List<MediaSelectorFile> checkData) {
        Intent intent = new Intent(PhotoListActivity.this, PreviewActivity.class);
        intent.putParcelableArrayListExtra(Contast.KEY_PREVIEW_DATA_MEDIA, (ArrayList<? extends Parcelable>) data);
        intent.putParcelableArrayListExtra(Contast.KEY_PREVIEW_CHECK_MEDIA, (ArrayList<? extends Parcelable>) checkData);
        intent.putExtra(Contast.KEY_OPEN_MEDIA, mOptions);
        intent.putExtra(Contast.KEY_PREVIEW_POSITION, position);
        startActivity(intent);
    }


    private void showMediaFolderWindows(View view) {

        if (mFolderWindow == null) {
            mFolderWindow = new FolderWindow(this, mMediaFolderData);
            mFolderWindow.setOnPopupItemClickListener(new FolderWindow.OnPopupItemClickListener() {
                @Override
                public void onItemClick(@NonNull View view, int position) {
                    clickCheckFolder(position);
                }
            });
            mFolderWindow.showWindows(view);
        } else if (mFolderWindow.getFolderWindow().isShowing()) {
            mFolderWindow.dismissWindows();
        } else {
            mFolderWindow.showWindows(view);
        }


    }

    private void clickCheckFolder(int position) {
        tv_all.setText(mMediaFolderData.get(position).folderName);
        mAdapterList.clear();
        mAdapterList.addAll(mMediaFolderData.get(position).fileData);
        mPhotoListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (mFolderWindow != null && mFolderWindow.getFolderWindow().isShowing()) {
            mFolderWindow.dismissWindows();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 预览图片选择发送事件
     *
     * @param mediaSelectorFile
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void previewMediaResult(@NonNull MediaSelectorFile mediaSelectorFile) {
        if (mediaSelectorFile.isCheck) {
            //首先先判断选择的媒体库
            if (!mSelectPhotoList.contains(mediaSelectorFile)) {
                mSelectPhotoList.add(mediaSelectorFile);
            }

        } else {
            if (mSelectPhotoList.contains(mediaSelectorFile)) {
                mSelectPhotoList.remove(mediaSelectorFile);
            }
        }
        for (int i = 0; i < mMediaFolderData.size(); i++) {
            if (mMediaFolderData.get(i).fileData.contains(mediaSelectorFile)) {
                mMediaFolderData.get(i).fileData.get(mMediaFolderData.get(i).fileData.indexOf(mediaSelectorFile)).isCheck = mediaSelectorFile.isCheck;
            }
        }
        mPhotoListAdapter.notifyDataSetChanged();
    }

    /**
     * 预览图片返回
     *
     * @param checkMediaData
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void resultCheckMediaData(@NonNull List<MediaSelectorFile> checkMediaData) {
        if (checkMediaData.size() > 0) {
            mSelectPhotoList.clear();
            mSelectPhotoList.addAll(checkMediaData);
            resultMediaIntent();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                if (requestCode == Contast.REQUEST_CAMERA_CODE) {
                    if (FileUtils.existsFile(mCameraFile.getAbsolutePath())) {
                        FileUtils.scanImage(this, mCameraFile);
                        MediaSelectorFile mediaSelectorFile = MediaSelectorFile.checkFileToThis(mCameraFile);
                        if (mediaSelectorFile.hasData()) {
                            mSelectPhotoList.add(mediaSelectorFile);
                        }
                        resultMediaData();
                    }

                }
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
