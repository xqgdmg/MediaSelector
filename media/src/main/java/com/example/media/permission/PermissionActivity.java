package com.example.media.permission;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.example.media.permission.imp.OnPermissionsResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PermissionActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 100;
    private  List<String> mAllowList = new ArrayList<>();
    private  List<String> mNoAllowList = new ArrayList<>();
    private  List<String> mForbidList = new ArrayList<>();
    private OnPermissionsResult mOnPermissionsResult;
    private String[] mPermissions;


    protected void requestPermission(@NonNull OnPermissionsResult onPermissionsResult, @NonNull String... permissions) {
        this.mPermissions = permissions;
        this.mOnPermissionsResult = onPermissionsResult;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> requestList = new ArrayList<>();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    requestList.add(permission);
                }
            }
            if (requestList.size() > 0) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            } else {
                if (mOnPermissionsResult != null ) {
                    mOnPermissionsResult.onAllow(Arrays.asList(permissions));
                }
            }
        } else {
            mOnPermissionsResult.onAllow(Arrays.asList(permissions));
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (permissions.length == grantResults.length) {
                    clearPermission();

                    for (int i = 0; i < grantResults.length; i++) {

                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            mAllowList.add(permissions[i]);
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                                mNoAllowList.add(permissions[i]);
                            } else {
                                mForbidList.add(permissions[i]);
                            }
                        }
                    }

                    if (mAllowList.size() == permissions.length) {
                        if (mOnPermissionsResult != null) {
                            //全部同意
                            mOnPermissionsResult.onAllow(mAllowList);
                        }

                    } else {
                        if (mForbidList.size() > 0) {
                            if (mOnPermissionsResult != null) {
                                //全部永久禁止或者部分永久禁止
                                mOnPermissionsResult.onForbid(mForbidList);
                            }
                        } else {
                            if (mOnPermissionsResult != null) {
                                //全部拒绝
                                mOnPermissionsResult.onNoAllow(mNoAllowList);
                            }
                        }
                    }

                }
                break;
            default:
                break;
        }
    }

    private void clearPermission() {
        mAllowList.clear();
        mNoAllowList.clear();
        mForbidList.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearPermission();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentUtils.OPEN_APPLY_CENTER_CODE && resultCode == 0) {
            if (mPermissions != null && mPermissions.length > 0 && mOnPermissionsResult != null) {
                requestPermission(mOnPermissionsResult, mPermissions);
            }
        }
    }
}
