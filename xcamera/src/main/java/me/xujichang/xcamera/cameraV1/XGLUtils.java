package me.xujichang.xcamera.cameraV1;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by xjc on 2017/8/22.
 */

public class XGLUtils {
    public static FloatBuffer getFloatBuffer(float[] floats) {

        return getFloatBuffer(floats, 4);
    }

    public static FloatBuffer getFloatBuffer(float[] floats, int size) {
        FloatBuffer buffer;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(floats.length * size);
        byteBuffer.order(ByteOrder.nativeOrder());
        buffer = byteBuffer.asFloatBuffer();
        buffer.put(floats);
        buffer.position(0);
        return buffer;
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        checkGLError("glCreateProgram");
        if (program == 0) {
            return 0;
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGLError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    public static void checkGLError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static String readTextFileFormResource(Context context, @RawRes int resId) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = context.getResources().openRawResource(resId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String nextline;
            while ((nextline = reader.readLine()) != null) {
                stringBuilder.append(nextline).append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("Can not open resource :" + resId, e);
        } catch (Resources.NotFoundException rnf) {
            throw new RuntimeException("Resource not found :" + resId, rnf);
        }
        return stringBuilder.toString();
    }

    public static int createOESTexture() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }
}
