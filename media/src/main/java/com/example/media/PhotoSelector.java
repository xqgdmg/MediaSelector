package com.example.media;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.example.media.activity.PhotoListActivity;
import com.example.media.bean.PhotoFile;
import com.example.media.resolver.Contast;

import java.lang.ref.SoftReference;
import java.util.List;

public class PhotoSelector {
    private PhotoOptions mPhotoOptions = PhotoSelector.getDefaultOptions();
    private SoftReference<Activity> mSoftActivity;

    private PhotoSelector(Activity activity) {
        mSoftActivity = new SoftReference<>(activity);
    }

    public static PhotoSelector with(Activity activity) {
        return new PhotoSelector(activity);
    }

    public PhotoSelector setMediaOptions(@NonNull PhotoOptions options) {
        this.mPhotoOptions = options;
        return this;
    }

    public void openPhotoListActivity() {
        if (mSoftActivity != null && mSoftActivity.get() != null) {
            Activity activity = mSoftActivity.get();
            Intent intent = new Intent(activity, PhotoListActivity.class);
            intent.putExtra(Contast.KEY_OPEN_MEDIA, mPhotoOptions);
            activity.startActivityForResult(intent, Contast.CODE_REQUEST_PHOTO_LIST);
        }
    }

    public static List<PhotoFile> resultMediaFile(Intent data) {
        if (data == null)
            return null;
        return data.getParcelableArrayListExtra(Contast.KEY_REQUEST_PHOTO_DATA);
    }


    public static class PhotoOptions implements Parcelable {
        public PhotoOptions() {
        }

        public int maxChooseMedia = Contast.MAX_CHOOSE_MEDIA;
        public boolean isCompress;
        public boolean isShowCamera;
        public boolean isShowVideo;
        public @ColorRes
        int themeColor = R.color.colorTheme;
        public boolean isCrop;
        public int scaleX = 1;
        public int scaleY = 1;
        public int cropWidth = 720;
        public int cropHeight = 720;


        protected PhotoOptions(Parcel in) {
            maxChooseMedia = in.readInt();
            isCompress = in.readByte() != 0;
            isShowCamera = in.readByte() != 0;
            isShowVideo = in.readByte() != 0;
            themeColor = in.readInt();
            isCrop = in.readByte() != 0;
            scaleX = in.readInt();
            scaleY = in.readInt();
            cropWidth = in.readInt();
            cropHeight = in.readInt();
        }

        public static final Creator<PhotoOptions> CREATOR = new Creator<PhotoOptions>() {
            @Override
            public PhotoOptions createFromParcel(Parcel in) {
                return new PhotoOptions(in);
            }

            @Override
            public PhotoOptions[] newArray(int size) {
                return new PhotoOptions[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(maxChooseMedia);
            dest.writeByte((byte) (isCompress ? 1 : 0));
            dest.writeByte((byte) (isShowCamera ? 1 : 0));
            dest.writeByte((byte) (isShowVideo ? 1 : 0));
            dest.writeInt(themeColor);
            dest.writeByte((byte) (isCrop ? 1 : 0));
            dest.writeInt(scaleX);
            dest.writeInt(scaleY);
            dest.writeInt(cropWidth);
            dest.writeInt(cropHeight);
        }
    }

    public synchronized static PhotoOptions getDefaultOptions() {
        return new PhotoOptions();
    }
}
