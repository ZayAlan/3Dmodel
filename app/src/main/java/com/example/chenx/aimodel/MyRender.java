package com.example.chenx.aimodel;

import android.opengl.GLSurfaceView;

import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_UNSIGNED_INT;

public class MyRender implements GLSurfaceView.Renderer {

    private float[] structurePoints;
    private float[] mColor;
    private int[] indice;
    private FloatBuffer mStructureBuffer;
    private FloatBuffer mColorBuffer;
    private IntBuffer indiceBuffer;

    public MyRender(MatOfPoint3f structure){
        initPoints(structure);
        ByteBuffer bb = ByteBuffer.allocateDirect(structurePoints.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mStructureBuffer = bb.asFloatBuffer();
        mStructureBuffer.put(structurePoints);
        mStructureBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(mColor.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        mColorBuffer = bb2.asFloatBuffer();
        mColorBuffer.put(mColor);
        mColorBuffer.position(0);

        ByteBuffer bb3 = ByteBuffer.allocateDirect(indice.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        indiceBuffer = bb3.asIntBuffer();
        indiceBuffer.put(indice);
        indiceBuffer.position(0);
    }

    private void initPoints(MatOfPoint3f structure){
        MatOfPoint3f originalStruct = structure;
        List<Float> tempList = new ArrayList<>();
        for (int i = 0;i<originalStruct.toArray().length;i++){
            Point3 temp = originalStruct.toArray()[i];
            if (Double.isNaN(temp.x)) continue;
            tempList.add((float)temp.x);
            tempList.add((float)temp.y);
            tempList.add((float)temp.z);
        }
        structurePoints = new float[tempList.size()];
        int colorSize = (tempList.size()/3)*4;
        int indiceSize = tempList.size()/3;
        mColor = new float[colorSize];
        indice = new int[indiceSize];
        for (int i = 0; i< tempList.size();i++){
            structurePoints[i] = tempList.get(i);
        }
        for (int i = 0; i< colorSize;i++){
            mColor[i] = 1;
        }
        indice[0] = indiceSize-1;
        for (int i = 1;i<indiceSize;i++){
            indice[i] = i-1 ;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 设置白色为清屏
        gl.glClearColor(0, 0, 0, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float) width / height;
        // 设置OpenGL场景的大小,(0,0)表示窗口内部视口的左下角，(w,h)指定了视口的大小
        gl.glViewport(0, 0, width, height);
        // 设置投影矩阵
        gl.glMatrixMode(GL10.GL_PROJECTION);
        // 重置投影矩阵
        gl.glLoadIdentity();
        // 设置视口的大小
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        //以下两句声明，以后所有的变换都是针对模型(即我们绘制的图形)
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清除屏幕和深度缓存
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        // 重置当前的模型观察矩阵
        gl.glLoadIdentity();

        // 允许设置顶点
        //GL10.GL_VERTEX_ARRAY顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glScalef(1,1,1);
        gl.glTranslatef(0f, 0.0f, 2.0f);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mStructureBuffer);
        gl.glColor4f(1,1,1,1);
//        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
        gl.glPointSize(1f); //改变点的像素大小

        gl.glDrawArrays(GL10.GL_POINTS, 0,structurePoints.length/3);
//        gl.glDrawElements(GL10.GL_POINTS,indice.length,GL_UNSIGNED_INT,indiceBuffer);

//        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glFinish();
    }
}
