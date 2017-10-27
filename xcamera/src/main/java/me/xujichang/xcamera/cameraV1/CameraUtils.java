package me.xujichang.xcamera.cameraV1;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.Surface;

import com.xujichang.utils.tool.LogTool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xjc on 2017/10/18.
 */

public class CameraUtils {
    private static boolean zoomInit = false;
    private static PointF point1;
    private static PointF point2;

    private static Map<Integer, HandlerHolderCallback> sCallbackMap = new HashMap<>();
    private static Handler.Callback sCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            HandlerHolderCallback lHandlerHolderCallback = sCallbackMap.get(msg.what);
            if (null != lHandlerHolderCallback) {
                lHandlerHolderCallback.onOutside(msg);
            }
            return false;
        }
    };
    private static Handler sHandler = new Handler(sCallback);

    /**
     * 查找前置相机的标志
     *
     * @return
     */
    public static int getFrontCamera(int facing) {
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo lCameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, lCameraInfo);
            if (lCameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 查找适合的预览分辨率
     *
     * @param pParameters
     * @return
     */
    public static Camera.Size getOptimalPreviewSize(Camera.Parameters pParameters) {
        List<Camera.Size> lSizes = pParameters.getSupportedPreviewSizes();
        int count = lSizes.size();
        int index = 0;
        if (count > 0) {
            index = (count + 1) / 2;
        } else {
            return null;
        }
        Camera.Size lSize = lSizes.get(index);
        LogTool.d("preview size:" + lSize.width + "       " + lSize.height);
        return lSize;
    }

    /**
     * 查找适合的预览分辨率
     *
     * @param pParameters
     * @return
     */
    public static Camera.Size getOptimalPreviewSize(Camera.Parameters pParameters, float ratio) {
        List<Camera.Size> lSizes = pParameters.getSupportedPreviewSizes();
        int count = lSizes.size();
        Camera.Size lSize = null;
        int index = (count + 1) / 2;
        float tempRatioDiff = 1f;
        float tempRatio;
        float innerRatio;
        Camera.Size size;
        for (int i = 0; i < count; i++) {
            size = lSizes.get(i);
            tempRatio = size.width / size.height;
            if ((innerRatio = Math.abs(tempRatio - ratio)) < tempRatioDiff) {
                tempRatioDiff = innerRatio;
                index = i;
            }
            LogTool.d("支持的分辨率：" + size.width + " * " + size.height);
        }
        lSize = lSizes.get(index);
        return lSize;
    }

    public static int getCameraDisplayOrientation(Activity activity,
                                                  int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 按照宽高的比例 选择预览的大小
     *
     * @param pCamera
     * @param pWidth
     * @param pHeight
     */
    public static Camera.Size setPreviewSize(Camera pCamera, int pWidth, int pHeight) {
        float ratio = pHeight / pWidth;
        Camera.Parameters lParameters = pCamera.getParameters();
        Camera.Size previewSize = getOptimalPreviewSize(lParameters, ratio);
        if (null != previewSize) {
            LogTool.d("获得最适合的预览分辨率：" + previewSize.width + " * " + previewSize.height);
            lParameters.setPreviewSize(previewSize.width, previewSize.height);
            lParameters.setPictureSize(previewSize.width, previewSize.height);
        }
        pCamera.setParameters(lParameters);
        return previewSize;
    }

    /**
     * 选取分辨率 最大的预览大小
     *
     * @param pCamera
     */
    public static void setMaxPreviewSize(Camera pCamera) {
        Camera.Parameters lParameters = pCamera.getParameters();
        Camera.Size previewSize = getMaxPreviewSize(lParameters);
        if (null != previewSize) {
            LogTool.d("获得最大的预览分辨率：" + previewSize.width + " * " + previewSize.height);
            lParameters.setPreviewSize(previewSize.width, previewSize.height);
        }
    }

    private static Camera.Size getMaxPreviewSize(Camera.Parameters pParameters) {
        List<Camera.Size> lSizes = pParameters.getSupportedPreviewSizes();
        int sum = 0;
        int tempSum;
        int count = lSizes.size();
        int index = (count + 1) / 2;
        Camera.Size tempSize;
        for (int i = 0; i < count; i++) {
            tempSize = lSizes.get(i);
            tempSum = tempSize.width * tempSize.height;
            if (tempSum > sum) {
                sum = tempSum;
                index = i;
            }
        }
        return lSizes.get(index);
    }

    /**
     * 添加Exif信息
     *
     * @param pStroeFile
     * @param pCamera
     */
    public static void addExifInfo(File pStroeFile, Camera pCamera) throws IOException {
        ExifInterface exifInfo = new ExifInterface(pStroeFile.getPath());
        exifInfo.saveAttributes();

    }

    /**
     * 做旋转
     *
     * @param pBitmap
     * @param angle
     * @return
     */
    public static Bitmap rotate(Bitmap pBitmap, int angle) {
        //旋转图片 动作
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(pBitmap, 0, 0,
                pBitmap.getWidth(), pBitmap.getHeight(), matrix, true);
        pBitmap.recycle();
        return resizedBitmap;
    }

    public static void saveImage(byte[] data, File stroeFile, Camera mCamera, final XCameraListener pXCameraListener) {
        registerCallback(1, new HandlerHolderCallback() {
            @Override
            public void onOutside(Message pMessage) {
                pXCameraListener.onPhotoSaved((String) pMessage.obj);
                sCallbackMap.remove(1);
            }
        });
        ThreadFinishListener<String> lFinishListener = new ThreadFinishListener<String>() {
            @Override
            public void onFinish(String result) {
                Message lMessage = sHandler.obtainMessage();
                lMessage.obj = result;
                lMessage.what = 1;
                sHandler.sendMessage(lMessage);
            }
        };
        Runnable lRunnable = RunnableConst.getSaveImageRunnable(data, stroeFile, mCamera, lFinishListener);
        startNewThread(lRunnable);
    }

    private static void registerCallback(int what, HandlerHolderCallback pHandlerHolderCallback) {
        sCallbackMap.put(what, pHandlerHolderCallback);
    }

    private static void startNewThread(Runnable pRunnable) {
        new Thread(pRunnable).start();
    }

    /**
     * 打印Parameters
     *
     * @param pParameters
     */
    public static void printParameters(Camera.Parameters pParameters) {
        List<Camera.Size> pictureSizes = pParameters.getSupportedPictureSizes();
        LogTool.d("支持的照片尺寸");
        for (Camera.Size lPictureSize : pictureSizes) {
            LogTool.d(lPictureSize.width + " * " + lPictureSize.height);
        }
        List<String> colorEffect = pParameters.getSupportedColorEffects();
        LogTool.d("支持的滤镜效果");
        for (String effect : colorEffect) {
            LogTool.d(effect);
        }
        LogTool.d("最大放大倍数：" + pParameters.getMaxZoom());
    }

    /**
     * 进行放大缩小操作
     *
     * @param pCamera
     * @param pEvent
     */
    public static void PreviewZoom(Camera pCamera, MotionEvent pEvent) {

        if (!zoomInit) {
            point1 = new PointF(pEvent.getX(0), pEvent.getY(0));
            point2 = new PointF(pEvent.getX(1), pEvent.getY(1));
            zoomInit = true;
            return;
        }
        Camera.Parameters lParameters = pCamera.getParameters();
        int zoomValue = getZoomValue(pEvent);
        int zoom = lParameters.getZoom();
        if (zoom == 0) {
            zoom = zoomValue;
        } else {
            zoom = zoomValue * zoom;
        }
        if (zoom > lParameters.getMaxZoom()) {
            zoom = lParameters.getMaxZoom();
        }
        if (zoom <= 1) {
            zoom = 1;
        }
        if (lParameters.isZoomSupported()) {
            LogTool.d("smoothZoom:" + zoom);
            pCamera.setZoomChangeListener(new Camera.OnZoomChangeListener() {
                @Override
                public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
                    LogTool.d("onZoomChange:" + zoomValue);
                }
            });
            pCamera.startSmoothZoom(zoom);
        } else {
            LogTool.d("Zoom:" + zoom);
            lParameters.setZoom(zoom);
            pCamera.setParameters(lParameters);
        }
    }

    /**
     * 计算放大倍数
     *
     * @param pEvent
     * @return
     */
    private static int getZoomValue(MotionEvent pEvent) {
        double src = getDistance(point1, point2);
        double current = getDistance(new PointF(pEvent.getX(0), pEvent.getX(1))
                , new PointF(pEvent.getY(0), pEvent.getY(1)));

        return (int) (current / src);
    }

    private static double getDistance(PointF pPoint1, PointF pPoint2) {
        return Math.sqrt(Math.pow(Math.abs(pPoint1.x - pPoint2.x), 2) + Math.pow(Math.abs(pPoint1.y - pPoint2.y), 2));
    }

    public interface HandlerHolderCallback {
        void onOutside(Message pMessage);
    }
}
