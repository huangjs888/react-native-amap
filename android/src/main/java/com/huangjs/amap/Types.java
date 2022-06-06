package com.huangjs.amap;

import android.graphics.Point;
import android.location.Location;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

public class Types {
  public final static int[] MAP_TYPES = new int[]{
    AMap.MAP_TYPE_NORMAL,
    AMap.MAP_TYPE_SATELLITE,
    AMap.MAP_TYPE_NIGHT,
    AMap.MAP_TYPE_NAVI,
    AMap.MAP_TYPE_BUS
  };

  public final static CoordinateConverter.CoordType[] COORDINATE_TYPES = new CoordinateConverter.CoordType[]{
    CoordinateConverter.CoordType.ALIYUN,
    CoordinateConverter.CoordType.BAIDU,
    CoordinateConverter.CoordType.GOOGLE,
    CoordinateConverter.CoordType.GPS,
    CoordinateConverter.CoordType.MAPABC,
    CoordinateConverter.CoordType.MAPBAR,
    CoordinateConverter.CoordType.SOSOMAP
  };

  public static WritableMap latLngToMap(LatLng latLng) {
    if (latLng == null) return null;
    WritableMap map = Arguments.createMap();
    map.putDouble("latitude", latLng.latitude);
    map.putDouble("longitude", latLng.longitude);
    return map;
  }

  public static LatLng mapToLatLng(ReadableMap map) {
    if (map == null) return null;
    return new LatLng(map.getDouble("latitude"), map.getDouble("longitude"));
  }

  public static WritableMap pointToMap(Point point) {
    if (point == null) return null;
    WritableMap map = Arguments.createMap();
    map.putInt("x", point.x);
    map.putInt("y", point.y);
    return map;
  }

  public static Point mapToPoint(ReadableMap map) {
    if (map == null) return null;
    return new Point(map.getInt("x"), map.getInt("y"));
  }

  public static WritableMap latLngBoundsToMap(LatLngBounds latLngBounds) {
    if (latLngBounds == null) return null;
    WritableMap map = Arguments.createMap();
    map.putMap("southwest", latLngToMap(latLngBounds.southwest));
    map.putMap("northeast", latLngToMap(latLngBounds.northeast));
    return map;
  }

  public static LatLngBounds mapToLatLngBounds(ReadableMap map) {
    if (map == null) return null;
    return new LatLngBounds(mapToLatLng(map.getMap("southwest")), mapToLatLng(map.getMap("northeast")));
  }

  public static WritableMap cameraPositionToMap(CameraPosition cameraPosition) {
    if (cameraPosition == null) return null;
    WritableMap map = Arguments.createMap();
    map.putDouble("rotate", cameraPosition.bearing);
    map.putDouble("pitch", cameraPosition.tilt);
    map.putDouble("zoom", cameraPosition.zoom);
    map.putMap("center", latLngToMap(cameraPosition.target));
    return map;
  }

  public static CameraPosition mapToCameraPosition(CameraPosition initCameraPosition, ReadableMap map) {
    CameraPosition.Builder builder = new CameraPosition.Builder(initCameraPosition);
    if (map != null) {
      // 使用hasKey判断，代表只修改部分参数
      if (map.hasKey("zoom")) {
        builder.zoom((float) map.getDouble("zoom"));
      }
      if (map.hasKey("rotate")) {
        builder.bearing((float) map.getDouble("rotate"));
      }
      if (map.hasKey("pitch")) {
        builder.tilt((float) map.getDouble("pitch"));
      }
      if (map.hasKey("center")) {
        builder.target(mapToLatLng(map.getMap("center")));
      }
    }
    return builder.build();
  }

  public static WritableMap locationToMap(Location location) {
    if (location == null) return null;
    WritableMap map = Arguments.createMap();
    map.putDouble("latitude", location.getLatitude());
    map.putDouble("longitude", location.getLongitude());
    map.putDouble("altitude", location.getAltitude());
    map.putDouble("accuracy", location.getAccuracy());
    map.putDouble("speed", location.getSpeed());
    map.putDouble("heading", location.getBearing());
    map.putDouble("timestamp", location.getTime());
    return map;
  }
}
