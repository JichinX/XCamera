package me.xujichang.xcamera.cameraV1;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.xujichang.utils.tool.LogTool;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_FRAMEBUFFER;

/**
 * Created by xjc on 2017/10/18.
 */

public class XRender implements GLSurfaceView.Renderer {
    private static final int PRE_FLOAT_BYTE = Float.SIZE / 8;
    private final Context context;
    private int muTextMatrixLoc;
    private int muMVPMatrixLoc;
    private FloatBuffer textCoordBuffer;
    private FloatBuffer positionBuffer;
    private int textureCoordLoc;
    private int positionLoc;
    private int msTextureLoc;
    private float[] matrix;
    private int mProgram;
    private XGLSurfaceView mSurfaceView;
    private Camera mCamera;
    private boolean isPreviewStarted = false;
    private int textureId;
    private XDrawEngine mEngine;
    private int[] mFBOIds = new int[1];
    private SurfaceTexture mSurfaceTexture;
    private float[] transformMatrix = new float[16];
    private float[] mvpMatrix = new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    public XRender(XGLSurfaceView pXGLSurfaceView, Camera pCamera, Context pContext) {
        this.context = pContext;
        mSurfaceView = pXGLSurfaceView;
        mCamera = pCamera;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        LogTool.d("onSurfaceCreated-------start");
        textureId = XGLUtils.createOESTexture();
        mEngine = new XDrawEngine(textureId, context);
        positionBuffer = mEngine.getPositionFloatBuffer();
        textCoordBuffer = mEngine.getTextCoordFloatBuffer();
        mProgram = mEngine.getProgrom();
        GLES20.glGenFramebuffers(1, mFBOIds, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOIds[0]);

        LogTool.d("onSurfaceCreated-------end");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        LogTool.d("onSurfaceChanged:width-" + width + " height-" + height);
        CameraUtils.setPreviewSize(mCamera, width, height);
//        CameraUtils.setMaxPreviewSize(mCamera);
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        if (null != mSurfaceTexture) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }
        if (!isPreviewStarted) {
            isPreviewStarted = initSurfaceTexture();
            return;
        }
        GLES20.glUseProgram(mProgram);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        positionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        textureCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        muTextMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");
        msTextureLoc = GLES20.glGetUniformLocation(mProgram, "sTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(msTextureLoc, 0);
        GLES20.glUniformMatrix4fv(muTextMatrixLoc, 1, false, transformMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        if (null != positionBuffer) {
            positionBuffer.position(0);
            GLES20.glEnableVertexAttribArray(positionLoc);
            GLES20.glVertexAttribPointer(positionLoc, 2, GLES20.GL_FLOAT, false, 2 * PRE_FLOAT_BYTE, positionBuffer);
        }
        if (null != textCoordBuffer) {
            textCoordBuffer.position(0);
            GLES20.glEnableVertexAttribArray(textureCoordLoc);
            GLES20.glVertexAttribPointer(textureCoordLoc, 2, GLES20.GL_FLOAT, false, 2 * PRE_FLOAT_BYTE, textCoordBuffer);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glFinish();
    }

    private boolean initSurfaceTexture() {
        if (null == mCamera || null == mSurfaceView) {
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(textureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mSurfaceView.requestRender();
            }
        });
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException pE) {
            pE.printStackTrace();
            return false;
        }
        mCamera.startPreview();
        return true;
    }
}
