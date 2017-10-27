package me.xujichang.xcamera.cameraV1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by xjc on 2017/10/19.
 */

public class RunnableConst {

    public static Runnable getSaveImageRunnable(byte[] pData, File pStoreFile, Camera pMCamera, ThreadFinishListener<String> pFinishListener) {

        return new SaveImageRunnable(pData, pStoreFile, pMCamera, pFinishListener);
    }

    private static class SaveImageRunnable implements Runnable {
        private byte[] data;
        private File storeFile;
        private Camera mCamera;
        private ThreadFinishListener<String> mFinishListener;

        public SaveImageRunnable(byte[] pData, File pStoreFile, Camera pMCamera, ThreadFinishListener<String> pFinishListener) {
            data = pData;
            storeFile = pStoreFile;
            mCamera = pMCamera;
            mFinishListener = pFinishListener;
        }

        @Override
        public void run() {
            try {
                Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap bitmap = CameraUtils.rotate(realImage, 90);

                FileOutputStream fos = new FileOutputStream(storeFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                CameraUtils.addExifInfo(storeFile, mCamera);
                mFinishListener.onFinish(storeFile.getPath());
            } catch (Exception e) {

            }
        }
    }
}
