package com.example.media.bean;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.example.media.utils.FileUtils;

import java.io.File;

public class PhotoFile implements Parcelable {
    public String fileName;
    public String filePath;
    public int fileSize;
    public int width;
    public int height;
    public String folderName;
    public String folderPath;
    public boolean isCheck;
    public boolean isShowCamera;
    public boolean isVideo;
    public long videoDuration;

    public PhotoFile() {
    }

    protected PhotoFile(Parcel in) {
        fileName = in.readString();
        filePath = in.readString();
        fileSize = in.readInt();
        width = in.readInt();
        height = in.readInt();
        folderName = in.readString();
        folderPath = in.readString();
        isCheck = in.readByte() != 0;
        isShowCamera = in.readByte() != 0;
        isVideo = in.readByte() != 0;
        videoDuration = in.readLong();
    }

    public static final Creator<PhotoFile> CREATOR = new Creator<PhotoFile>() {
        @Override
        public PhotoFile createFromParcel(Parcel in) {
            return new PhotoFile(in);
        }

        @Override
        public PhotoFile[] newArray(int size) {
            return new PhotoFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeString(filePath);
        dest.writeInt(fileSize);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(folderName);
        dest.writeString(folderPath);
        dest.writeByte((byte) (isCheck ? 1 : 0));
        dest.writeByte((byte) (isShowCamera ? 1 : 0));
        dest.writeByte((byte) (isVideo ? 1 : 0));
        dest.writeLong(videoDuration);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof PhotoFile) {
            PhotoFile photoFile = (PhotoFile) obj;
            if (photoFile.filePath != null && this.filePath != null &&
                    photoFile.filePath.equals(filePath)) {
                return true;
            }
        }
        return super.equals(obj);
    }


    public boolean hasData() {
        return !TextUtils.isEmpty(fileName) && TextUtils.getTrimmedLength(fileName) > 0
                && !TextUtils.isEmpty(filePath) && TextUtils.getTrimmedLength(filePath) > 0
                && fileSize > 0 && width > 0 && height > 0
                && !TextUtils.isEmpty(folderName) && TextUtils.getTrimmedLength(folderName) > 0
                && !TextUtils.isEmpty(folderPath) && TextUtils.getTrimmedLength(folderPath) > 0;
    }

    public static PhotoFile checkFileToThis(@NonNull File file) {
        PhotoFile mediaFile = new PhotoFile();
        mediaFile.fileName = file.getName();
        mediaFile.filePath = file.getAbsolutePath();
        mediaFile.fileSize = (int) file.length();
        mediaFile.width = FileUtils.getFileWidth(file.getAbsolutePath());
        mediaFile.height = FileUtils.getFileHeight(file.getAbsolutePath());
        mediaFile.folderName = FileUtils.getParentFileName(file.getAbsolutePath());
        mediaFile.folderPath = FileUtils.getParentFilePath(file.getAbsolutePath());
        mediaFile.isCheck = true;
        return mediaFile;
    }

}
