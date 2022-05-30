package com.huangjs.amap;

import android.opengl.Matrix;

import com.amap.api.maps.model.LatLng;

public class SubMesh extends Mesh {
    // 基点位置
    private LatLng position = null;
    // 所有点坐标
    private float[] points = null;
    // 所有面对应点索引集合
    private int[] faces = null;
    // 变换矩阵
    private final float[] mvpMatrix = new float[16];

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public LatLng getPosition() {
        return this.position;
    }

    public float[] getPoints() {
        return this.points;
    }

    public int[] getFaces() {
        return this.faces;
    }

    public void draw(float[] pMatrix, float[] vMatrix, float x, float y, float scale) {
        // 对当前图形重新绘图
        Matrix.setIdentityM(mvpMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, vMatrix, 0);
        this.draw(mvpMatrix, new float[]{x, y, 0.0f}, new float[]{scale, scale, scale}, new float[]{0.0f, 0.0f, 1.0f, 0.0f});
    }

    public void setData(float[] vertices, float[] vertexColors, int[] faces) {
        this.points = vertices;
        this.faces = faces;
        super.setData(vertices, vertexColors, faces);
    }
}
