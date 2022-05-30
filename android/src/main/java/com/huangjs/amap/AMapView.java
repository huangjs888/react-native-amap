package com.huangjs.amap;

import android.location.Location;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AMapView extends MapView {
    private final Map<Mesh, AMapMesh> meshMap = new HashMap<>();
    private final Map<Marker, AMapMarker> markerMap = new HashMap<>();
    private RCTEventEmitter rctEventEmitter = null;
    private boolean isInitialCameraPositionSet = false;
    private MeshRenderer openglRender = null;
    private boolean openglEvent = false;

    public AMapView(ThemedReactContext themedContext, ReactApplicationContext reactAppContext) {
        super(themedContext);
        super.onCreate(null);
        AMap map = getMap();
        // 定位一次，且将视角移动到地图中心点。
        MyLocationStyle style = new MyLocationStyle();
        style.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
        getMap().setMyLocationStyle(style);
        map.setOnMapLoadedListener(() -> dispatchEvent("onLoaded", Arguments.createMap()));
        map.setOnMyLocationChangeListener((Location location) -> dispatchEvent("onLocationChange", Types.locationToMap(location)));
        map.setOnMapClickListener((LatLng latLng) -> {
            dispatchEvent("onClick", positionToMap(map, latLng));
            // 注册mesh点击事件
            if (openglEvent) {
                pushMeshInfoEvent("onClick", latLng, null);
            }
        });
        map.setOnMapLongClickListener((LatLng latLng) -> {
            dispatchEvent("onLongClick", positionToMap(map, latLng));
            // 注册mesh点击事件
            if (openglEvent) {
                pushMeshInfoEvent("onLongClick", latLng, null);
            }
        });
        map.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                WritableMap event = Arguments.createMap();
                event.putMap("latLngBounds", Types.latLngBoundsToMap(map.getProjection().getVisibleRegion().latLngBounds));
                event.putMap("cameraPosition", Types.cameraPositionToMap(cameraPosition));
                dispatchEvent("onCameraMoving", event);
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                WritableMap event = Arguments.createMap();
                event.putMap("latLngBounds", Types.latLngBoundsToMap(map.getProjection().getVisibleRegion().latLngBounds));
                event.putMap("cameraPosition", Types.cameraPositionToMap(cameraPosition));
                dispatchEvent("onCameraChange", event);

            }
        });
        map.setInfoWindowAdapter(new AMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                AMapMarker view = markerMap.get(marker);
                return view == null ? null : view.getInfoWindow();
            }

            @Override
            public View getInfoContents(Marker marker) {
                AMapMarker view = markerMap.get(marker);
                return view == null ? null : view.getInfoContents();
            }
        });
        map.setOnInfoWindowClickListener((Marker marker) -> {
            AMapMarker view = markerMap.get(marker);
            if (view != null)
                dispatchEvent(view.getId(), "onInfoWindowClick", positionToMap(map, marker.getPosition()));

        });
        map.setOnMarkerClickListener((Marker marker) -> {
            AMapMarker view = markerMap.get(marker);
            if (view != null)
                dispatchEvent(view.getId(), "onClick", positionToMap(map, marker.getPosition()));
            return false;
        });
        map.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                AMapMarker view = markerMap.get(marker);
                if (view != null)
                    dispatchEvent(view.getId(), "onDragStart", positionToMap(map, marker.getPosition()));
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                AMapMarker view = markerMap.get(marker);
                if (view != null)
                    dispatchEvent(view.getId(), "onDrag", positionToMap(map, marker.getPosition()));
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                AMapMarker view = markerMap.get(marker);
                if (view != null)
                    dispatchEvent(view.getId(), "onDragEnd", positionToMap(map, marker.getPosition()));
            }
        });
    }

    public void addOverlay(View view) {
        if (view instanceof AMapOverlay) {
            ((AMapOverlay) view).added(this);
            if (view instanceof AMapMarker) {
                markerMap.put(((AMapMarker) view).getMarker(), (AMapMarker) view);
            } else if (view instanceof AMapMesh) {
                meshMap.put(((AMapMesh) view).getMesh(), (AMapMesh) view);
            }
        }
    }

    public void removeOverlay(View view) {
        if (view instanceof AMapOverlay) {
            ((AMapOverlay) view).removed(this);
            if (view instanceof AMapMarker) {
                markerMap.remove(((AMapMarker) view).getMarker());
            } else if (view instanceof AMapMesh) {
                meshMap.remove(((AMapMesh) view).getMesh());
            }
        }
    }

    public void destroy() {
        meshMap.clear();
        markerMap.clear();
        openglRender.clearMesh();
        openglRender = null;
    }

    public MeshRenderer getOpenglRender() {
        if (openglRender == null) {
            AMap map = getMap();
            openglRender = new MeshRenderer(map);
            map.setCustomRenderer(openglRender);
        }
        return openglRender;
    }

    public void setMyLocationEnabled(boolean enable) {
        getMap().setMyLocationEnabled(enable);
    }

    public void setMyLocationIconEnabled(boolean enable) {
        MyLocationStyle style = new MyLocationStyle();
        // true:连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动,false:定位一次，且将视角移动到地图中心点。
        style.myLocationType(enable ? MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER : MyLocationStyle.LOCATION_TYPE_LOCATE);
        // 连续定位间隔时间,true生效，false失效
        style.interval(2000);
        // 显示定位蓝点
        style.showMyLocation(enable);
        getMap().setMyLocationStyle(style);
    }

    public void setOpenglEvent(boolean enable) {
        openglEvent = enable;
    }

    public void setInitialCameraPosition(ReadableMap initialCameraPosition) {
        if (!isInitialCameraPositionSet) {
            setCameraPosition(initialCameraPosition);
            isInitialCameraPositionSet = true;
        }
    }

    public void setCameraPosition(ReadableMap cameraPosition) {
        animateCameraPosition(cameraPosition, -1);
    }

    public void animateCameraPosition(ReadableMap cameraPosition, int duration) {
        if (cameraPosition == null) return;
        AMap map = getMap();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(Types.mapToCameraPosition(map.getCameraPosition(), cameraPosition));
        if (duration > 0) {
            map.animateCamera(cameraUpdate, duration, null);
        } else {
            map.moveCamera(cameraUpdate);
        }
    }

    public void pointToCoordinate(ReadableMap point) {
        if (point == null) return;
        WritableMap event = Arguments.createMap();
        event.putString("trigger", "pointToCoordinate");
        event.putMap("latLng", Types.latLngToMap(getMap().getProjection().fromScreenLocation(Types.mapToPoint(point))));
        WritableMap position = Arguments.createMap();
        position.putDouble("x", point.getDouble("x"));
        position.putDouble("y", point.getDouble("y"));
        event.putMap("point", position);
        dispatchEvent("onPointToCoordinate", event);
    }

    public void coordinateToPoint(ReadableMap coordinate) {
        if (coordinate == null) return;
        WritableMap event = Arguments.createMap();
        event.putString("trigger", "coordinateToPoint");
        event.putMap("point", Types.pointToMap(getMap().getProjection().toScreenLocation(Types.mapToLatLng(coordinate))));
        WritableMap position = Arguments.createMap();
        position.putDouble("latitude", coordinate.getDouble("latitude"));
        position.putDouble("longitude", coordinate.getDouble("longitude"));
        event.putMap("latLng", position);
        dispatchEvent("onCoordinateToPoint", event);
    }

    public void getCameraPosition() {
        WritableMap event = Arguments.createMap();
        event.putString("trigger", "getCameraPosition");
        event.putMap("latLngBounds", Types.latLngBoundsToMap(getMap().getProjection().getVisibleRegion().latLngBounds));
        event.putMap("cameraPosition", Types.cameraPositionToMap(getMap().getCameraPosition()));
        dispatchEvent("onGetCameraPosition", event);
    }

    public void pickMeshInfoByPoint(ReadableArray viewIds, ReadableMap coordinate) {
        List<SubMesh> allMesh = null;
        if (viewIds != null) {
            allMesh = new ArrayList<>();
            for (int i = 0, size = viewIds.size(); i < size; i++) {
                AMapMesh view = findViewById(viewIds.getInt(i));
                if (view != null) {
                    allMesh.add((SubMesh) view.getMesh());
                }
            }
        }
        pushMeshInfoEvent(null, Types.mapToLatLng(coordinate), allMesh);
    }

    private WritableMap positionToMap(AMap map, LatLng latLng) {
        WritableMap event = Arguments.createMap();
        event.putMap("latLng", Types.latLngToMap(latLng));
        event.putMap("point", Types.pointToMap(map.getProjection().toScreenLocation(latLng)));
        return event;
    }

    private void pushMeshInfoEvent(String name, LatLng latLng, List<SubMesh> allMesh) {
        if (openglRender != null && latLng != null) {
            List<Map<String, Object>> allMeshInfo = openglRender.pickMeshInfoByPoint(latLng, allMesh);
            WritableArray meshInfoArray = Arguments.createArray();
            for (Map<String, Object> meshInfo : allMeshInfo) {
                if (meshInfo != null) {
                    WritableMap meshInfoMap = Arguments.createMap();
                    Object faceIndex = meshInfo.get("faceIndex");
                    meshInfoMap.putInt("faceIndex", faceIndex == null ? 0 : (int) faceIndex);
                    float[] projectionPoint = (float[]) meshInfo.get("projectionPoint");
                    WritableArray ppArray = Arguments.createArray();
                    if (projectionPoint != null) {
                        for (float v : projectionPoint) ppArray.pushDouble(v);
                    }
                    meshInfoMap.putArray("projectionPoint", ppArray);
                    AMapMesh view = meshMap.get((SubMesh) meshInfo.get("meshObject"));
                    meshInfoMap.putInt("meshViewId", view != null ? view.getId() : -1);
                    if (name == null) {
                        meshInfoArray.pushMap(meshInfoMap);
                    } else if (view != null) {
                        WritableMap event = Arguments.createMap();
                        event.putMap("position", positionToMap(getMap(), latLng));
                        event.putMap("meshInfo", meshInfoMap);
                        dispatchEvent(view.getId(), name, event);
                    }
                }
            }
            if (name == null) {
                WritableMap event = Arguments.createMap();
                event.putString("trigger", "pickMeshInfoByPoint");
                event.putArray("meshInfoList", meshInfoArray);
                dispatchEvent("onPickMeshInfo", event);
            }
        }
    }

    private void dispatchEvent(int id, String name, WritableMap data) {
        if (rctEventEmitter == null) {
            rctEventEmitter = ((ReactContext) getContext()).getJSModule(RCTEventEmitter.class);
        }
        rctEventEmitter.receiveEvent(id, name, data);
    }

    private void dispatchEvent(String name, WritableMap data) {
        dispatchEvent(getId(), name, data);
    }

}
