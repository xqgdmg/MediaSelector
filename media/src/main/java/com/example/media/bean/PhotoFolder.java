package com.example.media.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PhotoFolder implements Parcelable {
    public String folderName;
    public String folderPath;
    public List<PhotoFile> fileData = new ArrayList<>();
    public boolean isCheck;
    public String firstFilePath;
    public boolean isAllVideo;

    public PhotoFolder() {
    }

    protected PhotoFolder(Parcel in) {
        folderName = in.readString();
        folderPath = in.readString();
        fileData = in.createTypedArrayList(PhotoFile.CREATOR);
        isCheck = in.readByte() != 0;
        firstFilePath = in.readString();
        isAllVideo = in.readByte() != 0;
    }

    public static final Creator<PhotoFolder> CREATOR = new Creator<PhotoFolder>() {
        @Override
        public PhotoFolder createFromParcel(Parcel in) {
            return new PhotoFolder(in);
        }

        @Override
        public PhotoFolder[] newArray(int size) {
            return new PhotoFolder[size];
        }
    };

    /**
     * 判断文件夹的路径是否一致判断是否相等
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || folderPath == null)
            return false;
        if (obj instanceof PhotoFolder) {
            PhotoFolder folder = (PhotoFolder) obj;
            return this.folderPath.equals(folder.folderPath);
        }
        return super.equals(obj);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(folderName);
        dest.writeString(folderPath);
        dest.writeTypedList(fileData);
        dest.writeByte((byte) (isCheck ? 1 : 0));
        dest.writeString(firstFilePath);
        dest.writeByte((byte) (isAllVideo ? 1 : 0));
    }
}
