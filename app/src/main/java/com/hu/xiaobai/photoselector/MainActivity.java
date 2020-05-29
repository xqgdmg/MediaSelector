package com.hu.xiaobai.photoselector;

import android.content.Intent;
import androidx.annotation.NonNull;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.media.MediaSelector;
import com.example.media.OnRecyclerItemClickListener;
import com.example.media.bean.MediaSelectorFile;
import com.example.media.resolver.Contast;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {


    private DataAdapter mDataAdapter;
    private RecyclerView mRyMedia;
    private List<MediaSelectorFile> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();

    }


    private void initData() {
        mData = new ArrayList<>();
        MediaSelectorFile mediaSelectorFile = new MediaSelectorFile();
        mData.add(mediaSelectorFile);
        mDataAdapter = new DataAdapter(this, mData);
        mRyMedia.setAdapter(mDataAdapter);
    }

    private void initView() {
        mRyMedia = findViewById(R.id.rv_media);
        mRyMedia.setLayoutManager(new GridLayoutManager(this, 3));

    }

    private void initEvent() {
        mDataAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void itemClick(@NonNull View view, int position) {
                if (!mData.get(position).isCheck) {
                    if (mData.size() > 1) {
                        ListIterator<MediaSelectorFile> iterator = mData.listIterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().isCheck) {
                                iterator.remove();
                            }
                        }
                        mDataAdapter.notifyDataSetChanged();
                    }
                    MediaSelector.MediaOptions mediaOptions = new MediaSelector.MediaOptions();
                    mediaOptions.isShowCamera = true;
                    mediaOptions.isShowVideo = true;
                    mediaOptions.isCompress = true;
                    mediaOptions.maxChooseMedia = 1;
                    mediaOptions.isCrop = true;
                    MediaSelector.with(MainActivity.this).setMediaOptions(mediaOptions).openMediaActivity();

                }
            }
        });
    }

    /**
     * 选择图片结果回调
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
                mData.addAll(0, mediaList);
                mDataAdapter.notifyDataSetChanged();
                for (int i = 0; i < mediaList.size(); i++) {
                    Log.w("onActivityResult----", mediaList.get(i).filePath + mediaList.get(i).folderPath);
                }

            }
        }
    }

}
