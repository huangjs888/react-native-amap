package com.huangjs.amap;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.Map;

public class AMapViewManager extends ViewGroupManager<AMapView> {

    private static final String REACT_CLASS = "AMapView";
    private static final int GET_CAMERA_POSITION = 0;
    private static final int ANIMATE_CAMERA_POSITION = 1;
    private static final int POINT_TO_COORDINATE = 2;
    private static final int COORDINATE_TO_POINT = 3;
    private static final int PICK_MESH_INFO_BY_POINT = 4;
    private final ReactApplicationContext reactAppContext;

    public AMapViewManager(ReactApplicationContext context) {
        this.reactAppContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected AMapView createViewInstance(@NonNull ThemedReactContext themedContext) {
        return new AMapView(themedContext, reactAppContext);
    }

    @Override
    public void onDropViewInstance(AMapView view) {
        view.destroy();
        view.onDestroy();
        super.onDropViewInstance(view);
    }

    @Override
    public void addView(AMapView view, View child, int index) {
        view.addOverlay(child);
        super.addView(view, child, index);
    }

    @Override
    public void removeViewAt(AMapView view, int index) {
        view.removeOverlay(view.getChildAt(index));
        super.removeViewAt(view, index);
    }

    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        Map export = super.getExportedCustomDirectEventTypeConstants();
        if (export == null) {
            export = MapBuilder.newHashMap();
        }
        export.put("onLoaded", MapBuilder.of("registrationName", "onLoaded"));
        export.put("onClick", MapBuilder.of("registrationName", "onClick"));
        export.put("onLongClick", MapBuilder.of("registrationName", "onLongClick"));
        export.put("onCameraMoving", MapBuilder.of("registrationName", "onCameraMoving"));
        export.put("onCameraChange", MapBuilder.of("registrationName", "onCameraChange"));
        export.put("onLocationChange", MapBuilder.of("registrationName", "onLocationChange"));
        export.put("onGetCameraPosition", MapBuilder.of("registrationName", "onGetCameraPosition"));
        export.put("onPointToCoordinate", MapBuilder.of("registrationName", "onPointToCoordinate"));
        export.put("onCoordinateToPoint", MapBuilder.of("registrationName", "onCoordinateToPoint"));
        export.put("onPickMeshInfo", MapBuilder.of("registrationName", "onPickMeshInfo"));
        return export;
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "getCameraPosition", GET_CAMERA_POSITION,
                "animateCameraPosition", ANIMATE_CAMERA_POSITION,
                "pointToCoordinate", POINT_TO_COORDINATE,
                "coordinateToPoint", COORDINATE_TO_POINT,
                "pickMeshInfoByPoint", PICK_MESH_INFO_BY_POINT);
    }

    @Override
    public void receiveCommand(@NonNull AMapView view, String commandId, @Nullable ReadableArray args) {
        super.receiveCommand(view, commandId, args);
        int commandIdInt = Integer.parseInt(commandId);
        switch (commandIdInt) {
            case GET_CAMERA_POSITION:
                view.getCameraPosition();
                break;
            case ANIMATE_CAMERA_POSITION:
                view.animateCameraPosition(args.getMap(0), args.getInt(1));
                break;
            case POINT_TO_COORDINATE:
                if (args != null) view.pointToCoordinate(args.getMap(0));
                break;
            case COORDINATE_TO_POINT:
                if (args != null) view.coordinateToPoint(args.getMap(0));
                break;
            case PICK_MESH_INFO_BY_POINT:
                if (args != null) view.pickMeshInfoByPoint(args.getArray(0), args.getMap(1));
                break;
            default: {
            }
        }
    }

    @ReactProp(name = "initialCameraPosition")
    public void setInitialCameraPosition(AMapView view, ReadableMap cameraPosition) {
        view.setInitialCameraPosition(cameraPosition);
    }

    @ReactProp(name = "cameraPosition")
    public void setCameraPosition(AMapView view, ReadableMap cameraPosition) {
        view.setCameraPosition(cameraPosition);
    }

    @ReactProp(name = "openglEventEnabled")
    public void setOpenglEvent(AMapView view, boolean enable) {
        view.setOpenglEvent(enable);
    }

    @ReactProp(name = "locationIconEnabled")
    public void setMyLocationIconEnabled(AMapView view, Boolean enabled) {
        view.setMyLocationIconEnabled(enabled);
    }

    @ReactProp(name = "locationEnabled")
    public void setMyLocationEnabled(AMapView view, Boolean enabled) {
        view.setMyLocationEnabled(enabled);
    }

    @ReactProp(name = "mapType")
    public void setMapType(AMapView view, int mapType) {
        view.getMap().setMapType(Types.MAP_TYPES[mapType]);
    }

    // 设置是否显示3D建筑物，默认显示。
    @ReactProp(name = "buildingsEnabled")
    public void setBuildingsEnabled(AMapView view, Boolean enabled) {
        view.getMap().showBuildings(enabled);
    }

    // 设置是否显示底图文字标注，默认显示
    @ReactProp(name = "textEnabled")
    public void setTextEnabled(AMapView view, Boolean enabled) {
        view.getMap().showMapText(enabled);
    }

    @ReactProp(name = "indoorViewEnabled")
    public void setIndoorViewEnabled(AMapView view, Boolean enabled) {
        view.getMap().showIndoorMap(enabled);
    }

    @ReactProp(name = "trafficEnabled")
    public void setTrafficEnabled(AMapView view, Boolean enabled) {
        view.getMap().setTrafficEnabled(enabled);
    }

    @ReactProp(name = "maxZoom")
    public void setMaxZoom(AMapView view, float maxZoom) {
        view.getMap().setMaxZoomLevel(maxZoom);
    }

    @ReactProp(name = "minZoom")
    public void setMinZoom(AMapView view, float minZoom) {
        view.getMap().setMinZoomLevel(minZoom);
    }

    @ReactProp(name = "compassEnabled")
    public void setCompassEnabled(AMapView view, Boolean enabled) {
        view.getMap().getUiSettings().setCompassEnabled(enabled);
    }

    @ReactProp(name = "zoomControlsEnabled")
    public void setZoomControlsEnabled(AMapView view, Boolean enabled) {
        view.getMap().getUiSettings().setZoomControlsEnabled(enabled);
    }

    @ReactProp(name = "scaleControlsEnabled")
    public void setScaleControlsEnabled(AMapView view, Boolean enabled) {
        view.getMap().getUiSettings().setScaleControlsEnabled(enabled);
    }

    @ReactProp(name = "locationButtonEnabled")
    public void setMyLocationButtonEnabled(AMapView view, Boolean enabled) {
        view.getMap().getUiSettings().setMyLocationButtonEnabled(enabled);
    }

    @ReactProp(name = "zoomGesturesEnabled")
    public void setZoomGesturesEnabled(AMapView view, Boolean enabled) {
        view.getMap().getUiSettings().setZoomGesturesEnabled(enabled);
    }

    @ReactProp(name = "scrollGesturesEnabled")
    public void setScrollGesturesEnabled(AMapView view, Boolean enabled) {
        view.getMap().getUiSettings().setScrollGesturesEnabled(enabled);
    }

    @ReactProp(name = "rotateGesturesEnabled")
    public void setRotateGesturesEnabled(AMapView view, Boolean enabled) {
        view.getMap().getUiSettings().setRotateGesturesEnabled(enabled);
    }

    @ReactProp(name = "tiltGesturesEnabled")
    public void setTiltGesturesEnabled(AMapView view, Boolean enabled) {
        view.getMap().getUiSettings().setTiltGesturesEnabled(enabled);
    }

}
