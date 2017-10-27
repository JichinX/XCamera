package me.xujichang.xcamera.cameraV1;

import android.content.Context;

import java.nio.FloatBuffer;

import me.xujichang.xcamera.R;


/**
 * Created by xjc on 2017/10/18.
 */

public class XDrawEngine {
    private int mTextueId;
    private Context mContext;
    private FloatBuffer mFloatBuffer;
    private int progrom;
    private static final int PRE_FLOAT_BYTE = Float.SIZE / 8;
    private final float[] position = new float[]{
            -1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
    };
    private final float[] textureCoord = new float[]{
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    public XDrawEngine(int pTextueId, Context pContext) {
        mContext = pContext;
        mTextueId = pTextueId;
        String vertexSrc = XGLUtils.readTextFileFormResource(mContext, R.raw.vertex_shader);
        String fragmentSrc = XGLUtils.readTextFileFormResource(mContext, R.raw.fragment_shader);
        progrom = XGLUtils.createProgram(vertexSrc, fragmentSrc);
    }

    public FloatBuffer getFloatBuffer() {
        return mFloatBuffer;
    }

    public int getProgrom() {
        return progrom;
    }

    public FloatBuffer getTextCoordFloatBuffer() {
        return XGLUtils.getFloatBuffer(textureCoord);
    }

    public FloatBuffer getPositionFloatBuffer() {
        return XGLUtils.getFloatBuffer(position);

    }
}
