package me.xujichang.xcamera.cameraV1;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.xujichang.utils.tool.LogTool;

import java.io.File;

/**
 * Created by xjc on 2017/10/18.
 */

public class XGLSurfaceView extends GLSurfaceView {
    private XRender mXRender;
    private Camera mCamera;
    private Camera.PictureCallback mPictureCallback;
    private File stroeFile;
    private boolean takePhoto;
    private XCameraListener mXCameraListener;
    private boolean smoothZoom = false;

    public XGLSurfaceView(Context context) {
        this(context, null);
    }

    public XGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);

    }

    public void init(Camera pCamera, XCameraListener pXCameraListener) {
        mCamera = pCamera;
        mXCameraListener = pXCameraListener;
        setEGLContextClientVersion(2);
        mXRender = new XRender(this, pCamera, pXCameraListener.getContext());
        setRenderer(mXRender);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success && takePhoto) {
                    takePhoto = false;
                }
                if (null != mXCameraListener) {
                    if (success) {
                        mXCameraListener.onFocused();
                    } else {
                        mXCameraListener.onFocusing();
                    }
                }
                LogTool.d("对焦中..." + success);
            }
        });
        mPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                if (null != mXCameraListener) {
                    mXCameraListener.onPhotoSaving();
                }
                camera.startPreview();
                if (null == stroeFile) {
                    return;
                }
                CameraUtils.saveImage(data, stroeFile, mCamera, mXCameraListener);
            }
        };
    }

    public void takePhoto(String path) {
        if (null != mXCameraListener) {
            mXCameraListener.onPhotoTaking();
        }
        stroeFile = new File(path);
        mCamera.takePicture(null, null, null, mPictureCallback);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        LogTool.d("触摸事件");
        if (action == MotionEvent.ACTION_MOVE) {
            LogTool.d("Move事件");
            int count = event.getPointerCount();
            if (count > 1 && !smoothZoom) {
                mCamera.startSmoothZoom(10);
                smoothZoom = true;
//                CameraUtils.PreviewZoom(mCamera, event);
            }
        }
        return super.onTouchEvent(event);
    }
}
