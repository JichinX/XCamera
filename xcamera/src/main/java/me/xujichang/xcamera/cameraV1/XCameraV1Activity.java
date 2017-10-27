package me.xujichang.xcamera.cameraV1;


import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.xujichang.utils.tool.LogTool;

import java.io.File;


import me.xujichang.xcamera.R;

import static android.os.Environment.DIRECTORY_DCIM;

/**
 * Created by xjc on 2017/10/18.
 */

public class XCameraV1Activity extends AppCompatActivity implements XCameraListener {
    private XGLSurfaceView mSurfaceView;
    private Camera mCamera;
    private ImageView ivTakePhoto;
    private ProgressBar pbLoading;
    private ImageView ivFocus;
    private boolean isFocusing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_x_camera);
        initView();
        initCamera();
        mSurfaceView.init(mCamera, this);
    }

    private void initView() {
        ivFocus = findViewById(R.id.iv_focus);
        pbLoading = findViewById(R.id.pb_loading);
        pbLoading.setIndeterminate(false);
        mSurfaceView = findViewById(R.id.camera_preview);
        ivTakePhoto = findViewById(R.id.iv_take_photo);
        ivTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始拍照
                File pictureFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM), "YHJJ_" + System.currentTimeMillis()
                        + ".jpg");
                mSurfaceView.takePhoto(pictureFile.getPath());
            }
        });
    }

    private void hideStatusBar() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN //hide statusBar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; //hide navigationBar
        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }

    private void initCamera() {
        if (null == mCamera) {
            int lId = CameraUtils.getFrontCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (lId == -1) {
                return;
            }
            mCamera = Camera.open(lId);
            Camera.Parameters lParameters = mCamera.getParameters();
            CameraUtils.printParameters(lParameters);
            lParameters.set("orientation", "portrait");
            lParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);//白平衡
            lParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//对焦
            lParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);//闪光灯
            lParameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);//
            lParameters.setJpegQuality(100);//照片质量 1-100
            lParameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);//取景模式
            lParameters.setPictureFormat(ImageFormat.JPEG);
            LogTool.d("max :" + lParameters.getMaxExposureCompensation() + "   min :" + lParameters.getMinExposureCompensation());
            lParameters.setExposureCompensation(0);
            mCamera.setParameters(lParameters);
            mCamera.setDisplayOrientation(CameraUtils.getCameraDisplayOrientation(this, lId));
            mCamera.setZoomChangeListener(new Camera.OnZoomChangeListener() {
                @Override
                public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
                    LogTool.d("main  onZoomChange:" + zoomValue);

                }
            });
            mCamera.startSmoothZoom(10);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mCamera) {
            mCamera.stopPreview();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mCamera) {
            mCamera.startPreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCamera) {
            mCamera.release();
        }
    }

    @Override
    public void onPhotoTaking() {
        pbLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPhotoSaving() {
        pbLoading.setVisibility(View.GONE);
    }

    @Override
    public void onPhotoSaved(String path) {
        Toast.makeText(this, "保存成功:" + path, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onFocusing() {
        isFocusing = true;
        ivFocus.setImageResource(R.drawable.ic_focus_ing);
        ivFocus.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFocused() {
        isFocusing = false;
        ivFocus.setImageResource(R.drawable.ic_focus_ed);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFocusing) {
                    ivFocus.setVisibility(View.GONE);
                }
            }
        }, 500);
    }
}
