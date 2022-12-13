package com.huangjs.amap;

import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

public class AMapMeshManager extends ViewGroupManager<AMapMesh> {
  private static final String REACT_CLASS = "AMap.Mesh";
  private final ReactApplicationContext reactAppContext;

  public AMapMeshManager(ReactApplicationContext context) {
    this.reactAppContext = context;
  }

  @NonNull
  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @NonNull
  @Override
  public AMapMesh createViewInstance(@NonNull ThemedReactContext themedContext) {
    return new AMapMesh(themedContext, reactAppContext);
  }

  @Override
  public void onDropViewInstance(@NonNull AMapMesh view) {
    super.onDropViewInstance(view);
  }

  @Override
  public void addView(AMapMesh view, View child, int index) {
    super.addView(view, child, index);
  }

  @Override
  public void removeViewAt(AMapMesh view, int index) {
    super.removeViewAt(view, index);
  }

  @Override
  public Map getExportedCustomDirectEventTypeConstants() {
    Map export = super.getExportedCustomDirectEventTypeConstants();
    if (export == null) {
      export = MapBuilder.newHashMap();
    }
    export.put("onPress", MapBuilder.of("registrationName", "onPress"));
    export.put("onLongPress", MapBuilder.of("registrationName", "onLongPress"));
    export.put("onRendered", MapBuilder.of("registrationName", "onRendered"));
    return export;
  }

  @ReactProp(name = "transparent", defaultBoolean = true)
  public void transparentEnabled(AMapMesh view, boolean enabled) {
    view.transparentEnabled(enabled);
  }

  @ReactProp(name = "backOrFront")
  public void setBackOrFront(AMapMesh view, int backOrFront) {
    view.setBackOrFront(backOrFront);
  }

  @ReactProp(name = "depthTest", defaultBoolean = true)
  public void depthTestEnabled(AMapMesh view, boolean enabled) {
    view.depthTestEnabled(enabled);
  }

  @ReactProp(name = "drawMode")
  public void setDrawMode(AMapMesh view, String drawMode) {
    view.setDrawMode(drawMode);
  }

  @ReactProp(name = "coordinate")
  public void setPosition(AMapMesh view, ReadableMap coordinate) {
    view.setPosition(Types.mapToLatLng(coordinate));
  }

  @ReactProp(name = "rotateX")
  public void setRotateX(AMapMesh view, float angle) {
    view.setRotate("x", angle);
  }

  @ReactProp(name = "rotateY")
  public void setRotateY(AMapMesh view, float angle) {
    view.setRotate("y", angle);
  }

  @ReactProp(name = "rotateZ")
  public void setRotateZ(AMapMesh view, float angle) {
    view.setRotate("z", angle);
  }

  @ReactProp(name = "scale")
  public void setScale(AMapMesh view, float scale) {
    view.setScale(new float[]{scale, scale, scale});
  }

  @ReactProp(name = "dataSource")
  public void setDataSource(AMapMesh view, ReadableMap dataSource) {
    view.setDataSource(dataSource);
  }

  @ReactProp(name = "request")
  public void setRequest(AMapMesh view, ReadableMap request) {
    view.setRequest(request);
  }

  @ReactProp(name = "valueDomain")
  public void setValueDomain(AMapMesh view, ReadableMap valueDomain) {
    view.setValueDomain(valueDomain);
  }
}
