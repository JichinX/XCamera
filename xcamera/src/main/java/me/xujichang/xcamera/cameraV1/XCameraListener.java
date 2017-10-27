package me.xujichang.xcamera.cameraV1;

import android.content.Context;

/**
 * Created by xjc on 2017/10/19.
 */

public interface XCameraListener {
    void onPhotoTaking();

    void onPhotoSaving();

    void onPhotoSaved(String path);

    Context getContext();

    void onFocusing();

    void onFocused();
}
