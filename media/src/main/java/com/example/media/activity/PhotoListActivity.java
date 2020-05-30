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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.media.PhotoSelector;
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
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoListActivity extends PermissionActivity {

    private RecyclerView mRecyclerView;
    private PhotoListAdapter mPhotoListAdapter;
    private List<MediaSelectorFile> mAdapterList;
    private List<SelectorFolderPhoto> mPhotoFolderData;
    private FolderWindow mFolderWindow;
    private List<MediaSelectorFile> mSelectPhotoList;//already choose
    private PhotoSelector.MediaOptions mOptions;
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

    protected void initView() {
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
                    mAdapterList.addAll(data.get(0).fileData);//all photo
                    mPhotoListAdapter.notifyDataSetChanged();

                    if (mPhotoFolderData == null) {// TODO: 2020/5/31
                        mPhotoFolderData = data;
                    } else {
                        mPhotoFolderData.addAll(data);
                    }

                }

            }
        });
    }

    private void initIntent() {
        mAdapterList = new ArrayList<>();
        Intent intent = getIntent();
        mOptions = intent.getParcelableExtra(Contast.KEY_OPEN_MEDIA);
        if (mOptions == null) {
            mOptions = PhotoSelector.getDefaultOptions();
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
                resultIntent();
            }

        }
    }

    private void resultIntent() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Contast.KEY_REQUEST_PHOTO_DATA, (ArrayList<? extends Parcelable>) mSelectPhotoList);
        setResult(Contast.CODE_RESULT_PHOTO_LIST, intent);
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
                showPhotoFolderWindows(view);
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
                        toCrop(position, mAdapterList);
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

    private void toCrop(int position, @NonNull List<MediaSelectorFile> data) {

        if (mSelectPhotoList.size() <= 0) {
            data.get(position).isCheck = true;
            mSelectPhotoList.add(data.get(position));
        }
        sureData();
    }

    // 跳转裁剪
    private void sureData() {
        if (mOptions.isCrop && mOptions.maxChooseMedia == 1) {
            if (!mSelectPhotoList.get(0).isVideo) {
                UCrop.Options options = new UCrop.Options();
                options.setCompressionQuality(100);
                options.setToolbarColor(ContextCompat.getColor(this, mOptions.themeColor));
                options.setStatusBarColor(ContextCompat.getColor(this, mOptions.themeColor));
                options.setLogoColor(ContextCompat.getColor(this, mOptions.themeColor));
                options.setActiveWidgetColor(ContextCompat.getColor(this, mOptions.themeColor));
                UCrop.of(Uri.fromFile(new File(mSelectPhotoList.get(0).filePath)), Uri.fromFile(FileUtils.resultImageFile(this, "Crop")))
                        .withAspectRatio(mOptions.scaleX, mOptions.scaleY)
                        .withMaxResultSize(mOptions.cropWidth, mOptions.cropHeight)
                        .withOptions(options)
                        .start(this);
            } else {
                Toasts.with().showToast(this, R.string.video_not_crop);
            }
        } else {
            EventBus.getDefault().post(mSelectPhotoList);
            finish();
        }
    }

    private void showPhotoFolderWindows(View view) {

        if (mFolderWindow == null) {
            mFolderWindow = new FolderWindow(this, mPhotoFolderData);
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
        tv_all.setText(mPhotoFolderData.get(position).folderName);
        mAdapterList.clear();
        mAdapterList.addAll(mPhotoFolderData.get(position).fileData);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Activity.RESULT_OK:// 请求权限
                if (requestCode == Contast.REQUEST_CAMERA_CODE) {
                    if (FileUtils.existsFile(mCameraFile.getAbsolutePath())) {
                        FileUtils.scanImage(this, mCameraFile);
                        MediaSelectorFile mediaSelectorFile = MediaSelectorFile.checkFileToThis(mCameraFile);
                        if (mediaSelectorFile.hasData()) {
                            mSelectPhotoList.add(mediaSelectorFile);
                        }
                        resultMediaData();
                    }

                }else if (requestCode == UCrop.REQUEST_CROP) {//crop ok
                    if (data == null) {
                        return;
                    }
                    final Uri resultUri = UCrop.getOutput(data);
                    if (resultUri != null && resultUri.getPath() != null) {
                        mSelectPhotoList.clear();
                        File file = new File(resultUri.getPath());
                        if (FileUtils.existsFile(file.getAbsolutePath())) {
                            mSelectPhotoList.add(MediaSelectorFile.checkFileToThis(file));
                            resultIntent();
                            finish();
                        } else {
                            Toasts.with().showToast(this, R.string.file_not_exit, Toast.LENGTH_SHORT);
                        }
                    }

                }
                break;
            case UCrop.RESULT_ERROR://crop error
                if (requestCode == UCrop.REQUEST_CROP) {
                    Toasts.with().showToast(this, R.string.crop_image_fail);
                }
                break;
            default:
                break;
        }
    }


}
