package com.huangjs.amap;

import android.graphics.PointF;
import android.view.View;

import com.amap.api.maps.AMapUtils;
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
  private static Pattern RGB_PATTERN = Pattern.compile("^rgb\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)$");
  private static Pattern HEX_PATTERN = Pattern.compile("^#[0-9a-f]{3}([0-9a-f]{3})?$");


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
    HashMap<String, Object> meshData = parseMeshData(dataSource);
    if (meshData != null) {
      points = (float[]) meshData.get("vertices");
      colors = (float[]) meshData.get("vertexColors");
      faces = (int[]) meshData.get("faces");
      if (mesh != null) {
        mesh.setData(points, colors, faces);
      }
    }
  }

  public void setRequest(ReadableMap request) {
    if (request != null) {
      String url = request.getString("url");
      if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            HashMap<String, Object> requestData = doRequestData(url, request);
            if (requestData != null) {
              position = (LatLng) requestData.get("position");
              points = (float[]) requestData.get("vertices");
              faces = (int[]) requestData.get("faces");
              valueData = (double[]) requestData.get("valueData");
              float[] vertexColors = batchValueToColor(valueData, range, color, opacity);
              colors = vertexColors != null ? vertexColors : (float[]) requestData.get("vertexColors");
              if (mesh != null) {
                mesh.setPosition(position);
                mesh.setData(points, colors, faces);
              }
            }
          }
        }).start();
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
    }
  }

  @Override
  public void removed(View view) {
    MeshRenderer openglRender = ((AMapView) view).getOpenglRender();
    openglRender.removeMesh(mesh);
    mesh = null;
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
    float[] vertexColors = null;
    if (values != null && values.length > 0) {
      int length = values.length;
      vertexColors = new float[4 * length];
      for (int i = 0; i < length; i++) {
        // 值转色
        double[] vc = valueToColor(valueData[i], range, color, opacity);
        vertexColors[4 * i] = (float) vc[0];// 颜色分量 red
        vertexColors[4 * i + 1] = (float) vc[1];// 颜色分量 green
        vertexColors[4 * i + 2] = (float) vc[2];// 颜色分量 blue
        vertexColors[4 * i + 3] = (float) vc[3];// 透明度 alpha
      }
    }
    return vertexColors;
  }

  private HashMap<String, Object> parseMeshData(String response, ReadableMap dataParse) {
    HashMap<String, Object> dataMap = new HashMap<>();
    try {
      String dataKey = null;
      String pointMode = null;
      String valueMode = null;
      String centerMode = null;
      int coordType = -1;
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
      if (jsonData != null && dataKey != null && !"".equals(dataKey)) {
        String[] keyArray = dataKey.split("\\.");
        for (int i = 0, klen = keyArray.length; i < klen; i++) {
          jsonData = jsonData.optJSONObject(keyArray[i]);
        }
      }
      if (jsonData == null) {
        throw new JSONException("No data");
      }
      // 图在地图上的位置
      LatLng position = new LatLng(0, 0);
      JSONObject positionJson = jsonData.getJSONObject("position");
      if (positionJson != null) {
        position = Types.coordinateConvert(coordType, new LatLng(positionJson.getDouble("latitude"), positionJson.getDouble("longitude")), getContext());
      }
      // 需要绘制的所有点坐标和颜色信息
      float[] vertices = new float[0];
      float[] vertexColors = new float[0];
      double[] valueData = null;
      JSONArray pointsJson = jsonData.getJSONArray("point");
      if (pointsJson != null && pointsJson.length() > 0) {
        int length = pointsJson.length();
        boolean isSpherical = "spherical".equals(pointMode);
        boolean isColor = "color".equals(valueMode);
        if ("multiple".equals(centerMode)) {// 多中心点
          ArrayList<Float> verticesList = new ArrayList<>();
          ArrayList<Float> vertexColorsList = new ArrayList<>();
          ArrayList<Double> valueDataList = new ArrayList<>();
          PointF glPositionPoint = ((AMapView) parent).getMap().getProjection().toOpenGLLocation(position);
          LatLng position2 = new LatLng(position.latitude + 0.0001f, position.longitude + 0.0001f);
          PointF glPositionPoint2 = ((AMapView) parent).getMap().getProjection().toOpenGLLocation(position2);
          // 计算距离坐标和gl坐标比例
          float scale = (float) (Math.sqrt((Math.pow(glPositionPoint2.x - glPositionPoint.x, 2) + Math.pow(glPositionPoint2.y - glPositionPoint.y, 2))) / AMapUtils.calculateLineDistance(position, position2));
          for (int i = 0; i < length; i++) {
            JSONObject pointJson = pointsJson.getJSONObject(i);
            JSONObject centerJson = pointJson.getJSONObject("center");
            LatLng center = Types.coordinateConvert(coordType, new LatLng(centerJson.getDouble("latitude"), centerJson.getDouble("longitude")), getContext());
            PointF glCenterPoint = ((AMapView) parent).getMap().getProjection().toOpenGLLocation(center);
            double[] translate = new double[]{(glCenterPoint.x - glPositionPoint.x) / scale, (glCenterPoint.y - glPositionPoint.y) / scale, 0};
            JSONArray childrenJson = pointJson.getJSONArray("children");
            for (int j = 0, len = childrenJson.length(); j < len; j++) {
              JSONObject childJson = childrenJson.getJSONObject(j);
              double x = 0;// x坐标分量，米单位
              double y = 0;// y坐标分量，米单位
              double z = 0;// z坐标分量，米单位
              if (isSpherical) {// 球坐标
                double radius = childJson.getDouble("d");// 距离，当前点到原点距离，米单位
                double theta = childJson.getDouble("t");// 天顶角（θ），从+z轴向-z轴方向旋转形成的夹角[0,π]，弧度制
                double phi = childJson.getDouble("p");// 方位角（φ），从+x轴向+y轴、-x轴，-y轴，+x轴方向旋转形成的夹角[0,2π]，弧度制
                // 球坐标转笛卡尔坐标
                x = +radius * Math.sin(theta) * Math.cos(phi);// 此处为正值，因为地图三维坐标x轴正方向是向东
                y = -radius * Math.sin(theta) * Math.sin(phi);// 此处为负值，因为地图三维坐标y轴正方向是向南而不是向北
                z = +radius * Math.cos(theta);// 此处为正值，因为地图三维坐标z轴正方向是向上(web端z轴正方向是向下)
              } else {// 笛卡尔坐标
                x = childJson.getDouble("x");
                y = childJson.getDouble("y");
                z = childJson.getDouble("z");
              }
              verticesList.add((float) (x + translate[0]));
              verticesList.add((float) (y + translate[1]));
              verticesList.add((float) (z + translate[2]));
              if (isColor) {
                vertexColorsList.add((float) (childJson.getDouble("r") / 255));// 颜色分量 red
                vertexColorsList.add((float) (childJson.getDouble("g") / 255));// 颜色分量 green
                vertexColorsList.add((float) (childJson.getDouble("b") / 255));// 颜色分量 blue
                vertexColorsList.add((float) (childJson.getDouble("a") / 255));// 透明度 alpha
              } else {
                valueDataList.add(childJson.getDouble("v"));
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
          vertices = new float[3 * length];
          if (isColor) {
            vertexColors = new float[4 * length];
          } else {
            valueData = new double[length];
          }
          for (int i = 0; i < length; i++) {
            JSONObject pointJson = pointsJson.getJSONObject(i);
            double x = 0;// x坐标分量，米单位
            double y = 0;// y坐标分量，米单位
            double z = 0;// z坐标分量，米单位
            if (isSpherical) {// 球坐标
              double radius = pointJson.getDouble("d");// 距离，当前点到原点距离，米单位
              double theta = pointJson.getDouble("t");// 天顶角（θ），从+z轴向-z轴方向旋转形成的夹角[0,π]，弧度制
              double phi = pointJson.getDouble("p");// 方位角（φ），从+x轴向+y轴、-x轴，-y轴，+x轴方向旋转形成的夹角[0,2π]，弧度制
              // 球坐标转笛卡尔坐标
              x = +radius * Math.sin(theta) * Math.cos(phi);// 此处为正值，因为地图三维坐标x轴正方向是向东
              y = -radius * Math.sin(theta) * Math.sin(phi);// 此处为负值，因为地图三维坐标y轴正方向是向南而不是向北
              z = +radius * Math.cos(theta);// 此处为正值，因为地图三维坐标z轴正方向是向上(web端z轴正方向是向下)
            } else {// 笛卡尔坐标
              x = pointJson.getDouble("x");
              y = pointJson.getDouble("y");
              z = pointJson.getDouble("z");
            }
            vertices[3 * i] = (float) x;
            vertices[3 * i + 1] = (float) y;
            vertices[3 * i + 2] = (float) z;
            if (isColor) {
              vertexColors[4 * i] = (float) (pointJson.getDouble("r") / 255);// 颜色分量 red
              vertexColors[4 * i + 1] = (float) (pointJson.getDouble("g") / 255);// 颜色分量 green
              vertexColors[4 * i + 2] = (float) (pointJson.getDouble("b") / 255);// 颜色分量 blue
              vertexColors[4 * i + 3] = (float) (pointJson.getDouble("a") / 255);// 透明度 alpha
            } else {
              valueData[i] = pointJson.getDouble("v");
            }
          }
        }
      }
      // 绘制点时每一个面的点序号（数组下标）
      int[] faces = new int[0];
      JSONArray facesJson = jsonData.getJSONArray("faces");
      if (facesJson != null && facesJson.length() > 0) {
        int length = facesJson.length();
        faces = new int[length];
        for (int i = 0; i < length; i++) {
          faces[i] = facesJson.getInt(i);
        }
      }
      dataMap.put("vertices", vertices);
      dataMap.put("vertexColors", vertexColors);
      dataMap.put("faces", faces);
      dataMap.put("valueData", valueData);
      dataMap.put("position", position);
    } catch (JSONException e) {
      e.printStackTrace();
      dataMap.put("message", e.getMessage());
    }
    return dataMap;
  }

  private HashMap<String, Object> parseMeshData(ReadableMap data) {
    if (data == null) return null;
    ReadableArray verticesList = data.getArray("vertices");
    ReadableArray colorsList = data.getArray("vertexColors");
    ReadableArray facesList = data.getArray("faces");
    float[] vertices = new float[0];
    if (verticesList != null) {
      vertices = new float[verticesList.size()];
      for (int i = 0, len = verticesList.size(); i < len; i++) {
        vertices[i] = (float) verticesList.getDouble(i);
      }
    }
    float[] vertexColors = new float[0];
    if (colorsList != null) {
      vertexColors = new float[colorsList.size()];
      for (int i = 0, len = colorsList.size(); i < len; i++) {
        vertexColors[i] = (float) colorsList.getDouble(i);
      }
    }
    int[] faces = new int[0];
    if (facesList != null) {
      faces = new int[facesList.size()];
      for (int i = 0, len = facesList.size(); i < len; i++) {
        faces[i] = facesList.getInt(i);
      }
    }
    HashMap<String, Object> dataMap = new HashMap<>();
    dataMap.put("vertices", vertices);
    dataMap.put("vertexColors", vertexColors);
    dataMap.put("faces", faces);
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
      Iterator<Object> iterator = ((ReadableArray) value).toArrayList().iterator();
      while (iterator.hasNext()) {
        Object v = iterator.next();
        ((JSONArray) json).put(transformToJSON(v));
      }
    }
    return json;
  }

  private HashMap<String, Object> doRequestData(String url, ReadableMap request) {
    HttpURLConnection connection = null;
    InputStream inputStream = null;
    ByteArrayOutputStream byteArrayOut = null;
    try {
      String method = request.getString("method") != null && request.getString("method").toUpperCase(Locale.ROOT).equals("POST") ? "POST" : "GET";
      String type = request.getString("type");
      String dataString = null;
      ReadableMap data = request.getMap("data");
      if (data != null) {
        if (method.equals("GET") || "form".equals(type)) {
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
          dataString = URLEncoder.encode(((JSONObject) transformToJSON(data)).toString(), "UTF-8");
        }
      }
      if (method.equals("GET") && dataString != null) {
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
      if (method.equals("POST")) {
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
      WritableMap event = Arguments.createMap();
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
        HashMap<String, Object> dataMap = parseMeshData(result, request.getMap("dataParse"));
        if (dataMap.get("message") != null) {
          // 触发错误事件
          event.putString("error", "Response data is null or error, message: " + dataMap.get("message"));
          ((AMapView) parent).dispatchEvent(getId(), "onRequested", event);
        } else {
          // 触发正确事件
          WritableMap positionMap = Arguments.createMap();
          event.putMap("position", Types.latLngToMap((LatLng) dataMap.get("position")));
          ((AMapView) parent).dispatchEvent(getId(), "onRequested", event);
          return dataMap;
        }
      } else {
        // 触发错误事件
        event.putString("error", "Request failed, http code: " + code);
        ((AMapView) parent).dispatchEvent(getId(), "onRequested", event);
      }
    } catch (JSONException | IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (byteArrayOut != null) byteArrayOut.close();
        if (inputStream != null) inputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (connection != null) connection.disconnect();
    }
    return null;
  }
}
