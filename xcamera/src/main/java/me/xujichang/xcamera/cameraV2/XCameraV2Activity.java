package me.xujichang.xcamera.cameraV2;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;


import me.xujichang.xcamera.R;
import me.xujichang.util.tool.LogTool;

/**
 * Created by xjc on 2017/10/20.
 */

public class XCameraV2Activity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_v2);
        CameraManager camManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        String[] cameraIds;//获取相机ID 集合
        if (null == camManager) {
            return;
        }
        try {
            Integer frontCamLevel = -1;
            Integer backCamLevel = -1;
            cameraIds = camManager.getCameraIdList();
            if (cameraIds.length > 0) {
                for (int i = 0; i < cameraIds.length; i++) {
                    if (String.valueOf(
                            CameraCharacteristics.LENS_FACING_FRONT).equalsIgnoreCase(//对比   CameraCharacteristics.LENS_FACING_FRONT = 0；
                            cameraIds[i])) {
                        frontCamLevel = camManager.getCameraCharacteristics(cameraIds[i]).get(
                                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    } else if (String.valueOf(
                            CameraCharacteristics.LENS_FACING_BACK).equalsIgnoreCase(//  CameraCharacteristics.LENS_FACING_BACK = 1;
                            cameraIds[i])) {
                        backCamLevel = camManager.getCameraCharacteristics(cameraIds[i]).get(
                                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    }
                }
            }
            LogTool.d("frontCamLevel:" + frontCamLevel + "  backCamLevel:" + backCamLevel);
//            int propValue = SystemProperties.getInt("camera.mtkapp.api2.enable", 0);
//            LogTool.d("propValue:" + propValue);
        } catch (CameraAccessException pE) {
            pE.printStackTrace();
        }

    }
}
