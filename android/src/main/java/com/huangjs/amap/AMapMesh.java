package com.huangjs.amap;

import android.graphics.PointF;
import android.view.View;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class AMapMesh extends AMapOverlay {

  private View parent = null;
  private SubMesh mesh = null;
  private LatLng position = null;
  private String rotateAxis = null;
  private float rotateAngle = 0.0f;
  private float[] scale = null;
  private String drawMode = null;
  private int backOrFront = 0;
  private boolean transparent = true;
  private boolean depthTest = true;
  private float[] points = null;
  private float[] colors = null;
  private int[] faces = null;
  private double[] valueData = null;
  private double[] range = {0, 1};
  private double[][] color = {new double[]{0, 0, 0}, new double[]{1, 1, 1}};
  private double opacity = 1;
  private static final Pattern RGB_PATTERN = Pattern.compile("^rgb\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)$");
  private static final Pattern HEX_PATTERN = Pattern.compile("^#[0-9a-f]{3}([0-9a-f]{3})?$");


  public AMapMesh(ThemedReactContext themedContext, ReactApplicationContext reactAppContext) {
    super(themedContext);
  }


  public void transparentEnabled(boolean b) {
    transparent = b;
    if (mesh != null) {
      mesh.transparentEnabled(b);
    }
  }


  public void depthTestEnabled(boolean b) {
    depthTest = b;
    if (mesh != null) {
      mesh.depthTestEnabled(b);
    }
  }


  public void setDrawMode(String dm) {
    drawMode = dm;
    if (mesh != null) {
      mesh.setDrawMode(dm);
    }
  }


  public void setBackOrFront(int bf) {
    backOrFront = bf;
    if (mesh != null) {
      mesh.setBackOrFront(bf);
    }
  }

  public void setPosition(LatLng p) {
    position = p;
    if (mesh != null) {
      mesh.setPosition(position);
    }
  }

  public void setRotate(String t, float a) {
    rotateAxis = t;
    rotateAngle = a;
    if (mesh != null) {
      mesh.setRotate(rotateAxis, rotateAngle);
    }
  }

  public void setScale(float[] s) {
    scale = s;
    if (mesh != null) {
      mesh.setScale(scale);
    }
  }

  public void setDataSource(ReadableMap dataSource) {
    setDataSource(parseMeshData(dataSource));
  }

  public void setRequest(ReadableMap request) {
    if (request != null) {
      String url = request.getString("url");
      if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
        new Thread(() -> setDataSource(doRequestData(url, request))).start();
      }
    }
  }

  public void setValueDomain(ReadableMap valueDomain) {
    if (valueDomain != null) {
      if (valueDomain.hasKey("opacity")) {
        opacity = valueDomain.getDouble("opacity");
      }
      ReadableArray colorArray = valueDomain.getArray("color");
      if (colorArray != null && colorArray.size() > 0) {
        int clen = colorArray.size();
        color = new double[clen][3];
        for (int k = 0; k < clen; k++) {
          color[k] = color2One(colorArray.getString(k));
        }
      }
      ReadableArray rangeArray = valueDomain.getArray("range");
      if (rangeArray != null && rangeArray.size() > 0) {
        int rlen = rangeArray.size();
        range = new double[rlen];
        for (int k = 0; k < rlen; k++) {
          range[k] = rangeArray.getDouble(k);
        }
      }
      float[] vertexColors = batchValueToColor(valueData, range, color, opacity);
      if (vertexColors != null) {
        colors = vertexColors;
        if (mesh != null) {
          mesh.setData(points, colors, faces);
        }
      }
    }
  }

  public Mesh getMesh() {
    return mesh;
  }

  @Override
  public void added(View view) {
    this.parent = view;
    MeshRenderer openglRender = ((AMapView) view).getOpenglRender();
    mesh = openglRender.addMesh();
    if (position != null) {
      mesh.setPosition(position);
    }
    if (rotateAxis != null) {
      mesh.setRotate(rotateAxis, rotateAngle);
    }
    if (scale != null) {
      mesh.setScale(scale);
    }
    if (drawMode != null) {
      mesh.setDrawMode(drawMode);
    }
    mesh.setBackOrFront(backOrFront);
    mesh.transparentEnabled(transparent);
    mesh.depthTestEnabled(depthTest);
    if (points != null && colors != null && faces != null) {
      mesh.setData(points, colors, faces);
      WritableMap event = Arguments.createMap();
      event.putString("type", "create");
      event.putMap("position", Types.latLngToMap(position));
      ((AMapView) parent).dispatchEvent(getId(), "onRendered", event);
    }
  }

  @Override
  public void removed(View view) {
    MeshRenderer openglRender = ((AMapView) view).getOpenglRender();
    openglRender.removeMesh(mesh);
    mesh = null;
  }


  private void setDataSource(HashMap<String, Object> meshData) {
    if (meshData != null) {
      WritableMap event = Arguments.createMap();
      if (meshData.get("message") != null) {
        // 触发渲染错误事件
        event.putString("type", "error");
        event.putString("message", "Render error, message: " + meshData.get("message"));
      } else {
        Object po = meshData.get("position");
        position = po != null ? (LatLng) po : position;
        points = (float[]) meshData.get("vertices");
        faces = (int[]) meshData.get("faces");
        valueData = (double[]) meshData.get("values");
        float[] vertexColors = batchValueToColor(valueData, range, color, opacity);
        colors = vertexColors != null ? vertexColors : (float[]) meshData.get("vertexColors");
        if (mesh != null) {
          if (position != null) mesh.setPosition(position);
          mesh.setData(points, colors, faces);
          // 触发渲染正确事件
          event.putString("type", "update");
          event.putMap("position", Types.latLngToMap(position));
          ((AMapView) parent).dispatchEvent(getId(), "onRendered", event);
        }
      }
    }
  }

  private double[] color2One(String color) {
    if (color == null || "".equals(color)) return new double[0];
    double[] colorOne = new double[3];
    if (HEX_PATTERN.matcher(color.toLowerCase()).matches()) { // 16进制
      char[] colorCharArray = color.toCharArray();
      if (colorCharArray.length == 4) {
        colorOne[0] = Math.max(0, Math.min(1, (double) Integer.parseInt(colorCharArray[1] + "" + colorCharArray[1], 16) / 255));
        colorOne[1] = Math.max(0, Math.min(1, (double) Integer.parseInt(colorCharArray[2] + "" + colorCharArray[2], 16) / 255));
        colorOne[2] = Math.max(0, Math.min(1, (double) Integer.parseInt(colorCharArray[3] + "" + colorCharArray[3], 16) / 255));
      } else if (colorCharArray.length == 7) {
        colorOne[0] = Math.max(0, Math.min(1, (double) Integer.parseInt(colorCharArray[1] + "" + colorCharArray[2], 16) / 255));
        colorOne[1] = Math.max(0, Math.min(1, (double) Integer.parseInt(colorCharArray[3] + "" + colorCharArray[4], 16) / 255));
        colorOne[2] = Math.max(0, Math.min(1, (double) Integer.parseInt(colorCharArray[5] + "" + colorCharArray[6], 16) / 255));
      }
    } else if (RGB_PATTERN.matcher(color.toLowerCase()).matches()) {// rgb
      String[] colorChar = color.replaceAll("\\s", "")
        .replaceAll("rgb\\(", "")
        .replaceAll("\\)", "")
        .split(",");
      colorOne[0] = Math.max(0, Math.min(1, (double) Integer.parseInt(colorChar[0]) / 255));
      colorOne[1] = Math.max(0, Math.min(1, (double) Integer.parseInt(colorChar[1]) / 255));
      colorOne[2] = Math.max(0, Math.min(1, (double) Integer.parseInt(colorChar[2]) / 255));
    }
    return colorOne;
  }

  private double[] valueToColor(double value, double[] range, double[][] color, double opacity) {
    int len = color.length;
    double[] tempNext = color[len - 1];
    if (value >= range[len - 1]) {
      return new double[]{tempNext[0], tempNext[1], tempNext[2], opacity};
    }
    double[] nColor = tempNext;
    for (int i = len - 2; i >= 0; i -= 1) {
      double[] prev = color[i];
      double[] next = tempNext;
      double pval = range[i];
      double nval = range[i + 1];
      // 如果有两种颜色对应的值一样，应该取一种颜色，默认取后面一个
      if (pval == nval) {
        prev = next;
      }
      if (value == pval) {
        nColor = prev;
        break;
      }
      if (value > pval) {
        double rate = (value - pval) / (nval - pval);
        nColor = new double[]{prev[0] + rate * (next[0] - prev[0]), prev[1] + rate * (next[1] - prev[1]), prev[2] + rate * (next[2] - prev[2])};
        break;
      }
      tempNext = prev;
    }
    if (value < range[0]) {
      return new double[]{tempNext[0], tempNext[1], tempNext[2], opacity};
    }
    return new double[]{nColor[0], nColor[1], nColor[2], opacity};
  }

  private float[] batchValueToColor(double[] values, double[] range, double[][] color, double opacity) {
    if (values == null) return null;
    if (values.length == 0) return new float[0];
    int length = values.length;
    float[] vertexColors = new float[4 * length];
    for (int i = 0; i < length; i++) {
      // 值转色
      double[] vc = valueToColor(valueData[i], range, color, opacity);
      vertexColors[4 * i] = (float) vc[0];// 颜色分量 red
      vertexColors[4 * i + 1] = (float) vc[1];// 颜色分量 green
      vertexColors[4 * i + 2] = (float) vc[2];// 颜色分量 blue
      vertexColors[4 * i + 3] = (float) vc[3];// 透明度 alpha
    }
    return vertexColors;
  }

  private HashMap<String, Object> parseMeshData2(ReadableMap data) {
    if (data == null) return null;
    float[] vertices = new float[0];
    float[] vertexColors = new float[0];
    int[] faces = new int[0];
    HashMap<String, Object> dataMap = new HashMap<>();
    try {
      ReadableArray verticesList = data.getArray("vertices");
      ReadableArray colorsList = data.getArray("vertexColors");
      ReadableArray facesList = data.getArray("faces");
      if (verticesList != null) {
        vertices = new float[verticesList.size()];
        for (int i = 0, len = verticesList.size(); i < len; i++) {
          vertices[i] = (float) verticesList.getDouble(i);
        }
      }
      if (colorsList != null) {
        vertexColors = new float[colorsList.size()];
        for (int i = 0, len = colorsList.size(); i < len; i++) {
          vertexColors[i] = (float) colorsList.getDouble(i);
        }
      }
      if (facesList != null) {
        faces = new int[facesList.size()];
        for (int i = 0, len = facesList.size(); i < len; i++) {
          faces[i] = facesList.getInt(i);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      dataMap.put("message", e.getMessage());
    }
    dataMap.put("vertices", vertices);
    dataMap.put("vertexColors", vertexColors);
    dataMap.put("faces", faces);
    return dataMap;
  }

  private HashMap<String, Object> parseMeshData(ReadableMap dataSource) {
    if (dataSource == null) return null;
    if (dataSource.hasKey("vertices")) {
      return parseMeshData2(dataSource);
    }
    HashMap<String, Object> dataMap = new HashMap<>();
    float[] vertices = new float[0];
    double[] values = new double[0];
    int[] faces = new int[0];
    LatLng position = null;
    try {
      // 天顶角（θ），从+z轴向-z轴方向旋转形成的夹角[0,π]，弧度制
      double baseZenith = dataSource.hasKey("zenith") ? dataSource.getDouble("zenith") : 0;
      // 方位角（φ），从+x轴向+y轴、-x轴，-y轴，+x轴方向旋转形成的夹角[0,2π]，弧度制
      double baseAzimuth = dataSource.hasKey("azimuth") ? dataSource.getDouble("azimuth") : 0;
      // 距离，当前点到原点距离，米单位
      double baseSpacing = dataSource.hasKey("spacing") ? dataSource.getDouble("spacing") : 0;
      // 坐标类型
      int coordType = dataSource.hasKey("coordType") ? dataSource.getInt("coordType") : -1;
      // 地图上的位置
      ReadableMap positionJson = dataSource.getMap("position");
      if (positionJson != null) {
        position = Types.coordinateConvert(coordType,
          new LatLng(positionJson.hasKey("latitude") ?
            positionJson.getDouble("latitude") : 0,
            positionJson.hasKey("longitude") ?
              positionJson.getDouble("longitude") : 0),
          getContext());
      }
      ReadableArray pointsJson = dataSource.getArray("point");
      if (pointsJson != null) {
        int size = pointsJson.size();
        if (size > 0) {
          ReadableMap pointJson0 = pointsJson.getMap(0);
          ReadableArray valuesJson0 = pointJson0.getArray("value");
          if (valuesJson0 == null) {
            throw new Exception("data error");
          }
          int length = valuesJson0.size();
          if (length > 0) {
            vertices = new float[size * length * 3];
            values = new double[size * length];
            faces = new int[(size - 1) * (length - 1) * 6];
            if (position == null) { // 取第一个元素中center作为position
              ReadableMap centerJson0 = pointJson0.getMap("center");
              if (centerJson0 != null) {
                position = Types.coordinateConvert(coordType,
                  new LatLng(centerJson0.hasKey("latitude") ?
                    centerJson0.getDouble("latitude") : 0,
                    centerJson0.hasKey("longitude") ?
                      centerJson0.getDouble("longitude") : 0),
                  getContext());
              } else {
                throw new Exception("data error");
              }
            }
            // 计算距离坐标和gl坐标比例
            Projection mapProject = ((AMapView) parent).getMap().getProjection();
            PointF glPositionPoint = mapProject.toOpenGLLocation(position);
            LatLng position2 = new LatLng(position.latitude + 0.0001f, position.longitude + 0.0001f);
            PointF glPositionPoint2 = mapProject.toOpenGLLocation(position2);
            float scale = (float) (Math.sqrt((Math.pow(glPositionPoint2.x - glPositionPoint.x, 2) + Math.pow(glPositionPoint2.y - glPositionPoint.y, 2))) / AMapUtils.calculateLineDistance(position, position2));
            // 循环解析数据
            for (int i = 0; i < size; i++) {
              ReadableMap pointJson = pointsJson.getMap(i);
              // 计算每条数据中心点与位置的偏移量
              ReadableMap centerJson = pointJson.getMap("center");
              LatLng center = position;
              if (centerJson != null) {
                center = Types.coordinateConvert(coordType,
                  new LatLng(centerJson.hasKey("latitude") ?
                    centerJson.getDouble("latitude") : 0,
                    centerJson.hasKey("longitude") ?
                      centerJson.getDouble("longitude") : 0),
                  getContext());
              }
              PointF glCenterPoint = mapProject.toOpenGLLocation(center);
              double[] translate = new double[]{(glCenterPoint.x - glPositionPoint.x) / scale, (glCenterPoint.y - glPositionPoint.y) / scale, 0};
              // 计算xyz比例
              double zenith = baseZenith;
              if (pointJson.hasKey("zenith")) {
                zenith = pointJson.getDouble("zenith");
              }
              double azimuth = baseAzimuth;
              if (pointJson.hasKey("azimuth")) {
                azimuth = pointJson.getDouble("azimuth");
              }
              double xRatio = +Math.sin(zenith) * Math.cos(azimuth);// 此处为正值，因为地图三维坐标x轴正方向是向东
              double yRatio = -Math.sin(zenith) * Math.sin(azimuth);// 此处为负值，因为地图三维坐标y轴正方向是向南而不是向北
              double zRatio = +Math.cos(zenith);// 此处为正值，因为地图三维坐标z轴正方向是向上(web端z轴正方向是向下)
              double spacing = baseSpacing;
              if (pointJson.hasKey("spacing")) {
                spacing = pointJson.getDouble("spacing");
              }
              ReadableArray valuesJson = pointJson.getArray("value");
              if (valuesJson == null) {
                throw new Exception("data error");
              }
              // 循环子点
              for (int j = 0; j < length; j++) {
                int index = i * length + j;
                double radius = j * spacing;
                // 球坐标转笛卡尔坐标
                double x = (radius * xRatio) + translate[0];// x坐标分量，米单位
                double y = (radius * yRatio) + translate[1];// y坐标分量，米单位
                double z = (radius * zRatio) + translate[2];// z坐标分量，米单位
                vertices[index * 3] = (float) x;
                vertices[index * 3 + 1] = (float) y;
                vertices[index * 3 + 2] = (float) z;
                values[index] = valuesJson.getDouble(j);
                if (i != 0 && j != 0) {
                  int rightTop = index;
                  int leftTop = rightTop - length;
                  int rightBottom = rightTop - 1;
                  int leftBottom = leftTop - 1;
                  index = (i - 1) * (length - 1) + (j - 1);
                  faces[index * 6] = rightTop;
                  faces[index * 6 + 1] = leftTop;
                  faces[index * 6 + 2] = leftBottom;
                  faces[index * 6 + 3] = leftBottom;
                  faces[index * 6 + 4] = rightTop;
                  faces[index * 6 + 5] = rightBottom;
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      dataMap.put("message", e.getMessage());
    }
    dataMap.put("vertices", vertices);
    dataMap.put("faces", faces);
    dataMap.put("values", values);
    dataMap.put("position", position);
    return dataMap;
  }

  private HashMap<String, Object> parseMeshData(String response, ReadableMap dataParse) {
    HashMap<String, Object> dataMap = new HashMap<>();
    float[] vertices = new float[0];
    float[] vertexColors = new float[0];
    double[] valueData = null;
    int[] faces = new int[0];
    LatLng position = null;
    try {
      // 解析数据的关键参数
      String dataKey = null;
      String pointMode = null;
      String valueMode = null;
      String centerMode = null;
      int coordType = -1; // 坐标类型
      if (dataParse != null) {
        dataKey = dataParse.getString("dataKey");
        centerMode = dataParse.getString("centerMode");
        pointMode = dataParse.getString("pointMode");
        valueMode = dataParse.getString("valueMode");
        if (dataParse.hasKey("coordType")) {
          coordType = dataParse.getInt("coordType");
        }
      }
      JSONObject jsonData = new JSONObject(response);
      if (dataKey != null && !"".equals(dataKey)) {
        String[] keyArray = dataKey.split("\\.");
        for (String s : keyArray) {
          if (jsonData == null) break;
          jsonData = jsonData.optJSONObject(s);
        }
      }
      if (jsonData == null) {
        throw new JSONException("No data");
      }
      // 图在地图上的位置
      JSONObject positionJson = jsonData.optJSONObject("position");
      if (positionJson != null) {
        position = Types.coordinateConvert(coordType,
          new LatLng(positionJson.optDouble("latitude"),
            positionJson.optDouble("longitude")),
          getContext());
      }
      JSONArray pointsJson = jsonData.optJSONArray("point");
      if (pointsJson != null && pointsJson.length() > 0) {
        int size = pointsJson.length();
        boolean isSpherical = "spherical".equals(pointMode);
        boolean isColor = "color".equals(valueMode);
        if ("multiple".equals(centerMode)) { // 多中心点
          ArrayList<Float> verticesList = new ArrayList<>();
          ArrayList<Float> vertexColorsList = new ArrayList<>();
          ArrayList<Double> valueDataList = new ArrayList<>();
          if (position == null) { // 取第一个元素中center作为position
            JSONObject pointJson0 = pointsJson.optJSONObject(0);
            JSONObject centerJson0 = pointJson0.optJSONObject("center");
            if (centerJson0 != null) {
              position = Types.coordinateConvert(coordType,
                new LatLng(centerJson0.optDouble("latitude"),
                  centerJson0.optDouble("longitude")),
                getContext());
            } else {
              throw new JSONException("data error");
            }
          }
          // 计算距离坐标和gl坐标比例
          Projection mapProject = ((AMapView) parent).getMap().getProjection();
          PointF glPositionPoint = mapProject.toOpenGLLocation(position);
          LatLng position2 = new LatLng(position.latitude + 0.0001f, position.longitude + 0.0001f);
          PointF glPositionPoint2 = mapProject.toOpenGLLocation(position2);
          float scale = (float) (Math.sqrt((Math.pow(glPositionPoint2.x - glPositionPoint.x, 2) + Math.pow(glPositionPoint2.y - glPositionPoint.y, 2))) / AMapUtils.calculateLineDistance(position, position2));
          // 循环解析数据
          for (int i = 0; i < size; i++) {
            JSONObject pointJson = pointsJson.optJSONObject(i);
            // 计算每条数据中心点与位置的偏移量
            JSONObject centerJson = pointJson.optJSONObject("center");
            LatLng center = position;
            if (centerJson != null) {
              center = Types.coordinateConvert(coordType,
                new LatLng(centerJson.optDouble("latitude"),
                  centerJson.optDouble("longitude")),
                getContext());
            }
            PointF glCenterPoint = mapProject.toOpenGLLocation(center);
            double[] translate = new double[]{(glCenterPoint.x - glPositionPoint.x) / scale, (glCenterPoint.y - glPositionPoint.y) / scale, 0};
            // 循环子数据点
            JSONArray childrenJson = pointJson.optJSONArray("children");
            if (childrenJson != null && childrenJson.length() > 0) {
              for (int j = 0, length = childrenJson.length(); j < length; j++) {
                JSONObject childJson = childrenJson.optJSONObject(j);
                double x;// x坐标分量，米单位
                double y;// y坐标分量，米单位
                double z;// z坐标分量，米单位
                if (isSpherical) {// 球坐标
                  double radius = childJson.optDouble("d");// 距离，当前点到原点距离，米单位
                  double theta = childJson.optDouble("t");// 天顶角（θ），从+z轴向-z轴方向旋转形成的夹角[0,π]，弧度制
                  double phi = childJson.optDouble("p");// 方位角（φ），从+x轴向+y轴、-x轴，-y轴，+x轴方向旋转形成的夹角[0,2π]，弧度制
                  // 球坐标转笛卡尔坐标
                  x = +radius * Math.sin(theta) * Math.cos(phi);// 此处为正值，因为地图三维坐标x轴正方向是向东
                  y = -radius * Math.sin(theta) * Math.sin(phi);// 此处为负值，因为地图三维坐标y轴正方向是向南而不是向北
                  z = +radius * Math.cos(theta);// 此处为正值，因为地图三维坐标z轴正方向是向上(web端z轴正方向是向下)
                } else {// 笛卡尔坐标
                  x = childJson.optDouble("x");
                  y = childJson.optDouble("y");
                  z = childJson.optDouble("z");
                }
                verticesList.add((float) (x + translate[0]));
                verticesList.add((float) (y + translate[1]));
                verticesList.add((float) (z + translate[2]));
                if (isColor) {
                  vertexColorsList.add((float) (childJson.optDouble("r") / 255));// 颜色分量 red
                  vertexColorsList.add((float) (childJson.optDouble("g") / 255));// 颜色分量 green
                  vertexColorsList.add((float) (childJson.optDouble("b") / 255));// 颜色分量 blue
                  vertexColorsList.add((float) (childJson.optDouble("a") / 255));// 透明度 alpha
                } else {
                  valueDataList.add(childJson.optDouble("v"));
                }
              }
            }
          }
          vertices = new float[verticesList.size()];
          int index = 0;
          for (Float v : verticesList) {
            vertices[index++] = v;
          }
          if (isColor) {
            vertexColors = new float[vertexColorsList.size()];
            index = 0;
            for (Float v : vertexColorsList) {
              vertexColors[index++] = v;
            }
          } else {
            valueData = new double[valueDataList.size()];
            index = 0;
            for (Double v : valueDataList) {
              valueData[index++] = v;
            }
          }
        } else {// 单中心点
          vertices = new float[3 * size];
          if (isColor) {
            vertexColors = new float[4 * size];
          } else {
            valueData = new double[size];
          }
          for (int i = 0; i < size; i++) {
            JSONObject pointJson = pointsJson.optJSONObject(i);
            double x;// x坐标分量，米单位
            double y;// y坐标分量，米单位
            double z;// z坐标分量，米单位
            if (isSpherical) {// 球坐标
              double radius = pointJson.optDouble("d");// 距离，当前点到原点距离，米单位
              double theta = pointJson.optDouble("t");// 天顶角（θ），从+z轴向-z轴方向旋转形成的夹角[0,π]，弧度制
              double phi = pointJson.optDouble("p");// 方位角（φ），从+x轴向+y轴、-x轴，-y轴，+x轴方向旋转形成的夹角[0,2π]，弧度制
              // 球坐标转笛卡尔坐标
              x = +radius * Math.sin(theta) * Math.cos(phi);// 此处为正值，因为地图三维坐标x轴正方向是向东
              y = -radius * Math.sin(theta) * Math.sin(phi);// 此处为负值，因为地图三维坐标y轴正方向是向南而不是向北
              z = +radius * Math.cos(theta);// 此处为正值，因为地图三维坐标z轴正方向是向上(web端z轴正方向是向下)
            } else {// 笛卡尔坐标
              x = pointJson.optDouble("x");
              y = pointJson.optDouble("y");
              z = pointJson.optDouble("z");
            }
            vertices[3 * i] = (float) x;
            vertices[3 * i + 1] = (float) y;
            vertices[3 * i + 2] = (float) z;
            if (isColor) {
              vertexColors[4 * i] = (float) (pointJson.optDouble("r") / 255);// 颜色分量 red
              vertexColors[4 * i + 1] = (float) (pointJson.optDouble("g") / 255);// 颜色分量 green
              vertexColors[4 * i + 2] = (float) (pointJson.optDouble("b") / 255);// 颜色分量 blue
              vertexColors[4 * i + 3] = (float) (pointJson.optDouble("a") / 255);// 透明度 alpha
            } else {
              valueData[i] = pointJson.optDouble("v");
            }
          }
        }
      }
      // 绘制点时每一个面的点序号（数组下标）
      JSONArray facesJson = jsonData.optJSONArray("faces");
      if (facesJson != null && facesJson.length() > 0) {
        int length = facesJson.length();
        faces = new int[length];
        for (int i = 0; i < length; i++) {
          faces[i] = facesJson.optInt(i);
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
      dataMap.put("message", e.getMessage());
    }
    dataMap.put("vertices", vertices);
    dataMap.put("vertexColors", vertexColors);
    dataMap.put("faces", faces);
    dataMap.put("values", valueData);
    dataMap.put("position", position);
    return dataMap;
  }

  private Object transformToJSON(Object value) throws JSONException {
    Object json = value;
    if (value instanceof ReadableMap) {
      json = new JSONObject();
      Iterator<Map.Entry<String, Object>> iterator = ((ReadableMap) value).getEntryIterator();
      while (iterator.hasNext()) {
        Map.Entry<String, Object> next = iterator.next();
        String k = next.getKey();
        Object v = next.getValue();
        ((JSONObject) json).put(k, transformToJSON(v));
      }
    } else if (value instanceof ReadableArray) {
      json = new JSONArray();
      for (Object v : ((ReadableArray) value).toArrayList()) {
        ((JSONArray) json).put(transformToJSON(v));
      }
    }
    return json;
  }

  private HashMap<String, Object> doRequestData(String url, ReadableMap request) {
    HashMap<String, Object> dataMap = new HashMap<>();
    HttpURLConnection connection = null;
    InputStream inputStream = null;
    ByteArrayOutputStream byteArrayOut = null;
    try {
      String method = request.getString("method");
      if (method != null) {
        method = method.toUpperCase(Locale.ROOT).equals("POST") ? "POST" : "GET";
      }
      String type = request.getString("type");
      String dataString = null;
      ReadableMap data = request.getMap("data");
      if (data != null) {
        if ("GET".equals(method) || "form".equals(type)) {
          StringBuilder builder = new StringBuilder();
          Iterator<Map.Entry<String, Object>> iterator = data.getEntryIterator();
          while (iterator.hasNext()) {
            Map.Entry<String, Object> next = iterator.next();
            String k = next.getKey();
            Object v = next.getValue();
            builder.append("&").append(k).append("=").append(URLEncoder.encode(v.toString(), "UTF-8"));
          }
          dataString = builder.substring(1);
        } else {
          dataString = URLEncoder.encode((transformToJSON(data)).toString(), "UTF-8");
        }
      }
      if ("GET".equals(method) && dataString != null) {
        url = url + (!url.contains("?") ? "?" : "&") + dataString;
      }
      int timeout = request.hasKey("timeout") && request.getInt("timeout") > 0 ? request.getInt("timeout") : 10 * 1000;
      connection = (HttpURLConnection) (new URL(url).openConnection());
      connection.setRequestMethod(method);
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      connection.setRequestProperty("Content-Type", ("form".equals(type) ? "application/x-www-form-urlencoded" : "application/json") + ";charset=UTF-8");
      connection.setRequestProperty("Accept", "application/json");
      ReadableMap headers = request.getMap("headers");
      if (headers != null) {
        Iterator<Map.Entry<String, Object>> iteratorHeaders = headers.getEntryIterator();
        while (iteratorHeaders.hasNext()) {
          Map.Entry<String, Object> next = iteratorHeaders.next();
          String key = next.getKey();
          Object value = next.getValue();
          if (value instanceof ReadableArray) {
            ReadableArray valueArray = (ReadableArray) value;
            for (int i = 0, len = valueArray.size(); i < len; i++) {
              connection.addRequestProperty(key, valueArray.getString(i));
            }
          } else {
            connection.setRequestProperty(key, (String) value);
          }
        }
      }
      // 开启读，可以调用getInputStream后读取数据
      connection.setDoInput(true);
      if ("POST".equals(method)) {
        // 开启写，可以调用getOutputStream后写入数据
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        if (dataString != null) {
          OutputStream out = connection.getOutputStream();
          out.write(dataString.getBytes());
          out.flush();
        }
      }
      int code = connection.getResponseCode();
      if (code == 200) {
        inputStream = connection.getInputStream();
        byteArrayOut = new ByteArrayOutputStream();
        int len;
        byte[] buffer = new byte[1024];
        while ((len = inputStream.read(buffer)) != -1) {
          byteArrayOut.write(buffer, 0, len);
        }
        byteArrayOut.flush();
        String result = byteArrayOut.toString("UTF-8");
        return parseMeshData(result, request.getMap("dataParse"));
      } else {
        dataMap.put("message", "Request failed, http code: " + code);
      }
    } catch (JSONException | IOException e) {
      e.printStackTrace();
      dataMap.put("message", e.getMessage());
    } finally {
      try {
        if (byteArrayOut != null) byteArrayOut.close();
        if (inputStream != null) inputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (connection != null) connection.disconnect();
    }
    return dataMap;
  }
}
