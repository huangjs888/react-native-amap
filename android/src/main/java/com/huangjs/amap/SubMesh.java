package com.huangjs.amap;

import com.amap.api.maps.model.LatLng;

public class SubMesh extends Mesh {
  // 位置，最终转化为translate位置偏移
  private LatLng position = null;
  // 旋转
  private float[] rotate = new float[]{0.0f, 0.0f, 1.0f, 0.0f};
  // 缩放比例
  private float[] scale = new float[]{1.0f, 1.0f, 1.0f};
  // 所有点坐标
  private float[] points = null;
  // 所有面对应点索引集合
  private int[] faces = null;

  public void setPosition(LatLng position) {
    this.position = position;
  }

  public LatLng getPosition() {
    return this.position;
  }

  public void setRotate(String axis, float angle) {
    if (rotate.length == 4) {
      this.rotate = new float[]{rotate[0] != 0 ? 1.0f : 0.0f, rotate[1] != 0 ? 1.0f : 0.0f, (rotate[2] != 0 || (rotate[0] == 0 && rotate[1] == 0)) ? 1.0f : 0.0f, rotate[3]};
    }
    rotate = new float[]{"x".equals(axis) ? 1.0f : rotate[0], "y".equals(axis) ? 1.0f : rotate[1], "z".equals(axis) ? 1.0f : rotate[2], angle};
  }

  public float[] getRotate() {
    return this.rotate;
  }

  public void setScale(float[] scale) {
    if (scale.length == 3) {
      this.scale = new float[]{rotate[0] > 0 ? rotate[0] : 1.0f, rotate[1] > 0 ? rotate[1] : 1.0f, rotate[2] > 0 ? rotate[2] : 1.0f};
    }
  }

  public float[] getScale() {
    return this.scale;
  }

  public void setData(float[] vertices, float[] vertexColors, int[] faces) {
    this.points = vertices;
    this.faces = faces;
    super.setData(vertices, vertexColors, faces);
  }

  public float[] getPoints() {
    return this.points;
  }

  public int[] getFaces() {
    return this.faces;
  }
}
