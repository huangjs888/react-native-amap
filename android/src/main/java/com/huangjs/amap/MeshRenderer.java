package com.huangjs.amap;

import android.graphics.PointF;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MeshRenderer implements CustomRenderer {

  private final AMap aMap;
  private final List<SubMesh> meshList = new ArrayList<>();

  public MeshRenderer(AMap aMap) {
    this.aMap = aMap;
  }

  public void removeMesh(SubMesh mesh) {
    if (mesh != null) {
      mesh.destroy();
    }
    meshList.remove(mesh);
  }

  public void clearMesh() {
    for (SubMesh mesh : meshList) {
      if (mesh != null) {
        mesh.destroy();
      }
    }
    meshList.clear();
  }

  // 由给定的三个顶点的坐标，计算三角形面积。
  private double triangleArea(float[] pa, float[] pb, float[] pc) {
    if (pa == null || pb == null || pc == null) {
      return 0;
    }
    return Math.abs((pa[0] * pb[1] + pb[0] * pc[1] + pc[0] * pa[1] - pb[0] * pa[1] - pc[0] * pb[1] - pa[0] * pc[1]) / 2.0D);
  }

  private float[] minus(float[] a, float[] b) {
    if (a == null || b == null) {
      return null;
    }
    return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
  }

  private float multiply(float[] a, float[] b) {
    if (a == null || b == null) {
      return 0;
    }
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
  }

  // 判断点p是否在指定的三角形内或线上：点p与三角形三边
  private boolean isInTriangle(float[] p, float[] pa, float[] pb, float[] pc, int type) {
    if (type == 0) {
      // 面积法：p与三点形成的面积之和等于三角形面积
      double triangleArea = triangleArea(pa, pb, pc);
      double sumArea = triangleArea(p, pa, pb) + triangleArea(p, pa, pc) + triangleArea(p, pb, pc);
      double epsilon = 0.0001;  // 由于浮点数的计算存在误差，故指定一个足够小的数，用于判定两个面积是否(近似)相等。
      return Math.abs(triangleArea - sumArea) < epsilon;
    } else if (type == 1) {
      // 重心法：https://www.cnblogs.com/graphics/archive/2010/08/05/1793393.html
      float[] v0 = minus(pc, pa);
      float[] v1 = minus(pb, pa);
      float[] v2 = minus(p, pa);
      float dot00 = multiply(v0, v0);
      float dot01 = multiply(v0, v1);
      float dot02 = multiply(v0, v2);
      float dot11 = multiply(v1, v1);
      float dot12 = multiply(v1, v2);
      float divisor = dot00 * dot11 - dot01 * dot01;
      float u = (dot11 * dot02 - dot01 * dot12) / divisor;
      float v = (dot00 * dot12 - dot01 * dot02) / divisor;
      return u >= 0 && u <= 1 && v >= 0 && v <= 1 && u + v <= 1;
    }
    return false;
  }

  // 计算两点连线在坐标系中的方位角
  private double getAngle(double x0, double y0, double x1, double y1) {
    double distance = getDistance(x0, y0, x1, y1);
    double angle = Math.asin(Math.abs(y1 - y0) / distance);
    if (y1 >= y0) {
      if (x1 >= x0) angle = 2 * Math.PI - angle;// 第一象限+y正半轴+x正半轴
      else angle = Math.PI + angle;// 第二象限+x负半轴
    } else {
      if (x1 <= x0) angle = Math.PI - angle;// 第三象限+y负半轴
      else angle = angle * 1;// 第四象限
    }
    return angle;
  }

  // 计算两点之间的距离
  private double getDistance(double x0, double y0, double x1, double y1) {
    return Math.sqrt((Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2)));
  }

  public List<Map<String, Object>> pickMeshInfoByPoint(LatLng point2, List<SubMesh> curMesh) {
    PointF glPoint2 = aMap.getProjection().toOpenGLLocation(point2);
    List<Map<String, Object>> triangles = new ArrayList<>();
    List<SubMesh> allMesh = new ArrayList<>();
    if (curMesh != null && curMesh.size() > 0) {
      allMesh.addAll(curMesh);
    } else {
      allMesh.addAll(meshList);
    }
    for (SubMesh mesh : allMesh) {
      if (mesh != null) {
        LatLng point = mesh.getPosition();
        PointF glPoint = aMap.getProjection().toOpenGLLocation(point);
        double mapDistance = AMapUtils.calculateLineDistance(point, point2);
        double mapAzimuth = getAngle(glPoint.x, glPoint.y, glPoint2.x, glPoint2.y);
        double x = mapDistance * Math.cos(mapAzimuth);
        // 此处为负值，因为地图三维坐标y轴正方向是向南而不是向北
        double y = -mapDistance * Math.sin(mapAzimuth);
        float[] projectionPoint = new float[]{(float) x, (float) y, 0};
        int faceIndex = -1;
        float[] vertices = mesh.getPoints();
        int[] faces = mesh.getFaces();
        if (vertices != null && faces != null) {
          // 循环所有三角形面，判断该点是否在三角形内
          if (faces.length == 0) {
            // 此种情况是没用到面,9个数即为3个点
            for (int i = 0, len = vertices.length; i < len; i += 9) {
              boolean inner = isInTriangle(projectionPoint,
                new float[]{vertices[i], vertices[i + 1], vertices[i + 2]},
                new float[]{vertices[i + 3], vertices[i + 4], vertices[i + 5]},
                new float[]{vertices[i + 6], vertices[i + 7], vertices[i + 8]}, 1);
              if (inner) {
                // 一旦找到一个三角形，立即退出循环
                faceIndex = i / 9;
                break;
              }
            }
          } else {
            for (int i = 0, len = faces.length; i < len; i += 3) {
              int ap = faces[i];
              int bp = faces[i + 1];
              int cp = faces[i + 2];
              boolean inner = isInTriangle(projectionPoint,
                new float[]{vertices[ap * 3], vertices[ap * 3 + 1], vertices[ap * 3 + 2]},
                new float[]{vertices[bp * 3], vertices[bp * 3 + 1], vertices[bp * 3 + 2]},
                new float[]{vertices[cp * 3], vertices[cp * 3 + 1], vertices[cp * 3 + 2]}, 1);
              if (inner) {
                // 一旦找到一个三角形，立即退出循环
                faceIndex = i / 3;
                break;
              }
            }
          }
        }
        if (faceIndex != -1) {// 表示该点在该mesh上
          HashMap<String, Object> obj = new HashMap<>();
          obj.put("meshObject", mesh);
          obj.put("faceIndex", faceIndex);
          obj.put("projectionPoint", projectionPoint);
          obj.put("projectionDistance", mapDistance);
          triangles.add(obj);
        }
      }
    }
    return triangles;
  }

  public SubMesh addMesh() {
    SubMesh mesh = new SubMesh();
    meshList.add(mesh);
    return mesh;
  }

  public SubMesh addMesh(LatLng position) {
    SubMesh mesh = this.addMesh();
    mesh.setPosition(position);
    return mesh;
  }

  public SubMesh addMesh(LatLng position, float[] vertices, float[] vertexColors, int[] faces) {
    SubMesh mesh = this.addMesh();
    mesh.setPosition(position);
    mesh.setData(vertices, vertexColors, faces);
    return mesh;
  }

  @Override
  public void onDrawFrame(GL10 gl10) {
    for (SubMesh mesh : meshList) {
      if (mesh != null) {
        if (!mesh.isInitShader()) {
          // 初始化之后，用户主动添加的mesh没有在onSurfaceCreated事件内进行initShader，这里补上
          mesh.initShader();
        }
        LatLng position = mesh.getPosition();
        if (position != null) {
          PointF glPoint = aMap.getProjection().toOpenGLLocation(position);
          LatLng position2 = new LatLng(position.latitude + 0.0001, position.longitude + 0.0001);
          PointF glPoint2 = aMap.getProjection().toOpenGLLocation(position2);
          double mapDistance = AMapUtils.calculateLineDistance(position, position2);
          double glDistance = getDistance(glPoint.x, glPoint.y, glPoint2.x, glPoint2.y);
          // 传入的顶点都是实际图上距离，要转换位opengl距离
          // 计算距离坐标和gl坐标比例
          float scale = (float) (glDistance / mapDistance);
          mesh.draw(aMap.getProjectionMatrix(), aMap.getViewMatrix(), glPoint.x, glPoint.y, scale);
        } else {
          mesh.draw(aMap.getProjectionMatrix(), aMap.getViewMatrix(), 0, 0, 1);
        }
      }
    }
  }

  @Override
  public void OnMapReferencechanged() {
    // 回调这个时，坐标系发生改变
  }

  @Override
  public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
    for (SubMesh mesh : meshList) {
      if (mesh != null) {
        mesh.initShader();
      }
    }

  }

  @Override
  public void onSurfaceChanged(GL10 gl10, int width, int height) {
    for (SubMesh mesh : meshList) {
      if (mesh != null) {
        mesh.updateViewport(0, 0, width, height);
      }
    }
  }
}
