package com.example.media.permission.imp;

import java.util.List;

public interface OnPermissionsResult {
    void onAllow(List<String> allowPermissions);

    void onNoAllow(List<String> noAllowPermissions);

    void onForbid(List<String> noForbidPermissions);

}
