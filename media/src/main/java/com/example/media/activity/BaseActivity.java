package com.example.media.activity;

import android.os.Bundle;

import com.example.media.permission.PermissionActivity;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.Nullable;

public abstract class BaseActivity extends PermissionActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


}
